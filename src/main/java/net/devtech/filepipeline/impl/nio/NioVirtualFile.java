package net.devtech.filepipeline.impl.nio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.impl.ClosableVirtualRoot;
import net.devtech.filepipeline.impl.WritableVirtualFile;
import net.devtech.filepipeline.impl.util.FPInternal;

public class NioVirtualFile extends NioVirtualPath implements VirtualFile {
	public static final Map<String, ?> NIO_CREATE = Map.of("create", "true");
	private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);
	ByteBuffer content;

	public NioVirtualFile(VirtualRoot source, VirtualDirectory parent, Path path) {
		super(source, parent, path);
	}

	@Override
	public void copyTo(VirtualPath to) throws IOException {
		if(to instanceof NioVirtualFile f) {
			f.content = this.content;
			Files.copy(this.getPath(), f.getPath());
		} else {
			this.copyTo0(to);
		}
	}

	public void write(ByteBuffer buffer) {
		this.content = buffer;
		try(OutputStream stream = Files.newOutputStream(this.getPath())) {
			stream.write(buffer.array(), buffer.arrayOffset(), buffer.limit());
		} catch(IOException e) {
			throw FPInternal.rethrow(e);
		}
	}

	@Override
	protected void copyTo0(VirtualPath path) throws IOException {
		if(path instanceof WritableVirtualFile w) {
			w.setContents(this.getContents());
		} else {
			throw new IllegalArgumentException(path.getClass() + " does not implement " + WritableVirtualFile.class);
		}
	}

	public NioVirtualFile(VirtualRoot source, VirtualDirectory parent, Path path, ByteBuffer content) {
		super(source, parent, path);
		this.content = content;
	}

	public void setEmptyContents() {
		this.content = EMPTY;
	}

	@Override
	public ByteBuffer getContents() {
		ByteBuffer content = this.content;
		if(content != null) {
			return content;
		} else {
			return this.content = FPInternal.read(this.getPath());
		}
	}

	@Override
	protected VirtualRoot createSource(ClosableVirtualRoot source, boolean create) throws IOException {
		FileSystem system;
		if(create) {
			system = FileSystems.newFileSystem(this.getPath(), NIO_CREATE);
		} else {
			system = FileSystems.newFileSystem(this.getPath());
		}
		FileSystemVirtualRoot virtual = new FileSystemVirtualRoot(system, true);
		source.insert(virtual);
		return virtual;
	}
}
