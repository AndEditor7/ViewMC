package com.andedit.viewmc.util;

@FunctionalInterface
public interface FloatFunction<R> {
	R apply(float value);
}
