package net.devtech.filepipeline.impl.util.flushzipfs;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.FileSystem;

import net.devtech.filepipeline.impl.util.FPInternal;

public class ReflectionZipFsFlush {
	public static final Class<?> ZIP_FS;
	public static final MethodHandle ZIP_FILE_SYSTEM_SYNC;
	public static final Throwable ERROR;
	static {
		MethodHandle sync = null;
		Throwable error = null;
		Class<?> zipfs = null;
		try {
			zipfs = Class.forName("jdk.nio.zipfs.ZipFileSystem");
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(zipfs, lookup);
			sync = privateLookup.findVirtual(zipfs, "sync", MethodType.methodType(void.class));
		} catch(ReflectiveOperationException e) {
			error = e;
		}
		ZIP_FS = zipfs;
		ZIP_FILE_SYSTEM_SYNC = sync;
		ERROR = error;
	}
	
	public static void flushZipFs(FileSystem system) {
		try {
			ZIP_FILE_SYSTEM_SYNC.invoke(system);
		} catch(Throwable e) {
			throw FPInternal.rethrow(e);
		}
	}
}
