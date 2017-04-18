package com.pau101.auginter.client.interaction.animation.type;

import java.util.function.Predicate;

import com.pau101.auginter.client.interaction.action.ActionBlock;
import com.pau101.auginter.client.interaction.action.ActionBlock.Data;
import com.pau101.auginter.client.interaction.animation.AnimationDurated;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public abstract class AnimationBottle extends AnimationDurated<ActionBlock.Data> {
	private final float fluidLevel;

	public AnimationBottle(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Predicate<ItemStack> itemPredicate, float fluidLevel) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate, new ActionBlock());
		this.fluidLevel = fluidLevel;
	}

	@Override
	protected final Data getActionData() {
		return new ActionBlock.Data(getMouseOver(), getStack(), getHand());
	}

	@Override
	protected int getActionTick(Minecraft mc, World world, EntityPlayer player) {
		return getDuration() / 2;
	}

	@Override
	protected int getDuration() {
		return 15;
	}

	protected final float getFluidLevel() {
		return fluidLevel;
	}
}
