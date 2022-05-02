package net.devtech.filepipeline.impl.multi;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

public record IterableIterable<A, B>(Iterable<A> iterable, Function<A, Iterable<B>> mapper) implements Iterable<B> {
	@NotNull
	@Override
	public Iterator<B> iterator() {
		return new Iterator<>() {
			final Iterator<A> source = IterableIterable.this.iterable.iterator();
			Iterator<B> current;

			@Override
			public boolean hasNext() {
				return this.updateCurrent() == null;
			}

			@Override
			public B next() {
				return Objects.requireNonNull(this.updateCurrent(), "hasNext is false!").next();
			}

			private Iterator<B> updateCurrent() {
				Iterator<B> curr = this.current;
				while(!(curr != null && curr.hasNext())) {
					if(this.source.hasNext()) {
						curr = IterableIterable.this.mapper.apply(this.source.next()).iterator();
					} else {
						return null;
					}
				}
				return curr;
			}
		};
	}
}
