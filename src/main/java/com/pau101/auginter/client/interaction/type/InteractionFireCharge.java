package com.pau101.auginter.client.interaction.type;

import com.pau101.auginter.client.interaction.AnimationSupplier;
import com.pau101.auginter.client.interaction.InitiationResult;
import com.pau101.auginter.client.interaction.Interaction;
import com.pau101.auginter.client.interaction.animation.Animation;
import com.pau101.auginter.client.interaction.animation.type.AnimationFireCharge;
import com.pau101.auginter.client.interaction.item.ItemPredicate;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public final class InteractionFireCharge implements Interaction, AnimationSupplier<Void> {
	private final ItemPredicate fireCharge = s -> s.getItem() == Items.FIRE_CHARGE;

	@Override
	public InitiationResult<?> applies(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
		if (fireCharge.test(stack)) {
			BlockPos pos = mouseOver.getBlockPos().offset(mouseOver.sideHit);
			if (world.getBlockState(pos).getMaterial() == Material.AIR) {
				return InitiationResult.success(this);
			}
		}
		return InitiationResult.fail();
	}

	@Override
	public Animation create(World world, EntityPlayer player, ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Void data) {
		return new AnimationFireCharge(stack, actionBarSlot, hand, mouseOver, fireCharge);
	}
}
