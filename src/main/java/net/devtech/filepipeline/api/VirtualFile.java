package net.devtech.filepipeline.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.devtech.filepipeline.impl.util.ByteBufferInputStream;

public interface VirtualFile extends VirtualPath {
	ByteBuffer getContents();
	
	default ByteBufferInputStream newInputStream() {
		return new ByteBufferInputStream(this.getContents());
	}

	default BufferedReader newReader(Charset charset) {
		return new BufferedReader(new InputStreamReader(new ByteBufferInputStream(this.getContents()), charset));
	}

	default String asString(Charset set) {
		ByteBuffer contents = this.getContents();
		return new String(contents.array(), contents.arrayOffset(), contents.limit(), set);
	}

	default byte[] allBytes() {
		ByteBuffer contents = this.getContents();
		byte[] buf = new byte[contents.remaining()];
		contents.get(buf);
		return buf;
	}
}
