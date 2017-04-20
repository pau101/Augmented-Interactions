package com.pau101.auginter.client;

import java.io.File;

import org.lwjgl.input.Keyboard;

import com.pau101.auginter.AugmentedInteractions;
import com.pau101.auginter.client.interaction.AnimationSupplier;
import com.pau101.auginter.client.interaction.AnimationWarden;
import com.pau101.auginter.client.interaction.InteractionHandler;
import com.pau101.auginter.client.interaction.render.AnimationRenderer;
import com.pau101.auginter.common.Configurator;
import com.pau101.auginter.common.Proxy;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public final class ClientProxy extends Proxy implements AnimationWarden {
	private KeyBinding skipAnimation;

	private AnimationRenderer renderer;

	private InteractionHandler handler;

	private ClientConfigurator config;

	@Override
	public void init(File configFile) {
		super.init(configFile);
		skipAnimation = keyBinding("skipAnimation", KeyConflictContext.IN_GAME, Keyboard.KEY_X);
		renderer = new AnimationRenderer();
		handler = new InteractionHandler(renderer, this, skipAnimation);
		config = new ClientConfigurator(new Configuration(configFile), handler);
		config.update();
	}

	@Override
	public Configurator getConfigurator() {
		return config;
	}

	@Override
	public boolean rightClickMouse(EnumHand hand) {
		return handler.rightClickMouse(hand);
	}

	@Override
	public void setAllAnimationVisiblity(boolean value) {
		config.setAllAnimationVisiblity(value);
	}

	@Override
	public boolean isEnabled(AnimationSupplier<?> anim) {
		return config.isAnimationEnabled(anim);
	}

	private KeyBinding keyBinding(String name, KeyConflictContext keyContext, int keyCode) {
		KeyBinding key = new KeyBinding("key." + AugmentedInteractions.ID + "." + name, keyContext, keyCode, "key.categories." + AugmentedInteractions.ID);
		ClientRegistry.registerKeyBinding(key);
		return key;
	}
}
