package net.devtech.filepipeline.impl;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;

import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualRoot;

public abstract class InternalVirtualPath implements VirtualPath {
	public static final Cleaner ARCHIVE_CACHE = Cleaner.create();

	protected SoftReference<VirtualRoot> archive;

	protected abstract VirtualRoot createSource(ClosableVirtualRoot source, boolean create) throws IOException;

	public VirtualRoot openAsSource(ClosableVirtualRoot source, boolean create) throws IOException {
		VirtualRoot archive = this.archive.get();
		if(archive == null || archive.isInvalid()) {
			VirtualRoot src = this.createSource(source, create);
			this.archive = new SoftReference<>(src);
			if(src instanceof ClosableVirtualRoot i) {
				ARCHIVE_CACHE.register(src, i::close);
			}
			return src;
		}
		return archive;
	}

	public VirtualRoot asSource(boolean create) throws IOException {
		VirtualRoot root = this.getRoot();
		if(root instanceof InternalVirtualSource i) {
			return this.openAsSource(i.getClosable(), create);
		} else {
			throw new IllegalStateException(this.getRoot().getClass() + " does not implement " + InternalVirtualSource.class);
		}
	}

	@Override
	public VirtualRoot openAsSource() throws IOException {
		return this.asSource(false);
	}
}
