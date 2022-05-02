package net.devtech.filepipeline.api;

import java.io.IOException;

import net.devtech.filepipeline.api.source.VirtualRoot;
import net.devtech.filepipeline.impl.util.FPInternal;
import org.jetbrains.annotations.Nullable;

public interface VirtualPath {
	VirtualRoot getRoot();

	/**
	 * if file is in root directory, returns {@link VirtualRoot}.
	 *  If the file <b>is</b> the root directory, returns null
	 */
	@Nullable
	VirtualDirectory getParent();

	String relativePath();

	/**
	 * @return opens the file as a new source
	 *  for example, if the file is a directory, this returns a new VirtualSource starting from that directory
	 */
	VirtualRoot openAsSource() throws IOException;

	@Nullable
	default VirtualRoot openAsSourceSilent() {
		try {
			return this.openAsSource();
		} catch(IOException e) {
			return null;
		}
	}

	default VirtualRoot openOrThrow() {
		try {
			return this.openAsSource();
		} catch(IOException e) {
			throw FPInternal.rethrow(e);
		}
	}
}
