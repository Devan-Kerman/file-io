package net.devtech.filepipeline.api;

import java.io.FileNotFoundException;
import java.util.Spliterator;
import java.util.stream.Stream;

import net.devtech.filepipeline.impl.util.FPInternal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VirtualDirectory extends VirtualPath {
	/**
	 * @param relativePath if {@param relativePath} starts with "/", the "/" is ignored, rather than finding the root directory
	 * @return the path at the location or null if it is not there
	 */
	@Nullable
	VirtualPath find(String relativePath);

	@Nullable
	default VirtualFile findFile(String relativePath) {
		VirtualPath resolve = this.find(relativePath);
		if(resolve instanceof VirtualFile v) {
			return v;
		} else {
			return null;
		}
	}

	@Nullable
	default VirtualDirectory findDirectory(String relativePath) {
		VirtualPath resolve = this.find(relativePath);
		if(resolve instanceof VirtualDirectory d) {
			return d;
		} else {
			return null;
		}
	}

	@NotNull
	default VirtualPath resolve(String relativePath) {
		var path = this.find(relativePath);
		if(path == null) {
			throw FPInternal.rethrow(new FileNotFoundException(this.relativePath() + "/" + relativePath));
		}
		return path;
	}

	@NotNull
	default VirtualFile resolveFile(String relativePath) {
		var path = this.findFile(relativePath);
		if(path == null) {
			throw FPInternal.rethrow(new FileNotFoundException(this.relativePath() + "/" + relativePath));
		}
		return path;
	}

	@NotNull
	default VirtualDirectory resolveDirectory(String relativePath) {
		var path = this.findDirectory(relativePath);
		if(path == null) {
			throw FPInternal.rethrow(new FileNotFoundException(this.relativePath() + "/" + relativePath));
		}
		return path;
	}

	Iterable<VirtualPath> walk();

	Stream<VirtualPath> stream();

	default Stream<VirtualPath> depthStream() {
		return this.stream().flatMap(v -> {
			if(v instanceof VirtualDirectory p) {
				return p.depthStream();
			} else {
				return Stream.of(v);
			}
		});
	}
}