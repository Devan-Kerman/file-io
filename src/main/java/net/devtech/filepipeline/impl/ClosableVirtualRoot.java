package net.devtech.filepipeline.impl;

import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.impl.util.FPInternal;

public abstract class ClosableVirtualRoot implements InternalVirtualSource, AutoCloseable, VirtualRoot {
	static final class ChildRef {ClosableVirtualRoot child;}

	final AtomicBoolean isInvalid = new AtomicBoolean();
	ClosableVirtualRoot next;
	ChildRef holder = new ChildRef();
	SoftReference<VirtualRoot> ref;
	Cleaner.Cleanable clean;

	protected abstract Callable<?> close0();

	protected void inv() throws Exception {
		if(this.isInvalid.getAndSet(true)) {
			return;
		}

		ClosableVirtualRoot child = this.holder.child;
		if(child != null) {
			child.inv();
		}

		ClosableVirtualRoot next = this.next;
		if(next != null) {
			next.inv();
		}

		this.close0().call();
	}

	@Override
	public void close() {
		if(this.isInvalid.getAndSet(true)) {
			return;
		}
		// has to be done in reverse order
		try {
			if(this.holder.child != null) {
				this.holder.child.inv();
			}

			if(this.ref != null) {
				this.ref.clear();
				this.ref = null;
			}
			if(this.clean != null) { // remove from cleaner
				this.clean.clean();
				this.clean = null;
			}

			this.close0().call();
		} catch(Exception e) {
			throw FPInternal.rethrow(e);
		}
	}

	public Callable<?> cleanupFunction() {
		ChildRef holder = this.holder;
		Callable<?> closer = this.close0();
		AtomicBoolean invalid = this.isInvalid;
		return () -> {
			if(invalid.getAndSet(true)) { // already closed
				return null;
			}

			if(holder.child != null) {
				holder.child.inv();
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

	public void insert(ClosableVirtualRoot source) {
		source.next = this.holder.child;
		this.holder.child = source;
	}

	@Override
	public ClosableVirtualRoot getClosable() {
		return this;
	}
}
