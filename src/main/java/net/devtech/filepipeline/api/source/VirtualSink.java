package net.devtech.filepipeline.api.source;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.impl.InternalVirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;
import net.devtech.filepipeline.impl.util.ReadOnlySourceException;

public interface VirtualSink extends AutoCloseable {
	static VirtualSink primaryDrive() {
		try {
			return ((InternalVirtualSource) VirtualSource.primaryDrive()).createSink();
		} catch(ReadOnlySourceException e) {
			throw FPInternal.rethrow(e);
		}
	}

	static VirtualSink workingDirectory() {
		try {
			return ((InternalVirtualSource) VirtualSource.workingDirectory()).createSink();
		} catch(ReadOnlySourceException e) {
			throw FPInternal.rethrow(e);
		}
	}

	static VirtualSink ofFilePath(String path) {
		try {
			return ((InternalVirtualSource) VirtualSource.ofFilePath(path)).createSink();
		} catch(ReadOnlySourceException e) {
			throw FPInternal.rethrow(e);
		}
	}

	VirtualSink subsink(VirtualPath path);

	/**
	 * @return a view of the output filesystem
	 */
	VirtualSource getSource();

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
	default VirtualDirectory outputDir(String path) {
		return this.outputDir(this.getSource().getRoot(), path);
	}

	VirtualDirectory outputDir(VirtualDirectory directory, String path);

	void delete(VirtualPath path);

	void deleteContents(VirtualDirectory path);
	
	void copy(InputStream from, VirtualFile to);

	void copy(VirtualPath from, VirtualPath to);

	void write(VirtualFile path, ByteBuffer buffer);
	
	/**
	 * Flush the current VirtualSink, this is sometimes needed for zip files
	 */
	void flush();

	default void createIfAbsent(VirtualPath path) {
		if(!path.exists()) {
			this.create(path);
		}
	}

	default void create(VirtualPath path) {
		if(!path.exists()) {
			if(path instanceof VirtualFile) {
				this.outputFile(path.getParent(), path.fileName());
			} else {
				this.outputDir(path.getParent(), path.fileName());
			}
		} else {
			throw new IllegalArgumentException(path + " already exists!");
		}
	}

	default OutputStream newOutputStream(VirtualFile file) {
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				VirtualSink.this.write(file, ByteBuffer.wrap(this.buf, 0, this.count));
				super.close();
			}
		};
	}

	default Writer newWriter(VirtualFile file) {
		return new OutputStreamWriter(this.newOutputStream(file));
	}

	default void writeString(VirtualFile file, String contents, Charset charset) {
		this.write(file, charset.encode(contents));
	}

	default void write(VirtualFile path, byte[] data, int off, int len) {
		this.write(path, ByteBuffer.wrap(data, off, len));
	}

	/**
	 * closes the {@link #getSource()}
	 */
	@Override
	void close() throws Exception;
}
