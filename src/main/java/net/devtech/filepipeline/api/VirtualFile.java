package net.devtech.filepipeline.api;

import java.nio.ByteBuffer;

public interface VirtualFile extends VirtualPath {
	ByteBuffer getContents();
}
