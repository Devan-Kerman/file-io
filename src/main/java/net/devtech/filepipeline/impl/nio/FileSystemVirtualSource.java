package net.devtech.filepipeline.impl.nio;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.source.VirtualSink;
import net.devtech.filepipeline.impl.ClosableVirtualSource;
import net.devtech.filepipeline.impl.multi.MultiDirectory;
import net.devtech.filepipeline.impl.util.ReadOnlySourceException;

public class FileSystemVirtualSource extends ClosableVirtualSource {
	final boolean closeSystem;
	final MultiDirectory directory;
	final NioVirtualDirectory first;
	final FileSystem system;

	public FileSystemVirtualSource(FileSystem system, boolean closeSystem) {
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
	protected Callable<?> close0() {
		if(this.closeSystem) {
			FileSystem toClose = this.system;
			return () -> {
				toClose.close();
				return null;
			};
		} else {
			return () -> null;
		}
	}

	@Override
	public boolean isInvalid() {
		return super.isInvalid() || !this.system.isOpen();
	}

	@Override
	public String toString() {
		return "FileSystemVirtualSource{" + this.system + "}";
	}
}
