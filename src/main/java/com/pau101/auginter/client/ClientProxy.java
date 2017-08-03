package com.pau101.auginter.client;

import java.io.File;

import com.pau101.auginter.AugmentedInteractions;
import com.pau101.auginter.client.interaction.AnimationSupplier;
import com.pau101.auginter.client.interaction.AnimationWarden;
import com.pau101.auginter.client.interaction.InteractionHandler;
import com.pau101.auginter.client.interaction.render.AnimationRenderer;
import com.pau101.auginter.common.Configurator;
import com.pau101.auginter.common.Proxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;

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
		macgyverTheUseItemHook();
	}

	private void macgyverTheUseItemHook() {
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		KeyBinding useItem = settings.keyBindUseItem;
		((KeyBindingMap) ReflectionHelper.getPrivateValue(KeyBinding.class, null, "field_74514_b", "HASH")).removeKey(useItem);
		settings.keyBindUseItem = new KeyBinding(useItem.getKeyDescription(), useItem.getKeyCode(), useItem.getKeyCategory()) {
			@Override
			public boolean isPressed() {
				if (super.isPressed()) {
					for (EnumHand hand : EnumHand.values()) {
						if (handler.rightClickMouse(hand)) {
							while (super.isPressed());
							KeyBinding.setKeyBindState(getKeyCode(), false);
							return false;
						}
					}
					return true;
				}
				return false;
			}
		};
	}

	@Override
	public Configurator getConfigurator() {
		return config;
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
