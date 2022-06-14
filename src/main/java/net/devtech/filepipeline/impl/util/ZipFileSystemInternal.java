package net.devtech.filepipeline.impl.util;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ZipFileSystemInternal {
	public static final boolean CAN_USE_NATIVE;
	public static final MethodHandle HANDLE;
	public static final Class<?> ZIP_FS;
	
	static {
		ReflectiveOperationException reflectErr = null;
		MethodHandle handle = null;
		Class<?> zipfs = null;
		try {
			zipfs = Class.forName("jdk.nio.zipfs.ZipFileSystem");
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(zipfs, lookup);
			handle = privateLookup.findVirtual(zipfs, "sync", MethodType.methodType(void.class));
		} catch(ReflectiveOperationException e) {
			reflectErr = e;
		}
		ZIP_FS = zipfs;
		HANDLE = handle;
		
		List<Exception> nativeExceptions = new ArrayList<>();
		boolean nativeSuccess = true;
		try {
			Path temp = Files.createTempDirectory("file-pipeline-natives");
			if(tryLoadLibrary("native_.dll", temp, nativeExceptions)) {
				if(tryLoadLibrary("native_.so", temp, nativeExceptions)) {
					if(tryLoadLibrary("native_.dylib", temp, nativeExceptions)) {
						System.err.println("Unable to load ZipFileSystem#sync reflection bypass native!");
						nativeSuccess = false;
					}
				}
			}
		} catch(IOException e) {
			nativeSuccess = false;
			nativeExceptions.add(e);
		}
		CAN_USE_NATIVE = nativeSuccess;
		
		if(reflectErr != null && !CAN_USE_NATIVE) {
			for(Exception exception : nativeExceptions) {
				exception.printStackTrace();
			}
			reflectErr.printStackTrace();
			throw new UnsupportedOperationException("Unable to bypass ZipFileSystem#sync access! Please use the following jvm flags \"--add-opens jdk.zipfs/jdk.nio.zipfs=ALL-UNNAMED\"");
		}
	}
	
	private static boolean tryLoadLibrary(String name, Path temp, List<Exception> exceptions) throws IOException {
		Path attemptA = temp.resolve(name);
		Files.copy(ZipFileSystemInternal.class.getResourceAsStream("/" + name), attemptA);
		try {
			System.load(attemptA.toAbsolutePath().toString());
			return false;
		} catch(Exception e) {
			exceptions.add(e);
			return true;
		}
	}
	
	public static void syncZipFileSystem(FileSystem system) {
		if(ZIP_FS.isInstance(system)) {
			if(HANDLE != null) {
				try {
					HANDLE.invoke(system);
				} catch(Throwable e) {
					throw FPInternal.rethrow(e);
				}
			} else if(CAN_USE_NATIVE) {
				sync0(system);
			} else {
				throw new IllegalStateException("Unable to sync file system!");
			}
		}
	}
	
	private static native void sync0(FileSystem zipFileSystem);
}
