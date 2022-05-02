package net.devtech.filepipeline.impl;

import net.devtech.filepipeline.api.source.VirtualSink;
import net.devtech.filepipeline.impl.util.ReadOnlySourceException;
import org.jetbrains.annotations.ApiStatus;

public interface InternalVirtualSource {
	@ApiStatus.Internal
	ClosableVirtualRoot getClosable();

	// todo get sink to avoid duplication

	VirtualSink createSink() throws ReadOnlySourceException;
}
