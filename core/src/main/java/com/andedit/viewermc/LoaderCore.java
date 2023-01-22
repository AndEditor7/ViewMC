package com.andedit.viewermc;

import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LoaderCore<T> implements Screen {
	
	private final Supplier<Future<T>> supplier;
	private final Consumer<T> consumer;
	private Future<T> future;
	private boolean isDone;
	
	public LoaderCore(Supplier<Future<T>> supplier, Consumer<T> consumer) {
		this.supplier = supplier;
		this.consumer = consumer;
	}

	@Override
	public void show() {
		future = supplier.get();
	}

	@Override
	public void render() {
		if (isDone) return;
		if (future.isDone()) {
			try {
				consumer.accept(future.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
			isDone = true;
		}
	}

	@Override
	public void resize(int width, int height) {
		
	}
}
