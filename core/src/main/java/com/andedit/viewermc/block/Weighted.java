package com.andedit.viewermc.block;

import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class Weighted<T> implements IntFunction<T> {
	private final Array<T> array = new Array<T>();
    
    public Weighted<T> add(T result) {
    	return add(((IntSupplier)result).getAsInt(), result);
    }

    public Weighted<T> add(int weight, T result) {
        for (int i = 0; i < weight; i++) {
        	array.add(result);
        }
        return this;
    }

    @Null
	@Override
	public T apply(int value) {
    	return array.get(Math.abs(value % array.size));
	}
}
