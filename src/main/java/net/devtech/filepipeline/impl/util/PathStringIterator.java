package net.devtech.filepipeline.impl.util;

import java.util.Iterator;

public class PathStringIterator implements Iterator<String> {
	final String relativePath;
	final int length;
	int from, to;

	public static Iterable<String> iterable(String path) {
		return () -> new PathStringIterator(path);
	}

	public PathStringIterator(String path) {
		this.relativePath = path;
		this.length = path.length();
		int to = path.indexOf('/');
		if(to == 0) {
			this.from = 1;
			this.to = path.indexOf('/', 1);
		}
	}

	@Override
	public boolean hasNext() {
		return this.from != -1 && this.from != this.length;
	}

	@Override
	public String next() {
		String snip;
		if(this.to == -1) {
			snip = this.relativePath.substring(this.from);
			this.from = -1;
		} else {
			snip = this.relativePath.substring(this.from, this.to);
			this.from = this.to+1;
			this.to = this.relativePath.indexOf('/', this.from);
		}
		return snip;
	}
}
