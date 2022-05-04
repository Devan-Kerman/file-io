package net.devtech.filepipeline.api.source;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;

public interface VirtualSink {
	VirtualSink subsink(VirtualPath path);

	/**
	 * @return a view of the output filesystem
	 */
	VirtualRoot getSource();

	default VirtualFile outputFile(String path) {
		int i = path.lastIndexOf('/');
		if(i == 0) {
			return this.outputFile(this.getSource(), path.substring(1));
		} else {
			return this.outputFile(this.outputDir(path.substring(0, i)), path.substring(i + 1));
		}
	}

	/**
	 * an VirtualFile representing the path, it is not created with empty contents
	 */
	VirtualFile outputFile(VirtualDirectory directory, String relative);

	/**
	 * @return a newly-created empty directory at a given location if a directory does not already exist.
	 */
	VirtualDirectory outputDir(String path);

	void copy(VirtualPath from, VirtualPath to);

	void write(VirtualFile path, ByteBuffer buffer);

	default void write(VirtualFile path, byte[] data, int off, int len) {
		this.write(path, ByteBuffer.wrap(data, off, len));
	}
}
