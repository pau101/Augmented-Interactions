package com.pau101.auginter.client.interaction;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

public abstract class InteractionDuratedUse extends InteractionDurated {
	public InteractionDuratedUse(ItemStack used, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver) {
		super(used, actionBarSlot, hand, mouseOver);
	}

	@Override
	protected final boolean use() {
		Minecraft mc = Minecraft.getMinecraft();
		return mc.playerController.processRightClick(mc.player, mc.world, hand) == EnumActionResult.SUCCESS;
	}
}
