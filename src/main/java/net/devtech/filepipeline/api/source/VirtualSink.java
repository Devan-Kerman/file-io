package net.devtech.filepipeline.api.source;

import java.nio.ByteBuffer;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.impl.InternalVirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;
import net.devtech.filepipeline.impl.util.ReadOnlySourceException;

public interface VirtualSink extends AutoCloseable {
	static VirtualSink primaryDrive() {
		try {
			return ((InternalVirtualSource)VirtualRoot.primaryDrive()).createSink();
		} catch(ReadOnlySourceException e) {
			throw FPInternal.rethrow(e);
		}
	}

	static VirtualSink workingDirectory() {
		try {
			return ((InternalVirtualSource)VirtualRoot.workingDirectory()).createSink();
		} catch(ReadOnlySourceException e) {
			throw FPInternal.rethrow(e);
		}
	}

	static VirtualSink ofFilePath(String path) {
		try {
			return ((InternalVirtualSource)VirtualRoot.ofFilePath(path)).createSink();
		} catch(ReadOnlySourceException e) {
			throw FPInternal.rethrow(e);
		}
	}

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

	/**
	 * closes the {@link #getSource()}
	 */
	@Override
	void close() throws Exception;
}
