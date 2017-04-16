package com.pau101.auginter.client.interaction;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

public abstract class InteractionDuratedEntity extends InteractionDurated {
	public InteractionDuratedEntity(ItemStack used, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver) {
		super(used, actionBarSlot, hand, mouseOver);
	}

	@Override
	protected final void use() {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		if (mc.playerController.interactWithEntity(player, mouseOver.entityHit, mouseOver, hand) != EnumActionResult.SUCCESS) {
			mc.playerController.interactWithEntity(player, mouseOver.entityHit, hand);
		}
	}
}
