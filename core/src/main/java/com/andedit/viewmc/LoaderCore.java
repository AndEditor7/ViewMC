package com.andedit.viewmc;

import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.badlogic.gdx.utils.viewport.Viewport;

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
		if (future == null) { 
			future = supplier.get();
		}
	}
	
	@Override
	public void hide() {
		
	}
	
	@Override
	public void event(Events event, Object obj) {
		
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
	public void resize(Viewport view) {
		
	}

	
}
