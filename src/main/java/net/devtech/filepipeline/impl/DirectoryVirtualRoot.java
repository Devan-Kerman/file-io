package net.devtech.filepipeline.impl;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.api.source.VirtualSink;
import net.devtech.filepipeline.impl.nio.NioVirtualDirectory;
import net.devtech.filepipeline.impl.nio.NioVirtualDirectorySink;
import net.devtech.filepipeline.impl.util.ReadOnlySourceException;

public class DirectoryVirtualRoot implements VirtualRoot, InternalVirtualSource {
	protected final NioVirtualDirectory directory;
	final ClosableVirtualRoot closable;

	public DirectoryVirtualRoot(Path path, ClosableVirtualRoot closable) {
		this.directory = new NioVirtualDirectory(this, null, path);
		this.closable = closable;
	}

	@Override
	public boolean isInvalid() {
		return this.closable != null && this.closable.isInvalid;
	}

	@Override
	public VirtualDirectory rootDir() {
		return this.directory;
	}

	@Override
	public ClosableVirtualRoot getClosable() {
		return this.closable;
	}

	@Override
	public VirtualSink createSink() throws ReadOnlySourceException {
		return new NioVirtualDirectorySink(this, this.directory);
	}

	public static class Closable extends ClosableVirtualRoot {
		final NioVirtualDirectory directory;
		public Closable(Path path) {
			this.directory = new NioVirtualDirectory(this, null, path);
		}

		@Override
		public VirtualDirectory rootDir() {
			return this.directory;
		}

		@Override
		public VirtualSink createSink() throws ReadOnlySourceException {
			return new NioVirtualDirectorySink(this, this.directory);
		}

		@Override
		protected Callable<?> close0() {
			return () -> null;
		}
	}
}
