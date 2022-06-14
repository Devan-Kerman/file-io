package net.devtech.filepipeline.impl;

import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import net.devtech.filepipeline.api.source.VirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;

public abstract class ClosableVirtualSource implements InternalVirtualSource, AutoCloseable, VirtualSource {
	final AtomicBoolean isInvalid = new AtomicBoolean();
	List<ClosableVirtualSource> children = List.of();
	SoftReference<VirtualSource> ref;
	Cleaner.Cleanable clean;

	protected abstract Callable<?> close0();
	
	@Override
	public synchronized void flush() {
		for(ClosableVirtualSource child : this.children) {
			child.flush();
		}
	}
	
	@Override
	public void close() {
		if(this.isInvalid.getAndSet(true)) {
			return;
		}
		// has to be done in reverse order
		try {
			if(this.ref != null) {
				this.ref.clear();
				this.ref = null;
			}
			
			if(this.clean != null) { // remove from cleaner
				this.clean.clean();
				this.clean = null;
			} else {
				this.cleanupFunction().call();
			}
		} catch(Exception e) {
			throw FPInternal.rethrow(e);
		}
	}

	public Callable<?> cleanupFunction() {
		List<ClosableVirtualSource> children = this.children;
		Callable<?> closer = this.close0();
		AtomicBoolean invalid = this.isInvalid;
		return () -> {
			if(invalid.getAndSet(true)) { // already closed
				return null;
			}
			
			for(ClosableVirtualSource child : children) {
				child.close();
			}

			// object is unreachable anyways, so we don't have to worry about setting isInvalid to true
			closer.call();
			return null;
		};
	}

	@Override
	public boolean isInvalid() {
		return this.isInvalid.get();
	}

	public synchronized void insert(ClosableVirtualSource source) {
		List<ClosableVirtualSource> children = this.children;
		if(children.isEmpty()) {
			this.children = children = new ArrayList<>();
		}
		children.add(source);
	}

	@Override
	public ClosableVirtualSource getClosable() {
		return this;
	}

	@Override
	public boolean exists() {
		return !this.isInvalid();
	}

}
