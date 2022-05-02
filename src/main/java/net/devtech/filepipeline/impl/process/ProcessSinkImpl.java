package net.devtech.filepipeline.impl.process;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.ProcessRoot;
import net.devtech.filepipeline.api.source.ProcessSink;
import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.api.source.VirtualSink;

public class ProcessSinkImpl implements ProcessSink {
	final VirtualRoot root;
	final VirtualSink sink;

	public ProcessSinkImpl(VirtualRoot root, VirtualSink sink) {
		this.root = root;
		this.sink = sink;
	}

	@Override
	public void close() throws Exception {
		((ProcessRoot)this.getSource()).close();
	}

	@Override
	public VirtualSink subsink(VirtualPath path) throws IOException {
		return this.sink.subsink(path);
	}

	@Override
	public VirtualRoot getSource() {
		return this.root;
	}

	@Override
	public VirtualFile outputFile(VirtualDirectory directory, String relative) {
		return this.sink.outputFile(directory, relative);
	}

	@Override
	public VirtualDirectory outputDir(String path) {
		return this.sink.outputDir(path);
	}

	@Override
	public void copy(VirtualPath from, VirtualPath to) {
		this.sink.copy(from, to);
	}

	@Override
	public void write(VirtualFile path, ByteBuffer buffer) {
		this.sink.write(path, buffer);
	}
}
