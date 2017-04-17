package com.pau101.auginter.client.interaction.type;

import com.pau101.auginter.client.interaction.InitiationResult;
import com.pau101.auginter.client.interaction.Interaction;
import com.pau101.auginter.client.interaction.action.ActionUse;
import com.pau101.auginter.client.interaction.animation.Animation;
import com.pau101.auginter.client.interaction.animation.type.AnimationBucketDrain;
import com.pau101.auginter.client.interaction.item.ItemPredicateFluidHandler;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class InteractionBucketDrain implements Interaction<BlockPos> {
	@Override
	public InitiationResult<BlockPos> applies(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
		if (!ItemPredicateFluidHandler.INSTANCE.test(stack)) {
			return InitiationResult.fail(this);
		}
		FluidStack fs = FluidUtil.getFluidContained(stack);
		if (fs == null || fs.amount == 0) {
			return InitiationResult.fail(this);
		}
		RayTraceResult result = Mth.rayTraceBlocks(world, player, false);
		if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos = result.getBlockPos();
			boolean replaceable = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
			pos = replaceable && result.sideHit == EnumFacing.UP ? pos : pos.offset(result.sideHit);
			IBlockState state = world.getBlockState(pos);
			if (world.isAirBlock(pos) || !state.getMaterial().isSolid() || state.getBlock().isReplaceable(world, pos)) {
				return InitiationResult.success(this, pos);
			}
		}
		return InitiationResult.fail(this);
	}

	@Override
	public Animation create(World world, EntityPlayer player, ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, BlockPos fluidPos) {
		return new AnimationBucketDrain<EnumHand>(stack, actionBarSlot, hand, mouseOver, ItemPredicateFluidHandler.INSTANCE, new ActionUse(), fluidPos) {
			@Override
			protected EnumHand getActionData() {
				return getHand();
			}
		};
	}
}
