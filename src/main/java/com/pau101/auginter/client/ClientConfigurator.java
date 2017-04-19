package com.pau101.auginter.client;

import java.util.HashMap;
import java.util.Map;

import com.pau101.auginter.client.interaction.AnimationSupplier;
import com.pau101.auginter.client.interaction.Interaction;
import com.pau101.auginter.client.interaction.InteractionHandler;
import com.pau101.auginter.common.Configurator;

import net.minecraftforge.common.config.Configuration;

public final class ClientConfigurator extends Configurator {
	public static final String ANIMATION_CAT = "animation";

	private static final String ANIMATION_DESC = "If this interaction animation should play";

	private final InteractionHandler interactionHandler;

	private final Map<AnimationSupplier<?>, Boolean> animationActiveStates = new HashMap<>();

	public ClientConfigurator(Configuration config, InteractionHandler interactionHandler) {
		super(config);
		this.interactionHandler = interactionHandler;
	}

	public boolean isAnimationEnabled(AnimationSupplier<?> anim) {
		return animationActiveStates.getOrDefault(anim, true);
	}

	@Override
	public void readConfig() {
		Configuration config = getConfig();
		for (Interaction interaction : interactionHandler.getInteractions()) {
			for (AnimationSupplier<?> anim : interaction.getAnimationSuppliers()) {
				boolean state = config.getBoolean(anim.getName(), ANIMATION_CAT, true, ANIMATION_DESC);
				animationActiveStates.put(anim, state);
			}
		}
	}
}
