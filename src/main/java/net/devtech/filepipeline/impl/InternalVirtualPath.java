package net.devtech.filepipeline.impl;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;

import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;

public abstract class InternalVirtualPath implements VirtualPath {
	public static final Cleaner ARCHIVE_CACHE = Cleaner.create();

	/**
	 * Delays cache deletion until garbage collector thinks it needs more memory
	 */
	protected SoftReference<VirtualSource> archive;

	protected abstract VirtualSource createSource(ClosableVirtualSource source, boolean create) throws IOException;

	protected abstract String invalidState();

	protected void validateState() {
		if(this.getRoot().isInvalid()) {
			throw new IllegalStateException(this.invalidState());
		}
	}

	public synchronized VirtualSource openAsSource(ClosableVirtualSource source, boolean create) throws Exception {
		VirtualSource archive = this.archive != null ? this.archive.get() : null;
		if(archive == null || archive.isInvalid()) {
			VirtualSource src = this.createSource(source, create);
			SoftReference<VirtualSource> archiveRef = new SoftReference<>(src);
			this.archive = archiveRef;
			if(src instanceof ClosableVirtualSource i) { // once it is actually garbage collected, the archive is properly disposed of
				i.ref = archiveRef;
				Callable<?> callable = i.cleanupFunction();
				i.clean = ARCHIVE_CACHE.register(src, () -> {
					try {
						callable.call();
					} catch(Exception e) {
						e.printStackTrace();
					}
				});
			}
			return src;
		}
		return archive;
	}

	public VirtualSource asSource(boolean create) throws Exception {
		VirtualSource root = this.getRoot();
		if(root instanceof InternalVirtualSource i) {
			return this.openAsSource(i.getClosable(), create);
		} else {
			throw new IllegalStateException(this.getRoot().getClass() + " does not implement " + InternalVirtualSource.class);
		}
	}

	@Override
	public VirtualSource openAsSource() throws Exception {
		this.validateState();
		return this.asSource(false);
	}
}
