package com.andedit.viewmc.maker;

import com.andedit.viewmc.MakerCore;
import com.badlogic.gdx.utils.Disposable;

/** The base class for gif and video makers. */
public abstract class Maker implements Disposable {
	
	protected final MakerCore core;
	
	public Maker(MakerCore core) {
		this.core = core;
	}
	
	/** @return true if it finished. */
	public abstract boolean update();
}
