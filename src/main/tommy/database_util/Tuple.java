package database_util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Tuple {

	public static Tuple ofArray(final Object[] objs) {
		return new Tuple(objs);
	}

	private final Object[] objs;

	private Tuple(final Object[] objs) {
		this.objs = objs;
	}

	public Object get(final int index) {
		return this.objs[index];
	}

	public <T> T get(final int index, final Class<T> clazz) {
		return clazz.cast(this.objs[index]);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.objs);
	}

	public <T> void forEach(Consumer<T> looper) {
		for (Object obj : objs) {
			T t = (T) obj;
			looper.accept(t);
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Tuple)) {
			return false;
		}
		final Tuple t = (Tuple) obj;
		if (this.objs.length != t.objs.length) {
			return false;
		}
		for (int i = 0; i < this.objs.length; i++) {
			if (!Objects.equals(this.objs[i], t.objs[i])) {
				return false;
			}
		}
		return true;

	}

	@Override
	public String toString() {
		return Arrays.stream(this.objs).map(s -> s == null ? "" : s.toString()).collect(Collectors.joining(", "));
	}

	int size() {
		return objs.length;
	}

}
