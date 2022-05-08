package net.devtech.filepipeline.impl;

import java.nio.file.Path;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.source.VirtualSource;
import net.devtech.filepipeline.api.source.VirtualSink;
import net.devtech.filepipeline.impl.nio.NioVirtualDirectory;
import net.devtech.filepipeline.impl.nio.NioVirtualDirectorySink;
import net.devtech.filepipeline.impl.util.ReadOnlySourceException;

public class DirectoryVirtualSource implements VirtualSource, InternalVirtualSource {
	protected final NioVirtualDirectory directory;
	final ClosableVirtualSource closable;

	public DirectoryVirtualSource(Path path, ClosableVirtualSource closable) {
		this.directory = new NioVirtualDirectory(this, null, path);
		this.closable = closable;
	}

	@Override
	public boolean exists() {
		return this.directory.exists();
	}

	@Override
	public boolean isInvalid() {
		return this.closable != null && this.closable.isInvalid.get();
	}

	@Override
	public VirtualDirectory rootDir() {
		return this.directory;
	}

	@Override
	public ClosableVirtualSource getClosable() {
		return this.closable;
	}

	@Override
	public VirtualSink createSink() throws ReadOnlySourceException {
		return new NioVirtualDirectorySink(this, this.directory);
	}

	@Override
	public void close() throws Exception {
	}
}
