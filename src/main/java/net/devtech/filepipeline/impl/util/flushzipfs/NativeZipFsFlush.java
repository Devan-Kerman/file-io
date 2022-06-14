package net.devtech.filepipeline.impl.util.flushzipfs;

import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NativeZipFsFlush {
	public static final List<Exception> NATIVE_EXCEPTIONS;
	static {
		List<Exception> nativeExceptions = new ArrayList<>();
		try {
			Path temp = Files.createTempDirectory("file-pipeline-natives");
			String property = System.getProperty("os.name").toLowerCase(Locale.ROOT);
			if(property.contains("win")) {
				tryLoadLibrary("native_.dll", temp);
			} else if(property.contains("linux")) {
				tryLoadLibrary("native_.so", temp);
			} else if(property.contains("osx") || property.contains("mac")) {
				tryLoadLibrary("native_.dylib", temp);
			}
		} catch(Exception e) {
			nativeExceptions.add(e);
		}
		NATIVE_EXCEPTIONS = List.copyOf(nativeExceptions);
	}
	
	private static void tryLoadLibrary(String name, Path temp) throws Exception {
		Path attemptA = temp.resolve(name);
		try(InputStream stream = NativeZipFsFlush.class.getResourceAsStream("/" + name)) {
			Files.copy(stream, attemptA);
		}
		System.load(attemptA.toAbsolutePath().toString());
	}
	
	public static native void flushZipFs(FileSystem zipFileSystem);
}
