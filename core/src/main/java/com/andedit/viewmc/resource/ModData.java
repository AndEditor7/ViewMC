package com.andedit.viewmc.resource;

import java.util.List;
import java.util.Optional;

import com.andedit.viewmc.loader.ModType;
import com.andedit.viewmc.util.Pair;
import com.badlogic.gdx.graphics.Texture;

public interface ModData extends ResourceData {
	
	Optional<Texture> getLogo();
	String getVersion();
	List<String> getLicense();
	List<String> getAuthors();
	List<String> getContributors();
	/** The first pair is a name, and the second pair is a list of roles. */
	List<Pair<String, List<String>>> getQuiltContributors();
	ModType getType();
	
	@Override
	default boolean isResource() {
		return false;
	}
	
	@Override
	default boolean isMod() {
		return true;
	}
	
	@Override
	default void dispose() {
		ResourceData.super.dispose();
		getLogo().ifPresent(Texture::dispose);
	}
}
