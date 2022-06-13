package net.devtech.filepipeline.impl.util;

import java.nio.file.FileSystem;

public class ZipFileSystemInternal {
	static {
		System.loadLibrary("native_");
	}
	
	public static native void sync(FileSystem zipFileSystem);
}
