package net.devtech.filepipeline.impl.process;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.source.VirtualSource;
import net.devtech.filepipeline.api.source.VirtualSink;
import net.devtech.filepipeline.impl.ClosableVirtualSource;
import net.devtech.filepipeline.impl.InternalVirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;
import net.devtech.filepipeline.impl.util.ReadOnlySourceException;

public class ProcessSourceImpl implements VirtualSource, InternalVirtualSource {
	final VirtualSource root;
	final AtomicBoolean invalid = new AtomicBoolean();
	final Closable closable = new Closable(this.invalid);

	public ProcessSourceImpl(Function<ClosableVirtualSource, VirtualSource> root) {
		this.root = root.apply(this.closable);
	}

	protected void validateState() {
		if(this.root.isInvalid()) {
			throw new IllegalStateException("This source is invalidated!");
		}
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
	public void flush() {
		this.validateState();
		((InternalVirtualSource)this.root).flush();
	}
	
	@Override
	public VirtualDirectory rootDir() {
		this.validateState();
		return this.root.rootDir();
	}

	@Override
	public boolean isInvalid() {
		return this.invalid.get();
	}

	public static final class Closable extends ClosableVirtualSource {
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
	public ClosableVirtualSource getClosable() {
		return this.closable;
	}

	@Override
	public boolean exists() {
		return this.root.exists();
	}
}
