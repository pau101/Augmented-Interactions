package com.pau101.auginter.client.interaction.action;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;

public final class ActionUse implements Action<EnumHand> {
	@Override
	public boolean perform(Minecraft mc, EnumHand hand) {
		return mc.playerController.processRightClick(mc.player, mc.world, hand) == EnumActionResult.SUCCESS;
	}
}
