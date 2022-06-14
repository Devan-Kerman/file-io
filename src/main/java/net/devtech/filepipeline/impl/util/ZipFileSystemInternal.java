package net.devtech.filepipeline.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
		boolean nativ = false;
		try {
			Path temp = Files.createTempDirectory("file-pipeline-natives");
			String property = System.getProperty("os.name").toLowerCase(Locale.ROOT);
			if(property.contains("win")) {
				nativ = tryLoadLibrary("native_.dll", temp, nativeExceptions);
			} else if(property.contains("linux")) {
				nativ = tryLoadLibrary("native_.so", temp, nativeExceptions);
			} else if(property.contains("osx") || property.contains("mac")) {
				nativ = tryLoadLibrary("native_.dylib", temp, nativeExceptions);
			}
		} catch(IOException e) {
			nativeExceptions.add(e);
		}
		CAN_USE_NATIVE = nativ;
		
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
		try(InputStream stream = ZipFileSystemInternal.class.getResourceAsStream("/" + name)) {
			Files.copy(stream, attemptA);
		}
		try {
			System.load(attemptA.toAbsolutePath().toString());
			return true;
		} catch(Exception e) {
			exceptions.add(e);
			return false;
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
				sync(system);
			} else {
				throw new IllegalStateException("Unable to sync file system!");
			}
		}
	}
	
	private static native void sync(FileSystem zipFileSystem);
}
