package net.devtech.filepipeline.impl.process;

import java.nio.ByteBuffer;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualSource;
import net.devtech.filepipeline.api.source.VirtualSink;

public class ProcessSinkImpl implements VirtualSink {
	final VirtualSource root;
	final VirtualSink sink;

	public ProcessSinkImpl(VirtualSource root, VirtualSink sink) {
		this.root = root;
		this.sink = sink;
	}

	protected void validateState() {
		if(this.root.isInvalid()) {
			throw new IllegalStateException("VirtualSource " + this.root + " for this VirtualSink was invalidated, cannot reuse this object!");
		}
	}

	@Override
	public void close() throws Exception {
		this.root.close();
	}

	@Override
	public VirtualSink subsink(VirtualPath path) {
		this.validateState();
		return this.sink.subsink(path);
	}

	@Override
	public VirtualSource getSource() {
		return this.root;
	}

	@Override
	public VirtualFile outputFile(VirtualDirectory directory, String relative) {
		this.validateState();
		return this.sink.outputFile(directory, relative);
	}

	@Override
	public VirtualDirectory outputDir(String path) {
		this.validateState();
		return this.sink.outputDir(path);
	}

	@Override
	public VirtualDirectory outputDir(VirtualDirectory directory, String path) {
		this.validateState();
		return this.sink.outputDir(directory, path);
	}

	@Override
	public void delete(VirtualPath path) {
		this.validateState();
		this.sink.delete(path);
	}

	@Override
	public void deleteContents(VirtualDirectory path) {
		this.validateState();
		this.sink.deleteContents(path);
	}

	@Override
	public void copy(VirtualPath from, VirtualPath to) {
		this.validateState();
		this.sink.copy(from, to);
	}

	@Override
	public void write(VirtualFile path, ByteBuffer buffer) {
		this.validateState();
		this.sink.write(path, buffer);
	}
}
