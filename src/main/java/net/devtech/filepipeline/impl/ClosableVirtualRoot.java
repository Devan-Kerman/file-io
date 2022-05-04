package net.devtech.filepipeline.impl;

import java.util.concurrent.Callable;

import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.impl.util.FPInternal;

public abstract class ClosableVirtualRoot implements InternalVirtualSource, AutoCloseable, VirtualRoot {
	static final class ChildRef {ClosableVirtualRoot child;}
	public ClosableVirtualRoot next;
	public ChildRef holder = new ChildRef();

	boolean isInvalid;

	protected abstract Callable<?> close0();

	protected void inv() throws Exception {
		ClosableVirtualRoot current = this;
		if(this.isInvalid) {
			return;
		}

		ClosableVirtualRoot child = current.holder.child;
		if(child != null) {
			child.inv();
		}

		ClosableVirtualRoot next = current.next;
		if(next != null) {
			next.inv();
		}

		this.isInvalid = true;
		this.close0().call();
	}

	@Override
	public void close() {
		// has to be done in reverse order
		try {
			if(this.holder.child != null) {
				this.holder.child.inv();
			}

			this.isInvalid = true;
			this.close0().call();
		} catch(Exception e) {
			throw FPInternal.rethrow(e);
		}
	}

	public Callable<?> cleanupFunction() {
		ChildRef holder = this.holder;
		Callable<?> closer = this.close0();
		return () -> {
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
		return this.isInvalid;
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
