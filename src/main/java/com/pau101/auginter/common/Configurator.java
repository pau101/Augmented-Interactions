package com.pau101.auginter.common;

import com.pau101.auginter.AugmentedInteractions;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public abstract class Configurator {
	private final Configuration config;

	public Configurator(Configuration config) {
		this.config = config;
		MinecraftForge.EVENT_BUS.register(this);
	}

	public final Configuration getConfig() {
		return config;
	}

	@SubscribeEvent
	public void configChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (AugmentedInteractions.ID.equals(event.getModID())) {
			update();
		}
	}

	public final void update() {
		readConfig();
		save();
	}

	protected final void sync() {
		writeConfig();
		save();
	}

	protected final void save() {
		if (config.hasChanged()) {
			config.save();
		}
	}

	protected abstract void readConfig();

	protected abstract void writeConfig();
}
