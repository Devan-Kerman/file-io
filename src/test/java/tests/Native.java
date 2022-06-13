package tests;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;

import net.devtech.filepipeline.impl.util.ZipFileSystemInternal;
import org.junit.Test;

public class Native {
	@Test
	public void main() throws IOException {
		FileSystem system = FileSystems.newFileSystem(Path.of("test.jar"), Map.of("create", "true"));
		ZipFileSystemInternal.sync(system);
	}
}
