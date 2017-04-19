package com.pau101.auginter.client.interaction.type;

import com.google.common.collect.ImmutableList;
import com.pau101.auginter.client.interaction.AnimationSupplier;
import com.pau101.auginter.client.interaction.InitiationResult;
import com.pau101.auginter.client.interaction.Interaction;
import com.pau101.auginter.client.interaction.animation.Animation;
import com.pau101.auginter.client.interaction.animation.type.AnimationShears;
import com.pau101.auginter.client.interaction.item.ItemPredicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

public final class InteractionShears implements Interaction, AnimationSupplier<Void> {
	private final ItemPredicate shears = s -> s.getItem() == Items.SHEARS; 

	@Override
	public InitiationResult<Void> applies(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
		Entity entity = mouseOver.entityHit;
		if (shears.test(stack) && entity instanceof IShearable && entity instanceof EntityLivingBase) {
			if (((IShearable) entity).isShearable(stack, entity.world, new BlockPos(entity))) {
				return InitiationResult.success(this);
			}
		}
		return InitiationResult.fail();
	}

	@Override
	public ImmutableList<AnimationSupplier<?>> getAnimationSuppliers() {
		return ImmutableList.of(this);
	}

	@Override
	public String getName() {
		return "Shear Sheep";
	}

	@Override
	public Animation create(World world, EntityPlayer player, ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Void data) {
		return new AnimationShears(stack, actionBarSlot, hand, mouseOver, shears);
	}
}
