package com.andedit.viewmc.biome;

import com.badlogic.gdx.math.MathUtils;

public class Biome {
	
	public final float temperature, downfall;
	public final int skyColor, waterColor, grassColor, foliageColor;

	public Biome(float temperature, float downfall, int skyColor, int waterColor) {
		this(temperature, downfall, skyColor, waterColor, 0);
	}

	/** @param color overrides the colors of the grass and foliage */
	public Biome(float temperature, float downfall, int skyColor, int waterColor, int color) {
		this(temperature, downfall, skyColor, waterColor, color, color);
	}

	public Biome(float temperature, float downfall, int skyColor, int waterColor, int grassColor, int foliageColor) {
		this.temperature = MathUtils.clamp(temperature, 0.0f, 1.0f);
		this.downfall = MathUtils.clamp(downfall, 0.0f, 1.0f);
		this.skyColor = skyColor;
		this.waterColor = waterColor == 0 ? 0x3F76E4 : waterColor;
		this.grassColor = grassColor;
		this.foliageColor = foliageColor;
	}

	public boolean isGrassColorOverride() {
		return grassColor != 0;
	}

	public boolean isFoliageColorOverride() {
		return foliageColor != 0;
	}
}
