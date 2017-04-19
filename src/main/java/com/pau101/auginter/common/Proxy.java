package com.pau101.auginter.common;

import java.io.File;

import net.minecraft.util.EnumHand;

public abstract class Proxy {
	public abstract void init(File configFile);

	public abstract Configurator getConfigurator();

	public abstract boolean rightClickMouse(EnumHand hand);
}
