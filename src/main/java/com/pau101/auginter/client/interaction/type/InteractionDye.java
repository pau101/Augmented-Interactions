package com.pau101.auginter.client.interaction.type;

import com.pau101.auginter.client.interaction.AnimationSupplier;
import com.pau101.auginter.client.interaction.InitiationResult;
import com.pau101.auginter.client.interaction.Interaction;
import com.pau101.auginter.client.interaction.animation.Animation;
import com.pau101.auginter.client.interaction.animation.type.AnimationDye;
import com.pau101.auginter.client.interaction.item.ItemPredicate;

import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public final class InteractionDye implements Interaction, AnimationSupplier<Void> {
	private final ItemPredicate dye = s -> s.getItem() == Items.DYE;

	@Override
	public InitiationResult<?> applies(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
		if (dye.test(stack) && mouseOver.entityHit instanceof EntitySheep) {
			EntitySheep sheep = (EntitySheep) mouseOver.entityHit;
			if (!sheep.getSheared() && EnumDyeColor.byDyeDamage(stack.getMetadata()) != sheep.getFleeceColor()) {
				return InitiationResult.success(this); 
			}
		}
		return InitiationResult.fail();
	}

	@Override
	public Animation create(World world, EntityPlayer player, ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Void data) {
		return new AnimationDye(stack, actionBarSlot, hand, mouseOver, dye);
	}
}
