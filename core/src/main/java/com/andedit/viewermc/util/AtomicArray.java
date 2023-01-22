package com.andedit.viewermc.util;

import java.util.concurrent.atomic.AtomicReferenceArray;

public final class AtomicArray<T> {

	private volatile AtomicReferenceArray<T> items;
	private volatile int size;
	
	public AtomicArray() {
		this(16);
	}
	
	public AtomicArray(int size) {
		items = new AtomicReferenceArray<>(size);
	}
	
	public T get(int i) {
		return items.get(i);
	}
	
	public void add(T item) {
		if (size == items.length()) resize(Math.max(8, (int)(size * 1.75f)));
		items.set(size++, item);
		
	}
	
	public void remove(int i) {
		if (i >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + i + " >= " + size);
		items.set(i, items.getAndSet(--size, null));
	}
	
	private void resize(int newSize) {
		var newItems = new AtomicReferenceArray<T>(newSize);
		for (int i = 0, s = Math.min(size, newSize); i < s; i++) {
			newItems.set(i, items.get(i));
		}
		this.items = newItems;
	}
	
	public int size() {
		return size;
	}
}
