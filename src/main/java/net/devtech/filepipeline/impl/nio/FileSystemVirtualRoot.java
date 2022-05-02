package net.devtech.filepipeline.impl.nio;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.source.VirtualSink;
import net.devtech.filepipeline.impl.ClosableVirtualRoot;
import net.devtech.filepipeline.impl.multi.MultiDirectory;
import net.devtech.filepipeline.impl.util.ReadOnlySourceException;

public class FileSystemVirtualRoot extends ClosableVirtualRoot {
	final boolean closeSystem;
	final MultiDirectory directory;
	final NioVirtualDirectory first;
	final FileSystem system;

	public FileSystemVirtualRoot(FileSystem system, boolean closeSystem) {
		this.system = system;
		this.closeSystem = closeSystem;
		Iterable<Path> directories = system.getRootDirectories();
		List<NioVirtualDirectory> roots = new ArrayList<>();
		for(Path directory : directories) {
			roots.add(new NioVirtualDirectory(this, this, directory));
		}
		this.first = roots.get(0);
		this.directory = new MultiDirectory(null, this, roots);
	}

	@Override
	public VirtualDirectory rootDir() {
		return this.directory;
	}

	@Override
	public VirtualSink createSink() throws ReadOnlySourceException {
		return new NioVirtualDirectorySink(this, this.first); // todo multiwrite to all root directories?
	}

	@Override
	protected void close0() throws Exception {
		if(this.closeSystem) {
			this.system.close();
		}
	}

	@Override
	public boolean isInvalid() {
		return super.isInvalid() || !this.system.isOpen();
	}
}
