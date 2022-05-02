package net.devtech.filepipeline.api.source;

import net.devtech.filepipeline.impl.InternalVirtualSource;
import net.devtech.filepipeline.impl.util.FPInternal;
import net.devtech.filepipeline.impl.util.ReadOnlySourceException;

/**
 * The root sink, after all files are processed within this sink, it must be closed
 * <code>
 *     try(ProcessRoot root = ...; ProcessSink sink = ProcessSink.fileSystem()) {
 *         sink.copy(root.resolve("test"), sink.outputFile("test"));
 *     }
 * </code>
 */
public interface ProcessSink extends VirtualSink, AutoCloseable {
	// todo custom ProcessSink from VirtualSink to close early

	static ProcessSink primaryDrive() {
		try {
			return (ProcessSink) ((InternalVirtualSource)ProcessRoot.primaryDrive()).createSink();
		} catch(ReadOnlySourceException e) {
			throw FPInternal.rethrow(e);
		}
	}

	static ProcessSink workingDirectory() {
		try {
			return (ProcessSink) ((InternalVirtualSource)ProcessRoot.workingDirectory()).createSink();
		} catch(ReadOnlySourceException e) {
			throw FPInternal.rethrow(e);
		}
	}

	static ProcessSink ofFilePath(String path) {
		try {
			return (ProcessSink) ((InternalVirtualSource)ProcessRoot.ofFilePath(path)).createSink();
		} catch(ReadOnlySourceException e) {
			throw FPInternal.rethrow(e);
		}
	}
}
