package com.andedit.viewermc;

import static com.andedit.viewermc.Main.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

public class LoaderCore<T> implements Screen {
	
	private final LoaderTask<T> task;
	private Future<T> future;
	private boolean isDone;
	
	public LoaderCore(Function<ExecutorService, Future<T>> function, Consumer<T> consumer) {
		this(new LoaderTask<T>() {
			@Override
			public Future<T> submit(ExecutorService executor) {
				return function.apply(executor);
			}
			@Override
			public void run(T future) {
				consumer.accept(future);
			}
		});
	}
	
	
	public LoaderCore(LoaderTask<T> task) {
		this.task = task;
	}

	@Override
	public void show() {
		//future = task.submit(main.executor);
	}

	@Override
	public void render() {
		if (isDone) return;
		if (future.isDone()) {
			try {
				task.run(future.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
			isDone = true;
		}
	}

	@Override
	public void resize(int width, int height) {
		
	}
	
	public static interface LoaderTask<T> {
		Future<T> submit(ExecutorService executor);
		void run(T future); 
	}
}
