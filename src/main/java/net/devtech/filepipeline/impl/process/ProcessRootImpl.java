package net.devtech.filepipeline.impl.process;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.api.source.VirtualSink;
import net.devtech.filepipeline.impl.ClosableVirtualRoot;
import net.devtech.filepipeline.impl.InternalVirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;
import net.devtech.filepipeline.impl.util.ReadOnlySourceException;

public class ProcessRootImpl implements VirtualRoot, InternalVirtualSource {
	final VirtualRoot root;
	final AtomicBoolean invalid = new AtomicBoolean();
	final Closable closable = new Closable(this.invalid);

	public ProcessRootImpl(Function<ClosableVirtualRoot, VirtualRoot> root) {
		this.root = root.apply(this.closable);
	}

	@Override
	public void close() {
		this.closable.close();
	}

	@Override
	public VirtualSink createSink() {
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
		return this.invalid.get();
	}

	public static final class Closable extends ClosableVirtualRoot {
		final AtomicBoolean invalid;

		public Closable(AtomicBoolean invalid) {this.invalid = invalid;}

		@Override
		protected Callable<?> close0() {
			var invalid = this.invalid;
			return () -> invalid.getAndSet(true);
		}

		@Override public VirtualDirectory rootDir() {throw new UnsupportedOperationException();}
		@Override public VirtualSink createSink() throws ReadOnlySourceException {throw new UnsupportedOperationException();}
	}

	@Override
	public ClosableVirtualRoot getClosable() {
		return this.closable;
	}
}
