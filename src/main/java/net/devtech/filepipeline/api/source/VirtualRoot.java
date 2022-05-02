package net.devtech.filepipeline.api.source;

import java.io.IOException;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

import net.devtech.filepipeline.api.VirtualDirectory;
import net.devtech.filepipeline.api.VirtualPath;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface VirtualRoot extends VirtualDirectory {
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
	default VirtualRoot openAsSource() throws IOException {
		return this;
	}

	boolean isInvalid();
}
