package com.andedit.viewmc.ui;

import static com.andedit.viewmc.Main.main;
import static com.badlogic.gdx.Gdx.graphics;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.andedit.viewmc.Assets;
import com.andedit.viewmc.GameCore;
import com.andedit.viewmc.MenuCore;
import com.andedit.viewmc.Statics;
import com.andedit.viewmc.resource.ResourcePacker;
import com.andedit.viewmc.resource.Resources;
import com.andedit.viewmc.ui.actor.ProgressBar;
import com.andedit.viewmc.ui.util.BaseUI;
import com.andedit.viewmc.util.LoaderTask;
import com.andedit.viewmc.world.FutureWorld;
import com.andedit.viewmc.world.World;
import com.andedit.viewmc.world.WorldLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LoadingUI<T> extends BaseUI {
	
	private static final float TIMEOUT = 10;
	
	private final LoaderTask<T> task;
	private final Future<T> future;
	private final Consumer<Future<T>> consumer;
	
	private final Label status = new Label(null, Assets.skin);
	private final ProgressBar bar = new ProgressBar();
	
	private float time, lastProgress;
	
	public static LoadingUI<Resources> of(ResourcePacker packer, MenuCore core) {
		return new LoadingUI<>(Statics.meshExe, packer.getResourceLoader(), future -> {
			try {
				var resources = future.get();
				main.setResources(resources);
			} catch (Exception e) {
				e.getCause().printStackTrace();
				core.manager.setUI(new ResourcesUI(core), true);
				return;
			}
			core.manager.setUI(new MenuUI(core), true);
		});
	}
	
	public static LoadingUI<World> of(Resources resources, FileHandle worldFolder, MenuCore core) {
		var loader = new WorldLoader(resources, worldFolder);
		return new LoadingUI<>(new FutureWorld(loader), loader, future -> {
			try {
				var world = future.get();
				main.setScreen(new GameCore(resources, world));
			} catch (Exception e) {
				if (e.getCause() != null) {
					e.getCause().printStackTrace();
				}
			}
			core.manager.setUI(new DropWorldUI(core), true);
		});
	}
	
	public LoadingUI(ExecutorService executor, LoaderTask<T> task, Consumer<Future<T>> consumer) {
		this(executor.submit(task), task, consumer);
	}
	
	public LoadingUI(Future<T> future, LoaderTask<T> task, Consumer<Future<T>> consumer) {
		this.task = task;
		this.future = future;
		this.consumer = consumer;
		
		var group = add(new Group());
		group.setUserObject(new Vector2(0.5f, 0.5f));
		
		bar.setPosition(0, -10, Align.center);
		group.addActor(bar);
		
		status.setAlignment(Align.center);
		status.setPosition(0, 10, Align.center);
		group.addActor(status);
	}
	
	@Override
	public void update() {
		if (future.isDone()) {
			consumer.accept(future);
		}
		
		if (lastProgress != task.getProgress()) {
			lastProgress = task.getProgress();
			time = 0;
		} else {
			time += graphics.getDeltaTime();
			if (time > TIMEOUT) {
				//future.cancel(true);
				//consumer.accept(CompletableFuture.failedFuture(new TimeoutException()));
			}
		}
		
		status.setText(task.getStatus());
		bar.setProgress(task.getProgress());
	}
	
	@Override
	public void resize(Viewport view) {
		
	}
}
