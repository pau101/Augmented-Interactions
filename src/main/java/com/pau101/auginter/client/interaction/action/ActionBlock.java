package com.pau101.auginter.client.interaction.action;

import com.pau101.auginter.client.interaction.action.ActionBlock.Data;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class ActionBlock implements Action<Data> {
	@Override
	public boolean perform(Minecraft mc, Data dat) {
		BlockPos pos = dat.mouseOver.getBlockPos();
		if (mc.world.getBlockState(pos).getMaterial() != Material.AIR) {
			int count = dat.stack.getCount();
			EnumActionResult result = mc.playerController.processRightClickBlock(mc.player, mc.world, pos, dat.mouseOver.sideHit, dat.mouseOver.hitVec, dat.hand);
			if (result == EnumActionResult.SUCCESS) {
				if (!dat.stack.isEmpty() && (dat.stack.getCount() != count || mc.playerController.isInCreativeMode())) {
					mc.entityRenderer.itemRenderer.resetEquippedProgress(dat.hand);
				}
				return true;
			}
		}
		return false;
	}

	public static final class Data {
		private final RayTraceResult mouseOver;

		private final ItemStack stack;

		private final EnumHand hand;

		public Data(RayTraceResult mouseOver, ItemStack stack, EnumHand hand) {
			this.mouseOver = mouseOver;
			this.stack = stack;
			this.hand = hand;
		}
	}
}
