package net.devtech.filepipeline.api.source;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.impl.DirectoryVirtualRoot;
import net.devtech.filepipeline.impl.process.ProcessRootImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface VirtualRoot extends VirtualDirectory, AutoCloseable {
	static VirtualRoot primaryDrive() {
		return ofFilePath("/");
	}

	static VirtualRoot workingDirectory() {
		return ofFilePath("");
	}

	static VirtualRoot ofFilePath(String path) {
		return new ProcessRootImpl(c -> new DirectoryVirtualRoot(Path.of(path), c));
	}

	VirtualDirectory rootDir();

	@Override
	default @Nullable VirtualDirectory getParent() {
		return null;
	}

	@Override
	default Iterable<VirtualPath> walk() {
		return List.of(this.rootDir());
	}

	@Override
	default Stream<VirtualPath> stream() {
		return Stream.of(this.rootDir());
	}

	@Override
	default @Nullable VirtualPath find(String relativePath) {
		return this.rootDir().find(relativePath);
	}

	/**
	 * @return itself
	 */
	@Override
	@Contract("->this")
	default VirtualRoot getRoot() {
		return this;
	}

	@Override
	default String relativePath() {
		return this.rootDir().relativePath();
	}

	@Override
	@Contract("->this")
	default VirtualRoot openAsSource() {
		return this;
	}

	boolean isInvalid(); // todo make operations using invalid roots throw error

	/**
	 * Invalidates the current virtual root
	 */
	@Override
	void close() throws Exception;
}
