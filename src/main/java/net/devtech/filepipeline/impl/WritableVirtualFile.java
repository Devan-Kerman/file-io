package net.devtech.filepipeline.impl;

import java.nio.ByteBuffer;

public interface WritableVirtualFile {
	void setContents(ByteBuffer buffer);
}
