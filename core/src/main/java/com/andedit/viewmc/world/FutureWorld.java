package com.andedit.viewmc.world;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FutureWorld implements Future<World> {
	
	private final WorldLoader loader;
	
	public FutureWorld(WorldLoader loader) {
		this.loader = loader;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return loader.update();
	}

	@Override
	public World get() throws ExecutionException, RuntimeException {
		if (loader.world == null) throw new RuntimeException();
		return loader.world;
	}

	@Override
	public World get(long timeout, TimeUnit unit) throws ExecutionException {
		return get();
	}

}
