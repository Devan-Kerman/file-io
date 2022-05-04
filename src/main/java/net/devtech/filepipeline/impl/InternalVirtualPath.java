package net.devtech.filepipeline.impl;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;

import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.impl.util.FPInternal;

public abstract class InternalVirtualPath implements VirtualPath {
	public static final Cleaner ARCHIVE_CACHE = Cleaner.create();

	/**
	 * Delays cache deletion until garbage collector thinks it needs more memory
	 */
	protected SoftReference<VirtualRoot> archive;

	protected abstract VirtualRoot createSource(ClosableVirtualRoot source, boolean create) throws IOException;

	public VirtualRoot openAsSource(ClosableVirtualRoot source, boolean create) throws Exception {
		VirtualRoot archive = this.archive.get();
		if(archive == null || archive.isInvalid()) {
			VirtualRoot src = this.createSource(source, create);
			SoftReference<VirtualRoot> archiveRef = new SoftReference<>(src);
			this.archive = archiveRef;
			if(src instanceof ClosableVirtualRoot i) { // once it is actually garbage collected, the archive is properly disposed of
				i.ref = archiveRef;
				Callable<?> callable = i.cleanupFunction();
				i.clean = ARCHIVE_CACHE.register(src, () -> {
					try {
						callable.call();
					} catch(Exception e) {
						throw FPInternal.rethrow(e);
					}
				});
			}
			return src;
		}
		return archive;
	}

	public VirtualRoot asSource(boolean create) throws Exception {
		VirtualRoot root = this.getRoot();
		if(root instanceof InternalVirtualSource i) {
			return this.openAsSource(i.getClosable(), create);
		} else {
			throw new IllegalStateException(this.getRoot().getClass() + " does not implement " + InternalVirtualSource.class);
		}
	}

	@Override
	public VirtualRoot openAsSource() throws Exception {
		return this.asSource(false);
	}
}
