package com.andedit.viewmc.util;

import com.badlogic.gdx.utils.ObjectMap;

public class EmptyMap<K, V> extends ObjectMap<K, V> {
	
	private static final EmptyMap<?, ?> EMPTY = new EmptyMap<>();
	
	private EmptyMap() {
	}
	
	@SuppressWarnings("unchecked")
	public static <K, V> EmptyMap<K, V> instance() {
		return (EmptyMap<K, V>) EMPTY;
	}
	
	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void putAll(ObjectMap<? extends K, ? extends V> map) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean containsValue(Object value, boolean identity) {
		return false;
	}
	
	@Override
	public boolean notEmpty() {
		return false;
	}
	
	@Override
	public V remove(K key) {
		return null;
	}
	
	@Override
	public void shrink(int maximumCapacity) {
		
	}
	
	@Override
	public <T extends K> V get(T key) {
		return null;
	}
	
	@Override
	public V get(K key, V defaultValue) {
		return defaultValue;
	}
	
	@Override
	public boolean containsKey(K key) {
		return false;
	}
	
	@Override
	public void clear(int maximumCapacity) {
		
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}
	
	@Override
	public int hashCode() {
		return 239023147;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
}
