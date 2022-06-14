package net.devtech.filepipeline.impl.util.flushzipfs;

import java.nio.file.FileSystem;

public enum ZipFileSystemFlusher {
	REFLECTION {
		@Override
		protected boolean isSupported() {
			return ReflectionZipFsFlush.ZIP_FILE_SYSTEM_SYNC != null;
		}
		
		@Override
		protected void flush0(FileSystem system) {
			ReflectionZipFsFlush.flushZipFs(system);
		}
	},
	UNSAFE_REFLECTION {
		@Override
		protected boolean isSupported() {
			return UnsafeReflectionZipFsFlush.ZIP_FILE_SYSTEM_SYNC != null;
		}
		
		@Override
		protected void flush0(FileSystem system) {
			UnsafeReflectionZipFsFlush.flushZipFs(system);
		}
	},
	NATIVE {
		@Override
		protected boolean isSupported() {
			return NativeZipFsFlush.NATIVE_EXCEPTIONS.isEmpty();
		}
		
		@Override
		protected void flush0(FileSystem system) {
			NativeZipFsFlush.flushZipFs(system);
		}
	};
	
	public static final ZipFileSystemFlusher SUPPORTED;
	static {
		ZipFileSystemFlusher supported = null;
		for(ZipFileSystemFlusher value : ZipFileSystemFlusher.values()) {
			if(value.isSupported()) {
				supported = value;
			}
		}
		
		if(supported == null) {
			throw new IllegalStateException("No supported method for flushing ZipFileSystem! Try \"--add-opens jdk.zipfs/jdk.nio.zipfs=ALL-UNNAMED\" jvm flags.");
		}
		SUPPORTED = supported;
	}
	
	protected abstract boolean isSupported();
	protected abstract void flush0(FileSystem system);
	
	public final void flush(FileSystem system) {
		if(this.isSupported()) {
			this.flush0((FileSystem) ReflectionZipFsFlush.ZIP_FS.cast(system));
		} else {
			throw new UnsupportedOperationException(this + " ZipFileSystem flush");
		}
	}
}