package com.andedit.viewmc.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

public class ResourcePacker implements Disposable {
	public @Null PackSection corePack;
	public @Null ModSection coreMod;
	
	public final ArrayList<PackSection> availableRes = new ArrayList<>();
	public final ArrayList<PackSection> selectedRes = new ArrayList<>();
	public final ArrayList<ModSection> modRes = new ArrayList<>();
	
	// memory
	private final ArrayList<PackSection> lastAvailableRes = new ArrayList<>();
	private final ArrayList<PackSection> lastSelectedRes = new ArrayList<>();
	private final ArrayList<ModSection> lastModRes = new ArrayList<>();
	private @Null PackSection lastCorePack;
	
	public boolean isPackDirty, isModDirty, isStarted;
	
	public void start() {
		if (isStarted) return;
		
		lastAvailableRes.clear();
		lastSelectedRes.clear();
		lastModRes.clear();
		
		for (var res : availableRes) {
			lastAvailableRes.add(new PackSection(res));
		}
		for (var res : selectedRes) {
			lastSelectedRes.add(new PackSection(res));
		}
		for (var res : modRes) {
			lastModRes.add(new ModSection(res));
		}
		
		isStarted = true;
	}
	
	public boolean hasChanged() {
		return !Objects.equals(corePack, lastCorePack) || !selectedRes.equals(lastSelectedRes) || !modRes.equals(lastModRes);
	}
	
	public void cancel() {
		if (!isStarted) return;
			
		availableRes.clear();
		availableRes.addAll(lastAvailableRes);
		selectedRes.clear();
		selectedRes.addAll(lastSelectedRes);
		modRes.clear();
		modRes.addAll(lastModRes);
		
		isPackDirty = true;
		isModDirty = true;
		isStarted = false;
	}
	
	/** create a new resource loader. */
	public ResourceLoader getResourceLoader() {
		var list = new ArrayList<ResourceData>();
		list.add(corePack.data);
		modRes.stream().filter(ModSection::filter).forEachOrdered(r->list.add(r.data));;
		selectedRes.forEach(r->list.add(r.data));;
		return new ResourceLoader(list);
	}
	
	public void move(PackSection section) {
		if (availableRes.contains(section)) {
			selectedRes.add(section);
			availableRes.remove(section);
			isPackDirty = true;
		} else if (selectedRes.contains(section)) {
			availableRes.add(section);
			selectedRes.remove(section);
			isPackDirty = true;
		}
	}
	
	public void swap(PackSection section, boolean up) {
		int index = selectedRes.indexOf(section);
		int to = index + (up?1:-1);
		if (to < 0 || to >= selectedRes.size()) return; 
		Collections.swap(selectedRes, index, to);
		isPackDirty = true;
	}
	
	public boolean contains(FileHandle file) {
		for (var res : availableRes) {
			if (res.equals(file)) {
				return true;
			}
		}
		for (var res : selectedRes) {
			if (res.equals(file)) {
				return true;
			}
		}
		for (var res : modRes) {
			if (res.equals(file)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasCore() {
		return corePack != null;
	}
	
	public void addCore(FileHandle file) throws Exception {
		if (!file.name().endsWith(".jar")) return;
		removeCore();
		var core = new Core(file);
		corePack = new PackSection(core);
		coreMod = new ModSection(core);
	}
	
	public void removeCore() {
		if (corePack != null) {
			corePack.dispose();
			corePack = null;
			coreMod = null;
		}
	}
	
	public void add(FileHandle file) throws Exception {
		if (contains(file)) return;
		var name = file.name();
		if (name.endsWith(".zip")) {
			var pack = new PackSection(new Resource(file));
			availableRes.add(pack);
			lastAvailableRes.add(pack);
			isPackDirty = true;
		}
		if (name.endsWith(".jar")) {
			modRes.add(new ModSection(new Mod(file)));
			isModDirty = true;
		}
	}
	
	public static class ModSection implements ResourceSection {
		public final ModData data;
		public boolean isDisabled;
		
		private ModSection(ModSection mod) {
			this(mod.data);
			isDisabled = mod.isDisabled;
		}
		
		private ModSection(ModData data) {
			this.data = data;
		}
		
		@Override
		public ResourceData data() {
			return data;
		}
		
		@Override
		public boolean isPack() {
			return false;
		}
		
		boolean filter() {
			return !isDisabled;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ModSection mod) {
				return mod.isDisabled == isDisabled && mod.data.equals(data);
			}
			return false;
		}
	}
	
	public static class PackSection implements ResourceSection {
		public final ResourceData data;
		
		private PackSection(PackSection pack) {
			this(pack.data);
		}
		
		private PackSection(ResourceData data) {
			this.data = data;
		}

		@Override
		public ResourceData data() {
			return data;
		}
		
		@Override
		public boolean isPack() {
			return true;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PackSection mod) {
				return mod.data.equals(data);
			}
			return false;
		}
	}

	@Override
	public void dispose() {
		if (corePack != null) corePack.dispose();
		availableRes.forEach(Disposable::dispose);
		selectedRes.forEach(Disposable::dispose);
		modRes.forEach(Disposable::dispose);
	}
}
