package com.pau101.auginter.client;

import com.pau101.auginter.client.interaction.InteractionRenderer;
import com.pau101.auginter.server.ServerProxy;

import net.minecraft.util.EnumHand;

public class ClientProxy extends ServerProxy {
	private InteractionRenderer renderer;

	@Override
	public void init() {
		super.init();
		renderer = new InteractionRenderer();
	}

	@Override
	public boolean rightClickMouse(EnumHand hand) {
		return renderer.rightClickMouse(hand);
	}
}
