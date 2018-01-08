package com.pau101.auginter.client.interaction.type;

import com.google.common.collect.ImmutableList;
import com.pau101.auginter.client.interaction.AnimationSupplier;
import com.pau101.auginter.client.interaction.InitiationResult;
import com.pau101.auginter.client.interaction.Interaction;
import com.pau101.auginter.client.interaction.action.ActionBlock;
import com.pau101.auginter.client.interaction.animation.Animation;
import com.pau101.auginter.client.interaction.animation.type.AnimationInsertRecord;
import com.pau101.auginter.client.interaction.item.ItemPredicate;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public final class InteractionRecord implements Interaction, AnimationSupplier<Void> {
	private final ItemPredicate record = s -> s.getItem() instanceof ItemRecord;

	@Override
	public InitiationResult<?> applies(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
		if (record.test(stack)) {
			IBlockState state = world.getBlockState(mouseOver.getBlockPos());
			if (state.getBlock() == Blocks.JUKEBOX && !state.getValue(BlockJukebox.HAS_RECORD)) {
				return InitiationResult.successBlock(this);
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
		return "Insert Record";
	}

	@Override
	public Animation create(World world, EntityPlayer player, ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Void data) {
		return new AnimationInsertRecord(stack, actionBarSlot, hand, mouseOver, record, new ActionBlock());
	}
}
