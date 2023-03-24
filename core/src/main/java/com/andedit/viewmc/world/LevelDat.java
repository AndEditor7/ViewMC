package com.andedit.viewmc.world;

import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.NumberTag;

public class LevelDat {
	
	public final PlayerDat player;
	
	public LevelDat(NamedTag namedTag) {
		if (namedTag.getTag() instanceof CompoundTag tag) {
			var data = tag.getCompoundTag("Data");
			player = new PlayerDat(data.getCompoundTag("Player"));
		} else {
			throw new IllegalStateException();
		}
	}
	
	public static class PlayerDat {
		
		public final double xPos, yPos, zPos;
		public final float pitch, yaw;
		
		public PlayerDat(CompoundTag tag) {
			var list = tag.getListTag("Pos");
			xPos = ((NumberTag<?>)list.get(0)).asDouble();
			yPos = ((NumberTag<?>)list.get(1)).asDouble();
			zPos = ((NumberTag<?>)list.get(2)).asDouble();
			list = tag.getListTag("Rotation");
			yaw = ((NumberTag<?>)list.get(0)).asFloat();
			pitch = ((NumberTag<?>)list.get(1)).asFloat();
		}
	}
}
