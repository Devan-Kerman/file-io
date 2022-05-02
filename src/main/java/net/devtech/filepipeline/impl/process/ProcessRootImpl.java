package net.devtech.filepipeline.impl.process;

import java.io.IOException;
import java.util.function.Function;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.source.ProcessRoot;
import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.api.source.VirtualSink;
import net.devtech.filepipeline.impl.ClosableVirtualRoot;
import net.devtech.filepipeline.impl.InternalVirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;
import net.devtech.filepipeline.impl.util.ReadOnlySourceException;

public class ProcessRootImpl implements ProcessRoot, InternalVirtualSource {
	private final Closable closable = new Closable();
	final VirtualRoot root;
	boolean invalid;

	public ProcessRootImpl(Function<ClosableVirtualRoot, VirtualRoot> root) {
		this.root = root.apply(this.closable);
	}

	@Override
	public void close() throws Exception {
		this.closable.close();
	}

	@Override
	public VirtualSink createSink() throws ReadOnlySourceException {
		try {
			return new ProcessSinkImpl(this, ((InternalVirtualSource)this.root).createSink());
		} catch(IOException e) {
			throw FPInternal.rethrow(e);
		}
	}

	@Override
	public VirtualDirectory rootDir() {
		return this.root.rootDir();
	}

	@Override
	public boolean isInvalid() {
		return this.invalid;
	}

	public class Closable extends ClosableVirtualRoot {
		@Override
		protected void close0() throws Exception {
			ProcessRootImpl.this.invalid = true;
		}

		@Override public VirtualDirectory rootDir() {throw new UnsupportedOperationException();}
		@Override public VirtualSink createSink() throws ReadOnlySourceException {throw new UnsupportedOperationException();}
	}

	@Override
	public ClosableVirtualRoot getClosable() {
		return this.closable;
	}
}
