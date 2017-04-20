package com.pau101.auginter.server;

import java.io.File;

import com.pau101.auginter.common.Configurator;
import com.pau101.auginter.common.Proxy;

import net.minecraft.util.EnumHand;
import net.minecraftforge.common.config.Configuration;

public class ServerProxy extends Proxy {
	private ServerConfigurator config;

	public void init(File configFile) {
		super.init(configFile);
		config = new ServerConfigurator(new Configuration(configFile));
		config.update();
	}

	@Override
	public Configurator getConfigurator() {
		return config;
	}

	public boolean rightClickMouse(EnumHand hand) {
		return false;
	}
}
