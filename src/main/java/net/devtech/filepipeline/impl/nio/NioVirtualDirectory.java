package net.devtech.filepipeline.impl.nio;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualSource;
import net.devtech.filepipeline.impl.ClosableVirtualSource;
import net.devtech.filepipeline.impl.DirectoryVirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;
import net.devtech.filepipeline.impl.util.PathStringIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NioVirtualDirectory extends NioVirtualPath implements VirtualDirectory {
	Map<String, NioVirtualPath> children = Map.of();
	/**
	 * we use an int counter here instead of a boolean because it works better for concurrency. If this value is the same length as the size of
	 * {@link
	 * #children} then the cache has all values
	 */
	int completedSize = -1;

	public NioVirtualDirectory(VirtualSource source, VirtualDirectory parent, Path path) {
		super(source, parent, path);
	}

	@Override
	public VirtualPath find(String relativePath) {
		this.validateState();
		VirtualPath current = this;
		for(String path : PathStringIterator.iterable(relativePath)) {
			current = find(current, path, CreatePathSettings.FIND);
			if(current == null || !current.exists()) {
				return null;
			}
		}
		return current;
	}

	@Override
	public @NotNull VirtualFile getFile(String relativePath) {
		this.validateState();
		VirtualPath current = this;
		Iterator<String> iterator = PathStringIterator.iterable(relativePath).iterator();
		while(iterator.hasNext()) {
			String path = iterator.next();
			current = find(current, path, iterator.hasNext() ? CreatePathSettings.GET_DIR : CreatePathSettings.GET_FILE);
		}
		return (VirtualFile) current;
	}

	@Override
	public @NotNull VirtualDirectory getDir(String relativePath) {
		this.validateState();
		VirtualPath current = this;
		for(String path : PathStringIterator.iterable(relativePath)) {
			current = find(current, path, CreatePathSettings.GET_DIR);
		}
		return (VirtualDirectory) current;
	}

	@Override
	public Iterable<VirtualPath> walk() {
		this.validateState();
		if(this.completedSize == this.children.size()) {
			//noinspection unchecked
			return (Iterable) this.children.values();
		} else {
			DirectoryStream<Path> dirStream;
			try {
				dirStream = Files.newDirectoryStream(this.getPath());
			} catch(IOException e) {
				throw FPInternal.rethrow(e);
			}
			return () -> new Iterator<>() {
				final Iterator<Path> path = dirStream.iterator();
				int size;

				@Override
				public boolean hasNext() {
					return this.path.hasNext();
				}

				@Override
				public NioVirtualPath next() {
					Path next = this.path.next();
					NioVirtualPath path = NioVirtualDirectory.this.resolveChild(next.getFileName().toString(), next, CreatePathSettings.FIND);
					this.size++;
					if(!this.path.hasNext()) {
						NioVirtualDirectory.this.completedSize = this.size;
					}
					return path;
				}
			};
		}
	}

	@Override
	public Stream<VirtualPath> stream() {
		this.validateState();
		DirectoryStream<Path> paths;
		try {
			paths = Files.newDirectoryStream(this.getPath());
		} catch(IOException e) {
			throw FPInternal.rethrow(e);
		}
		if(this.completedSize == -1) {
			AtomicInteger count = new AtomicInteger();
			return StreamSupport.stream(FPInternal.map(paths.spliterator(), next -> {
				count.incrementAndGet();
				return NioVirtualDirectory.this.resolveChild(next.getFileName().toString(), next, CreatePathSettings.FIND);
			}, () -> {
				this.completedSize = count.incrementAndGet();
			}), false);
		} else {
			return StreamSupport.stream(FPInternal.map(paths.spliterator(), next -> {
				return NioVirtualDirectory.this.resolveChild(next.getFileName().toString(), next, CreatePathSettings.FIND);
			}, () -> {}), false);
		}
	}

	public VirtualFile outputFile(String relative) {
		return (VirtualFile) this.resolveChild(relative, null, CreatePathSettings.CREATE);
	}

	public VirtualDirectory outputDir(String relative) {
		PathStringIterator iterator = new PathStringIterator(relative);
		NioVirtualDirectory dir = this;
		while(iterator.hasNext()) {
			String path = iterator.next();
			dir = (NioVirtualDirectory) dir.resolveChild(path, null, CreatePathSettings.CREATE_DIRECTORY);
		}
		return dir;
	}

	@Override
	public void delete() {
		try {
			this.deleteContents();
			Files.deleteIfExists(this.getPath());
		} catch(IOException e) {
			throw FPInternal.rethrow(e);
		}
	}

	@Override
	protected void copyTo0(VirtualPath path) throws IOException {
		if(path instanceof VirtualDirectory v) {
			for(VirtualPath virt : this.walk()) {
				String relative = virt.relativePath();
				int index = relative.lastIndexOf('/');
				if(index == relative.length() - 1) { // exclude ending '/'
					index = relative.lastIndexOf('/', index - 1);
				}
				((NioVirtualPath) virt).copyTo0(v.resolve(relative.substring(index + 1)));
			}
		} else {
			throw new IllegalArgumentException(path + " is not a directory!");
		}
	}

	public void deleteContents() {
		this.depthStream().map(NioVirtualPath.class::cast).forEach(NioVirtualPath::delete);
	}

	@Override
	public String toString() {
		return "NioVirtualDirectory{" + this.relativePath + "}";
	}

	private static VirtualPath find(VirtualPath current, String name, CreatePathSettings settings) {
		if(current instanceof NioVirtualDirectory d) {
			return d.resolveChild(name, null, settings);
		} else {
			throw new IllegalStateException(current.relativePath() + " is a file, not directory, so \"" + name + "\" is invalid!");
		}
	}

	protected NioVirtualPath resolveChild(String fileName, @Nullable Path path, CreatePathSettings settings) {
		NioVirtualPath child;
		Map<String, NioVirtualPath> children = this.children;
		if(this.completedSize == children.size()) {
			child = children.get(fileName);
		} else if(!this.children.isEmpty()) {
			child = children.computeIfAbsent(fileName, f -> this.createPath(f, path, settings));
		} else {
			child = this.createPath(fileName, path, settings);
			if(child != null) {
				synchronized(this) {
					children = children.isEmpty() ? new ConcurrentHashMap<>() : this.children;
					children.put(fileName, child);
					this.children = children;
				}
			}
		}

		if(settings.create && child != null && !child.exists()) {
			try {
				if(settings.isDirectory) {
					Files.createDirectories(child.getPath());
				} else {
					Path path1 = child.getPath().toAbsolutePath();
					Path parent = path1.getParent();
					if(parent != null) {
						Files.createDirectories(parent);
					}
				}
			} catch(IOException e) {
				throw FPInternal.rethrow(e);
			}
		}

		return child;
	}

	private NioVirtualPath createPath(String fileName, @Nullable Path path, CreatePathSettings settings) {
		if(path == null) {
			path = this.getPath().resolve(fileName);
		}
		if(settings.forceExists || Files.exists(path)) {
			if((settings.isDirectory && settings.create) && !Files.exists(path)) {
				try {
					Files.createDirectory(path);
				} catch(IOException e) {
					throw FPInternal.rethrow(e);
				}
			}
			if(settings.isDirectory || Files.isDirectory(path)) {
				return new NioVirtualDirectory(this.source, this, path);
			} else {
				NioVirtualFile file = new NioVirtualFile(this.source, this, path);
				if(settings.create) {
					file.setEmptyContents();
				}
				return file;
			}
		} else {
			return null;
		}
	}

	@Override
	protected VirtualSource createSource(ClosableVirtualSource source, boolean create) {
		return new DirectoryVirtualSource(this.getPath(), source);
	}

	@Override
	protected String invalidState() {
		return "source " + this.source + " of directory " + this.relativePath + " was invalidated!";
	}

	public enum CreatePathSettings {
		FIND(true, false, false),
		GET_FILE(true, false, false),
		GET_DIR(true, false, true),
		CREATE(true, true, false),
		CREATE_DIRECTORY(true, true, true);

		final boolean forceExists, create, isDirectory;

		CreatePathSettings(boolean forceExists, boolean create, boolean isDirectory) {
			this.forceExists = forceExists;
			this.create = create;
			this.isDirectory = isDirectory;
		}
	}
}
