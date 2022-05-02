package net.devtech.filepipeline.api.source;

import java.nio.file.Path;

import net.devtech.filepipeline.impl.DirectoryVirtualRoot;
import net.devtech.filepipeline.impl.process.ProcessRootImpl;

public interface ProcessRoot extends AutoCloseable, VirtualRoot {
	static ProcessRoot primaryDrive() {
		return ofFilePath("/");
	}

	static ProcessRoot workingDirectory() {
		return ofFilePath("");
	}

	static ProcessRoot ofFilePath(String path) {
		return new ProcessRootImpl(c -> new DirectoryVirtualRoot(Path.of(path), c));
	}
}
