package net.devtech.filepipeline.api;

import net.devtech.filepipeline.api.source.VirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;
import org.jetbrains.annotations.Nullable;

public interface VirtualPath {
	VirtualSource getRoot();

	/**
	 * if file is in root directory, returns {@link VirtualSource}.
	 *  If the file <b>is</b> the root directory, returns null
	 */
	@Nullable
	VirtualDirectory getParent();

	String relativePath();

	default String fileName() {
		String path = this.relativePath();
		return path.substring(path.lastIndexOf('/')+1);
	}

	/**
	 * @return opens the file as a new source
	 *  for example, if the file is a directory, this returns a new VirtualSource starting from that directory
	 */
	VirtualSource openAsSource() throws Exception;

	boolean exists();

	@Nullable
	default VirtualSource openAsSourceSilent() {
		try {
			return this.openAsSource();
		} catch(Exception e) {
			return null;
		}
	}

	default VirtualSource openOrThrow() {
		try {
			return this.openAsSource();
		} catch(Exception e) {
			throw FPInternal.rethrow(e);
		}
	}

	default VirtualFile asFile() {
		return (VirtualFile) this;
	}

	default VirtualDirectory asDir() {
		return (VirtualDirectory) this;
	}
}
