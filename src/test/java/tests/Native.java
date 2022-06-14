package tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.devtech.filepipeline.impl.util.flushzipfs.NativeZipFsFlush;
import org.junit.Test;

public class Native {
	@Test
	public void main() throws IOException {
		Path of = Path.of("test.jar");
		FileSystem system = FileSystems.newFileSystem(of, Map.of("create", "true"));
		for(int i = 0; i < 1024; i++) {
			Path path = system.getPath("test" + i + ".txt");
			Files.write(path, new byte[1024]);
		}
		NativeZipFsFlush.flushZipFs(system);
		System.out.println("Reading file!");
		int entries = 0;
		try(ZipInputStream in = new ZipInputStream(new FileInputStream(of.toFile()))) {
			ZipEntry entry;
			while((entry = in.getNextEntry()) != null) {
				System.out.println(entry.getName());
				entries++;
				in.closeEntry();
			}
		}
		System.out.println("Found entries: " + entries);
	}
}
