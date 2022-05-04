package net.devtech.filepipeline.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.api.source.VirtualSink;
import net.devtech.filepipeline.impl.InternalVirtualPath;
import net.devtech.filepipeline.impl.InternalVirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;

public class NioVirtualDirectorySink implements VirtualSink {
	final VirtualRoot source;
	final NioVirtualDirectory directory;

	public NioVirtualDirectorySink(VirtualRoot source, NioVirtualDirectory directory) {
		this.source = source;
		this.directory = directory;
	}

	@Override
	public VirtualSink subsink(VirtualPath directory) {
		try {
			return ((InternalVirtualSource)((InternalVirtualPath)directory).asSource(true)).createSink();
		} catch(Exception e) {
			throw FPInternal.rethrow(e);
		}
	}

	@Override
	public VirtualRoot getSource() {
		return this.source;
	}

	@Override
	public VirtualFile outputFile(VirtualDirectory directory, String relative) {
		if(directory.getRoot() != this.source) {
			throw new IllegalArgumentException(directory + " does not belong to " + this.source);
		}
		return ((NioVirtualDirectory) directory).outputFile(relative);
	}

	@Override
	public VirtualDirectory outputDir(String path) {
		return this.directory.outputDir(path);
	}

	@Override
	public void copy(VirtualPath from, VirtualPath to) {
		if(directory.getRoot() != this.source) {
			throw new IllegalArgumentException(directory + " does not belong to " + this.source);
		}
		try {
			((NioVirtualPath) from).copyTo(to);
		} catch(IOException e) {
			throw FPInternal.rethrow(e);
		}
	}

	@Override
	public void write(VirtualFile path, ByteBuffer buffer) {
		if(directory.getRoot() != this.source) {
			throw new IllegalArgumentException(directory + " does not belong to " + this.source);
		}
		((NioVirtualFile) path).write(buffer);
	}
}