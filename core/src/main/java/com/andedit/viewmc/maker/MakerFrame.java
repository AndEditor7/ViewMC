package com.andedit.viewmc.maker;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;

public interface MakerFrame extends Disposable {
	
	void begin();
	
	void end();
	
	int getInWidth();
	
	int getInHeight();
	
	int getOutWidth();
	
	int getOutHeight();
	
	/** Read framebuffer to pixmap */
	Pixmap read();
	
	/** Create pixmap from framebuffer */
	Pixmap create();
}
