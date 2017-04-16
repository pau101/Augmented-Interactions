package com.pau101.auginter.client.interaction;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public abstract class InteractionDuratedBlock extends InteractionDurated {
	public InteractionDuratedBlock(ItemStack used, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver) {
		super(used, actionBarSlot, hand, mouseOver);
	}

	@Override
	protected final void use() {
		Minecraft mc = Minecraft.getMinecraft();
		BlockPos blockpos = mouseOver.getBlockPos();
		if (mc.world.getBlockState(blockpos).getMaterial() != Material.AIR) {
			int stackcount = stack.func_190916_E();
			EnumActionResult result = mc.playerController.processRightClickBlock(mc.player, mc.world, blockpos, mouseOver.sideHit, mouseOver.hitVec, hand);
			if (result == EnumActionResult.SUCCESS) {
				mc.player.swingArm(hand);
				if (!stack.func_190926_b() && (stack.func_190916_E() != stackcount || mc.playerController.isInCreativeMode())) {
					mc.entityRenderer.itemRenderer.resetEquippedProgress(hand);
				}
			}
		}
	}
}
