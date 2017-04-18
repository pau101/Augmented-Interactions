package com.pau101.auginter.client.interaction.animation;

import java.util.function.Predicate;

import com.pau101.auginter.client.interaction.action.Action;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public abstract class AnimationConsumed<D> extends AnimationDurated<D> {
	public AnimationConsumed(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Predicate<ItemStack> itemPredicate, Action<D> action) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate, action);
	}

	@Override
	protected final int getActionTick(Minecraft mc, World world, EntityPlayer player) {
		return getDuration() - (player.capabilities.isCreativeMode ? 1 : 4);
	}

	@Override
	protected final boolean shouldReverseTransform() {
		return false;
	}
}
