package net.devtech.filepipeline.impl.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

public class FPInternal {
	/**
	 * @return nothing, because it throws
	 * @throws T rethrows {@code throwable}
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Throwable> RuntimeException rethrow(Throwable throwable) throws T {
		throw (T) throwable;
	}

	public static ByteBuffer read(ReadableByteChannel stream) {
		ByteBuffer curr = ByteBuffer.allocate(4096);
		try {
			while(stream.read(curr) != -1) {
				if(curr.remaining() == 0) { // if buffer is full
					int lim = curr.limit();
					ByteBuffer clone = ByteBuffer.allocate(lim * 2);
					clone.put(0, curr, 0, lim);
					clone.position(curr.position());
					curr = clone;
				}
			}
		} catch(IOException e) {
			throw rethrow(e);
		}
		curr.limit(curr.limit() - curr.remaining());
		curr.rewind();
		return curr;
	}

	public static ByteBuffer read(Path p) {
		try(SeekableByteChannel stream = Files.newByteChannel(p)) {
			return read(stream);
		} catch(IOException e) {
			throw rethrow(e);
		}
	}



	/**
	 * Returns a {@code Spliterator} over the elements of {@code fromSpliterator} mapped by {@code function}. This only really exists for paralell streams
	 */
	public static <I, O> Spliterator<O> map(Spliterator<I> split, Function<? super I, ? extends O> func, Runnable exitFunction) {
		return new MappedSpliterator<>(split, func, exitFunction);
	}

	private static class MappedSpliterator<O, I> implements Spliterator<O> {
		private final Spliterator<I> split;
		private final Function<? super I, ? extends O> func;
		private final Object next;
		private int count;

		public MappedSpliterator(Spliterator<I> split, Function<? super I, ? extends O> func, Object next) {
			this.split = split;
			this.func = func;
			this.next = next;
		}

		private void end() {
			Object next = this.next;
			if(this.count-- == 0) {
				if(next instanceof Runnable r) {
					r.run();
				} else if(next instanceof MappedSpliterator s) {
					s.end();
				}
			}
		}

		@Override
		public boolean tryAdvance(Consumer<? super O> action) {
			boolean b = split.tryAdvance(fromElement -> action.accept(func.apply(fromElement)));
			if(!b) {
				this.end();
			}
			return b;
		}

		@Override
		public void forEachRemaining(Consumer<? super O> action) {
			split.forEachRemaining(fromElement -> action.accept(func.apply(fromElement)));
			this.end();
		}

		@Override
		public Spliterator<O> trySplit() {
			Spliterator<I> fromSplit = split.trySplit();
			if(fromSplit != null) {
				this.count++;
				return new MappedSpliterator<>(fromSplit, func, this);
			}
			return null;
		}

		@Override
		public long estimateSize() {
			return split.estimateSize();
		}

		@Override
		public int characteristics() {
			return split.characteristics() & ~(Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED);
		}
	}
}
