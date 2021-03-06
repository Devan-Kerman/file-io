package net.devtech.filepipeline.api.source;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualFile;
import net.devtech.filepipeline.api.VirtualPath;
import net.devtech.filepipeline.impl.DirectoryVirtualSource;
import net.devtech.filepipeline.impl.process.ProcessSourceImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VirtualSource extends VirtualDirectory, AutoCloseable {
	static VirtualSource primaryDrive() {
		return ofFilePath("/");
	}

	static VirtualSource workingDirectory() {
		return ofFilePath("");
	}

	static VirtualSource ofFilePath(String path) {
		return new ProcessSourceImpl(c -> new DirectoryVirtualSource(Path.of(path), c));
	}

	VirtualDirectory rootDir();

	@Override
	@Contract("->null")
	default @Nullable VirtualDirectory getParent() {
		return null;
	}

	@Override
	default Iterable<VirtualPath> walk() {
		return this.rootDir().walk();
	}

	@Override
	default Stream<VirtualPath> stream() {
		return this.rootDir().stream();
	}

	@Override
	default @Nullable VirtualPath find(String relativePath) {
		return this.rootDir().find(relativePath);
	}

	@Override
	default @NotNull VirtualFile getFile(String relativePath) {
		return this.rootDir().getFile(relativePath);
	}

	@Override
	default @NotNull VirtualDirectory getDir(String relativePath) {
		return this.rootDir().getDir(relativePath);
	}

	/**
	 * @return itself
	 */
	@Override
	@Contract("->this")
	default VirtualSource getRoot() {
		return this;
	}

	@Override
	default String relativePath() {
		return this.rootDir().relativePath();
	}

	@Override
	@Contract("->this")
	default VirtualSource openAsSource() {
		return this;
	}

	boolean isInvalid();

	/**
	 * Invalidates the current virtual root
	 */
	@Override
	void close() throws Exception;
}
