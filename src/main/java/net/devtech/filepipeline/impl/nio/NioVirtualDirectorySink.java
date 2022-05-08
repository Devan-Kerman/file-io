package net.devtech.filepipeline.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualSource;
import net.devtech.filepipeline.api.source.VirtualSink;
import net.devtech.filepipeline.impl.InternalVirtualPath;
import net.devtech.filepipeline.impl.InternalVirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;

public class NioVirtualDirectorySink implements VirtualSink {
	final VirtualSource source;
	final NioVirtualDirectory directory;

	public NioVirtualDirectorySink(VirtualSource source, NioVirtualDirectory directory) {
		this.source = source;
		this.directory = directory;
	}

	public void validateState() {
		if(this.source.isInvalid()) {
			throw new IllegalStateException("source " + this.source + " of directory " + this.directory + " was invalidated!");
		}
	}

	@Override
	public VirtualSink subsink(VirtualPath path) {
		try {
			this.validateState();
			return ((InternalVirtualSource)((InternalVirtualPath)path).asSource(true)).createSink();
		} catch(Exception e) {
			throw FPInternal.rethrow(e);
		}
	}

	@Override
	public VirtualSource getSource() {
		return this.source;
	}



	@Override
	public VirtualFile outputFile(VirtualDirectory directory, String relative) {
		this.validateState();
		if(directory.getRoot() != this.source) {
			throw new IllegalArgumentException(directory + " does not belong to " + this.source);
		}
		return ((NioVirtualDirectory) directory).outputFile(relative);
	}

	@Override
	public VirtualDirectory outputDir(String path) {
		return this.outputDir(this.directory, path);
	}

	@Override
	public VirtualDirectory outputDir(VirtualDirectory directory, String path) {
		this.validateState();
		if(directory.getRoot() != this.source) {
			throw new IllegalArgumentException(directory + " does not belong to " + this.source);
		}
		return ((NioVirtualDirectory)directory).outputDir(path);
	}

	@Override
	public void copy(VirtualPath from, VirtualPath to) {
		this.validateState();
		if(to.getRoot() != this.source) {
			throw new IllegalArgumentException(directory + " does not belong to " + this.source);
		}
		try {
			((NioVirtualPath) from).copyTo(to);
		} catch(IOException e) {
			throw FPInternal.rethrow(e);
		}
	}

	@Override
	public void delete(VirtualPath path) {
		this.validateState();
		if(path.getRoot() != this.source) {
			throw new IllegalArgumentException(directory + " does not belong to " + this.source);
		}
		((NioVirtualPath) path).delete();
	}

	@Override
	public void deleteContents(VirtualDirectory path) {
		this.validateState();
		if(path.getRoot() != this.source) {
			throw new IllegalArgumentException(directory + " does not belong to " + this.source);
		}
		((NioVirtualDirectory) path).deleteContents();
	}

	@Override
	public void write(VirtualFile path, ByteBuffer buffer) {
		this.validateState();
		if(path.getRoot() != this.source) {
			throw new IllegalArgumentException(path + " does not belong to " + this.source);
		}
		((NioVirtualFile) path).write(buffer);
	}

	@Override
	public void close() throws Exception {
		this.source.close();
	}
}