package net.devtech.filepipeline.impl.util.flushzipfs;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystem;

import net.devtech.filepipeline.impl.util.FPInternal;
import net.devtech.filepipeline.impl.util.ReflectiveHacks;
import sun.misc.Unsafe;

public class UnsafeReflectionZipFsFlush {
	public static final MethodHandle ZIP_FILE_SYSTEM_SYNC;
	public static final Throwable ERROR;
	static {
		MethodHandle sync = null;
		Throwable error = null;
		try {
			Class<?> zipfs = ReflectionZipFsFlush.ZIP_FS, current = ReflectiveHacks.class;
			
			// get unsafe
			Unsafe unsafe = null;
			for(Field field : Unsafe.class.getDeclaredFields()) {
				if(field.getType() == Unsafe.class && Modifier.isStatic(field.getModifiers())) {
					try {
						field.setAccessible(true);
						unsafe = (Unsafe) field.get(null);
						break;
					} catch(Exception e) {}
				}
			}
			
			if(unsafe == null) {
				throw new UnsupportedOperationException("Unsafe not supported!");
			}
			
			// find Class's module object
			Field module = Class.class.getDeclaredField("module");
			long offset = unsafe.objectFieldOffset(module);
			
			// set the current class's Module to the ZipFileSystem's Module temporarily to bypass reflection restrictions
			Module nioModule = zipfs.getModule(), currentModule = current.getModule();
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			
			try {
				unsafe.putObjectVolatile(current, offset, nioModule);
				MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(zipfs, lookup);
				sync = privateLookup.findVirtual(zipfs, "sync", MethodType.methodType(void.class));
			} finally {
				// restore ZipFileSystem's module
				unsafe.putObjectVolatile(current, offset, currentModule);
			}
		} catch(Throwable e) {
			error = e;
		}
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
