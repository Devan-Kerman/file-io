package net.devtech.filepipeline.impl.multi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualSource;
import org.jetbrains.annotations.Nullable;

public class MultiDirectory implements VirtualDirectory {
	final VirtualDirectory parent;
	final VirtualSource source;
	final Iterable<? extends VirtualDirectory> directories;
	final Iterable<VirtualPath> walker;
	final String path;

	public MultiDirectory(VirtualDirectory parent, VirtualSource source, Iterable<? extends VirtualDirectory> directories) {
		this.parent = parent;
		this.source = source;
		if(!directories.iterator().hasNext()) {
			throw new IllegalArgumentException("directories is empty!");
		}
		this.directories = directories;
		this.walker = new IterableIterable<>(directories, VirtualDirectory::walk);
		this.path = directories.iterator().next().relativePath();
	}

	public void validateState() {
		if(this.source.isInvalid()) {
			throw new IllegalStateException("source " + this.source + " of directory " + this.path + " was invalidated!");
		}
	}

	public VirtualPath find(String relativePath, boolean directory, boolean file) {
		this.validateState();
		List<VirtualDirectory> dirs = null;
		for(VirtualDirectory dir : this.directories) {
			VirtualPath path = dir.find(relativePath);
			if(path instanceof VirtualFile v && file) {
				return v;
			} else if(path instanceof VirtualDirectory d && directory) {
				if(dirs == null) {
					dirs = new ArrayList<>();
				}
				dirs.add(d);
			}
		}
		if(dirs != null) {
			return new MultiDirectory(this, this.source, dirs);
		} else {
			return null;
		}
	}

	@Override
	public VirtualPath find(String relativePath) {
		return this.find(relativePath, true, true);
	}

	@Override
	public @Nullable VirtualDirectory findDirectory(String relativePath) {
		return (VirtualDirectory) this.find(relativePath, true, false);
	}

	@Override
	public @Nullable VirtualFile findFile(String relativePath) {
		return (VirtualFile) this.find(relativePath, false, true);
	}

	@Override
	public Iterable<VirtualPath> walk() {
		this.validateState();
		return this.walker;
	}

	@Override
	public Stream<VirtualPath> stream() {
		this.validateState();
		var builder = Stream.<Stream<VirtualPath>>builder();
		for(VirtualDirectory directory : this.directories) {
			builder.add(directory.stream());
		}
		return builder.build().flatMap(s -> s);
	}

	@Override
	public VirtualSource getRoot() {
		return this.source;
	}

	@Override
	public @Nullable VirtualDirectory getParent() {
		return this.parent;
	}

	@Override
	public String relativePath() {
		return this.path;
	}

	@Override
	public VirtualSource openAsSource() {
		this.validateState();
		return this.source;
	}

	@Override
	public boolean exists() {
		this.validateState();
		for(VirtualDirectory directory : this.directories) {
			if(directory.exists()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof MultiDirectory dir && this.directories.equals(dir.directories);
	}

	@Override
	public int hashCode() {
		return this.directories.hashCode();
	}

	@Override
	public String toString() {
		return this.relativePath();
	}
}
