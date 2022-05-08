package net.devtech.filepipeline.impl.nio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualSource;
import net.devtech.filepipeline.impl.InternalVirtualPath;
import org.jetbrains.annotations.Nullable;

public abstract class NioVirtualPath extends InternalVirtualPath {
	final VirtualSource source;
	final VirtualDirectory parent;
	private final Path path;
	String relativePath;

	public NioVirtualPath(VirtualSource source, VirtualDirectory parent, Path path) {
		this.source = source;
		this.parent = parent;
		this.path = path; // todo relativize
	}

	@Override
	public boolean exists() {
		return Files.exists(this.path);
	}

	@Override
	public String relativePath() {
		String path = this.relativePath;
		if(path == null) {
			return this.relativePath = this.getPath().toString().replace(this.getPath().getFileSystem().getSeparator(), "/");
		}
		return path;
	}

	@Override
	public VirtualSource getRoot() {
		return this.source;
	}

	@Override
	public @Nullable VirtualDirectory getParent() {
		return this.parent;
	}

	public void copyTo(VirtualPath to) throws IOException {
		if(to instanceof NioVirtualPath n) {
			Files.copy(this.getPath(), n.getPath());
		} else {
			this.copyTo0(to);
		}
	}

	public Path getPath() {
		return path;
	}

	public abstract void delete();

	protected abstract void copyTo0(VirtualPath path) throws IOException;
}
