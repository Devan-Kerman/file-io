package net.devtech.filepipeline.impl.nio;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.impl.ClosableVirtualRoot;
import net.devtech.filepipeline.impl.DirectoryVirtualRoot;
import net.devtech.filepipeline.impl.util.FPInternal;
import net.devtech.filepipeline.impl.util.PathStringIterator;
import org.jetbrains.annotations.Nullable;

public class NioVirtualDirectory extends NioVirtualPath implements VirtualDirectory {
	Map<String, NioVirtualPath> children = Map.of();
	/**
	 * we use an int counter here instead of a boolean because it works better for concurrency. If this value is the same length as the size of
	 * {@link
	 * #children} then the cache has all values
	 */
	int completedSize = -1;

	public NioVirtualDirectory(VirtualRoot source, VirtualDirectory parent, Path path) {
		super(source, parent, path);
	}

	@Override
	public VirtualPath find(String relativePath) {
		VirtualPath current = this;
		for(String path : PathStringIterator.iterable(relativePath)) {
			current = find(current, path);
		}
		return current;
	}

	@Override
	public Iterable<VirtualPath> walk() {
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
					NioVirtualPath path = NioVirtualDirectory.this.resolveChild(next.getFileName().toString(), next, false, false);
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
				return NioVirtualDirectory.this.resolveChild(next.getFileName().toString(), next, false, false);
			}, () -> {
				this.completedSize = count.incrementAndGet();
			}), false);
		} else {
			return StreamSupport.stream(FPInternal.map(paths.spliterator(), next -> {
				return NioVirtualDirectory.this.resolveChild(next.getFileName().toString(), next, false, false);
			}, () -> {}), false);
		}
	}

	public VirtualFile outputFile(String relative) {
		return (VirtualFile) this.resolveChild(relative, null, true, false);
	}

	public VirtualDirectory outputDir(String relative) {
		PathStringIterator iterator = new PathStringIterator(relative);
		NioVirtualDirectory dir = this;
		while(iterator.hasNext()) {
			String path = iterator.next();
			dir = (NioVirtualDirectory) this.resolveChild(path, null, true, true);
		}
		return dir;
	}

	private static VirtualPath find(VirtualPath current, String name) {
		if(current instanceof NioVirtualDirectory d) {
			return d.resolveChild(name, null, false, false);
		} else {
			throw new IllegalStateException(current.relativePath() + " is a file, not directory, so \"" + name + "\" is invalid!");
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

	protected NioVirtualPath resolveChild(String fileName, @Nullable Path path, boolean create, boolean createDirectory) {
		NioVirtualPath child;
		Map<String, NioVirtualPath> children = this.children;
		if(this.completedSize == children.size()) {
			child = children.get(fileName);
		} else if(!this.children.isEmpty()) {
			child = children.computeIfAbsent(fileName, f -> this.createPath(f, path, create, createDirectory));
		} else {
			child = this.createPath(fileName, path, create, createDirectory);
			if(child != null) {
				synchronized(this) {
					children = children.isEmpty() ? new ConcurrentHashMap<>() : this.children;
					children.put(fileName, child);
					this.children = children;
				}
			}
		}

		return child;
	}

	private NioVirtualPath createPath(String fileName, @Nullable Path path, boolean create, boolean createDirectory) {
		if(path == null) {
			path = this.getPath().resolve(fileName);
		}
		if(create || Files.exists(path)) {
			if(createDirectory && !Files.exists(path)) {
				try {
					Files.createDirectory(path);
				} catch(IOException e) {
					throw FPInternal.rethrow(e);
				}
			}
			if(createDirectory || Files.isDirectory(path)) {
				return new NioVirtualDirectory(this.source, this, path);
			} else {
				NioVirtualFile file = new NioVirtualFile(this.source, this, path);
				if(create) {
					file.setEmptyContents();
				}
				return file;
			}
		} else {
			return null;
		}
	}

	@Override
	protected VirtualRoot createSource(ClosableVirtualRoot source, boolean create) {
		return new DirectoryVirtualRoot(this.getPath(), source);
	}
}
