package com.andedit.viewmc.util;

import java.util.concurrent.Callable;

public interface LoaderTask<T> extends Callable<T> {
	
	default String getStatus() {
		return "";
	}
	
	default float getProgress() {
		return 0;
	}
}
