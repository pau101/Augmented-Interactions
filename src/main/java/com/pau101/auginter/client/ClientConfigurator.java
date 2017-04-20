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

	public void setAllAnimationVisiblity(boolean value) {
		for (AnimationSupplier<?> anim : animationActiveStates.keySet()) {
			animationActiveStates.put(anim, value);
		}
		sync();
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

	@Override
	protected void writeConfig() {
		Configuration config = getConfig();
		for (Interaction interaction : interactionHandler.getInteractions()) {
			for (AnimationSupplier<?> anim : interaction.getAnimationSuppliers()) {
				String name = anim.getName();
				if (config.hasKey(ANIMATION_CAT, name)) {
					config.get(ANIMATION_CAT, name, true).set(isAnimationEnabled(anim));
				}
			}
		}
	}
}
