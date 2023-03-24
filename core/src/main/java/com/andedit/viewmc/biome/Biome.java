package com.andedit.viewmc.biome;

import com.badlogic.gdx.math.MathUtils;

public class Biome {
	
	public final float temperature, downfall;
	public final int skyColor;
	
	public Biome(float temperature, float downfall, int skyColor) {
		this.temperature = MathUtils.clamp(temperature, 0.0f, 1.0f);
		this.downfall = MathUtils.clamp(downfall, 0.0f, 1.0f);
		this.skyColor = skyColor;
	}
}
