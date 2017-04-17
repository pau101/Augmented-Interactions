package com.pau101.auginter.client.interaction.action;

import com.pau101.auginter.client.interaction.action.ActionEntity.Data;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

public final class ActionEntity implements Action<Data> {
	@Override
	public boolean perform(Minecraft mc, Data dat) {
		EntityPlayer player = mc.player;
		if (mc.playerController.interactWithEntity(player, dat.mouseOver.entityHit, dat.mouseOver, dat.hand) != EnumActionResult.SUCCESS) {
			return mc.playerController.interactWithEntity(player, dat.mouseOver.entityHit, dat.hand) == EnumActionResult.SUCCESS;
		}
		return false;
	}

	public static final class Data {
		private final RayTraceResult mouseOver;

		private final EnumHand hand;

		public Data(RayTraceResult mouseOver, EnumHand hand) {
			this.mouseOver = mouseOver;
			this.hand = hand;
		}
	}
}
