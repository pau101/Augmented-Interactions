package com.pau101.auginter.client.gui;

import java.util.List;

import com.pau101.auginter.AugmentedInteractions;
import com.pau101.auginter.client.ClientConfigurator;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GuiConfigAugInter extends GuiConfig {
	public GuiConfigAugInter(GuiScreen parentScreen) {
		super(parentScreen, getConfigElements(), AugmentedInteractions.ID, false, false, I18n.format("auginter.config"));
	}

	private static List<IConfigElement> getConfigElements() {
		return new ConfigElement(AugmentedInteractions.getConfigurator().getConfig().getCategory(ClientConfigurator.ANIMATION_CAT)).getChildElements();
	}
}
