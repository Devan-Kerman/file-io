package net.devtech.filepipeline.impl.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
	ByteBuffer buffer;
	int pos, mark;
	
	public ByteBufferInputStream(ByteBuffer buffer) {
		this.buffer = buffer;
	}
	
	@Override
	public boolean markSupported() {
		return true;
	}
	
	@Override
	public void mark(int readlimit) {
		this.mark = this.pos;
	}
	
	@Override
	public void reset() {
		this.pos = this.mark;
	}
	
	@Override
	public int available() {
		return this.buffer.limit() - this.pos;
	}
	
	@Override
	public long skip(long n) {
		if(n > 0) {
			int skip = (int) Math.min(this.available(), Math.min(n, Integer.MAX_VALUE));
			this.pos += skip;
			return skip;
		} else {
			return 0;
		}
	}
	
	@Override
	public void skipNBytes(long n) throws IOException {
		if(n > 0 && this.skip(n) != n) {
			throw new EOFException();
		}
	}
	
	@Override
	public long transferTo(OutputStream out) throws IOException {
		ByteBuffer buffer = this.buffer;
		if(buffer.hasArray()) {
			byte[] array = buffer.array();
			int offset = buffer.arrayOffset();
			out.write(array, offset, buffer.limit());
		}
		return super.transferTo(out);
	}
	
	@Override
	public int read(byte[] b, int off, int len) {
		int pos = this.pos;
		ByteBuffer buffer = this.buffer;
		int read = Math.min(buffer.limit() - pos, len);
		buffer.get(pos, b, 0, read);
		this.pos = read + pos;
		return read == 0 ? -1 : read;
	}
	
	@Override
	public int read() {
		int pos = this.pos;
		ByteBuffer buffer = this.buffer;
		if(pos < buffer.limit()) {
			this.pos = pos+1;
			return buffer.get(pos);
		}
		return -1;
	}
	
	@Override
	public void close() throws IOException {
		this.buffer = null;
	}
}
