package com.pau101.auginter.client.interaction;

import com.pau101.auginter.client.interaction.animation.Animation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public interface Interaction<T> {
	InitiationResult<T> applies(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver);

	default boolean allowBlockActivation(World world, BlockPos pos, IBlockState state) {
		return true;
	}

	Animation create(World world, EntityPlayer player, ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, T data);
}