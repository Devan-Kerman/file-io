package net.devtech.filepipeline.api;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface VirtualFile extends VirtualPath {
	ByteBuffer getContents();

	default BufferedReader newReader(Charset charset) {
		return new BufferedReader(new StringReader(charset.decode(this.getContents()).toString()));
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
