package net.devtech.filepipeline.impl;

import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.impl.util.FPInternal;

public abstract class ClosableVirtualRoot implements InternalVirtualSource, AutoCloseable, VirtualRoot {
	public ClosableVirtualRoot next, child;
	boolean isInvalid;

	protected abstract void close0() throws Exception;

	protected void inv() throws Exception {
		ClosableVirtualRoot current = this;
		if(this.isInvalid) {
			return;
		}

		ClosableVirtualRoot child = current.child;
		if(child != null) {
			child.inv();
		}

		ClosableVirtualRoot next = current.next;
		if(next != null) {
			next.inv();
		}

		this.isInvalid = true;
		this.close0();
	}

	@Override
	public void close() {
		// has to be done in reverse order
		try {
			if(this.child != null) {
				this.child.inv();
			}

			this.isInvalid = true;
			this.close0();
		} catch(Exception e) {
			throw FPInternal.rethrow(e);
		}
	}

	@Override
	public boolean isInvalid() {
		return this.isInvalid;
	}

	public void insert(ClosableVirtualRoot source) {
		source.next = this.child;
		this.child = source;
	}

	@Override
	public ClosableVirtualRoot getClosable() {
		return this;
	}
}
