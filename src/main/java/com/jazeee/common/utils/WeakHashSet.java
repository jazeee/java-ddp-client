package com.jazeee.common.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakHashSet<E> implements Set<E> {
	private final Set<E> set;

	public WeakHashSet() {
		this.set = Collections.newSetFromMap(new WeakHashMap<E, Boolean>());
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object element) {
		return set.contains(element);
	}

	@Override
	public Iterator<E> iterator() {
		return set.iterator();
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] sourceArray) {
		return set.toArray(sourceArray);
	}

	@Override
	public boolean add(E element) {
		return set.add(element);
	}

	@Override
	public boolean remove(Object element) {
		return set.remove(element);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return set.containsAll(collection);
	}

	@Override
	public boolean addAll(Collection<? extends E> collection) {
		return set.addAll(collection);
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		return set.retainAll(collection);
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		return set.removeAll(collection);
	}

	@Override
	public void clear() {
		set.clear();
	}

}
