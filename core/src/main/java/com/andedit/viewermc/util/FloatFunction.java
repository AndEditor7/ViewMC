package com.andedit.viewermc.util;

@FunctionalInterface
public interface FloatFunction<R> {
	R apply(float value);
}
