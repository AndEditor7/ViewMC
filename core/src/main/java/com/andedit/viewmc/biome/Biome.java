package com.andedit.viewmc.biome;

import com.badlogic.gdx.math.MathUtils;

public class Biome {
	
	public final float temperature, downfall;
	public final int skyColor, color;

	public Biome(float temperature, float downfall, int skyColor) {
		this(temperature, downfall, skyColor, 0);
	}

	/** @param color overrides the colors of the grass and foliage */
	public Biome(float temperature, float downfall, int skyColor, int color) {
		this.temperature = MathUtils.clamp(temperature, 0.0f, 1.0f);
		this.downfall = MathUtils.clamp(downfall, 0.0f, 1.0f);
		this.skyColor = skyColor;
		this.color = color;
	}

	public boolean isColorOverride() {
		return color != 0;
	}
}
