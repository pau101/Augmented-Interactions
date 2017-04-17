package com.pau101.auginter.client.interaction.type;

import com.pau101.auginter.client.interaction.InitiationResult;
import com.pau101.auginter.client.interaction.Interaction;
import com.pau101.auginter.client.interaction.animation.Animation;
import com.pau101.auginter.client.interaction.animation.type.AnimationFlintAndSteel;
import com.pau101.auginter.client.interaction.item.ItemPredicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class InteractionFlintAndSteel implements Interaction<Void> {
	private final ItemPredicate flintAndSteel = s -> s.getItem() == Items.FLINT_AND_STEEL;

	@Override
	public InitiationResult<Void> applies(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
		return InitiationResult.result(this, flintAndSteel.test(stack));
	}

	@Override
	public Animation create(World world, EntityPlayer player, ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Void data) {
		return new AnimationFlintAndSteel(stack, actionBarSlot, hand, mouseOver, flintAndSteel);
	}
}
