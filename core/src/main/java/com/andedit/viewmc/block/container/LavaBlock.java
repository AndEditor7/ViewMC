package com.andedit.viewmc.block.container;

import com.andedit.viewmc.block.BlockModel;
import com.andedit.viewmc.block.BlockState;
import com.andedit.viewmc.block.BlockModel.Quad;
import com.andedit.viewmc.util.Cull;
import com.andedit.viewmc.util.Facing;
import com.andedit.viewmc.world.Section;

public class LavaBlock extends WaterBlock {
	
	@Override
	protected BlockModel newBlockModel() {
		var model = new BlockModel();
		model.ao = false;
		return model;
	}
	
	@Override
	protected void quad(Quad quad) {
		quad.allowRender = true;
	}
	
	@Override
	protected String getTexture() {
		return "block/lava_still";
	}
	
	@Override
	public boolean canRender(BlockState primary, BlockState secondary, Quad quad, Facing face, Cull cull, int x, int y, int z) {
		if (primary.isOf(secondary.block)) {
			return false;
		}
		return cull.isRenderable();
	}
	
	@Override
	public boolean isWaterLogged(BlockState state) {
		return false;
	}
	
	@Override
	protected BlockModel getModel(Section section, int x, int y, int z) {
		return section.getBlockStateAt(x, y+1, z).isOf(this) ? fullModel : haftModel;
	}
}
