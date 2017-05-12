package com.pau101.auginter.client.interaction.type;

import com.google.common.collect.ImmutableList;
import com.pau101.auginter.client.interaction.AnimationSupplier;
import com.pau101.auginter.client.interaction.InitiationResult;
import com.pau101.auginter.client.interaction.Interaction;
import com.pau101.auginter.client.interaction.action.ActionUse;
import com.pau101.auginter.client.interaction.animation.Animation;
import com.pau101.auginter.client.interaction.animation.type.AnimationBucketFill;
import com.pau101.auginter.client.interaction.item.ItemPredicateFluidHandler;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public final class InteractionBucketFill implements Interaction, AnimationSupplier<RayTraceResult> {
	@Override
	public InitiationResult<RayTraceResult> applies(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
		if (!ItemPredicateFluidHandler.INSTANCE.test(stack)) {
			return InitiationResult.fail();
		}
		FluidStack fs = FluidUtil.getFluidContained(stack);
		if (fs != null && fs.amount >= Fluid.BUCKET_VOLUME) {
			return InitiationResult.fail();
		}
		RayTraceResult result = Mth.rayTraceBlocks(world, player, true);
		if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos = result.getBlockPos();
			if (world.getBlockState(pos).getMaterial().isLiquid()) {
				IFluidHandler fluid = FluidUtil.getFluidHandler(world, pos, result.sideHit);
				if (fluid != null) {
					FluidStack fstack = fluid.drain(Integer.MAX_VALUE, false);
					if (fstack != null && fstack.amount > 0) {
						return InitiationResult.success(this, result);
					}
				}
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
		return "Fill Bucket";
	}

	@Override
	public Animation create(World world, EntityPlayer player, ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, RayTraceResult fluidMouseOver) {
		return new AnimationBucketFill<EnumHand>(stack, actionBarSlot, hand, fluidMouseOver, ItemPredicateFluidHandler.INSTANCE, new ActionUse()) {
			@Override
			protected EnumHand getActionData() {
				return getHand();
			}
		};
	}
}
