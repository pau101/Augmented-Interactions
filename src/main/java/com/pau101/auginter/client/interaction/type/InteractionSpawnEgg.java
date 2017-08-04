package com.pau101.auginter.client.interaction.type;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.pau101.auginter.client.interaction.AnimationSupplier;
import com.pau101.auginter.client.interaction.InitiationResult;
import com.pau101.auginter.client.interaction.Interaction;
import com.pau101.auginter.client.interaction.action.ActionBlock;
import com.pau101.auginter.client.interaction.animation.Animation;
import com.pau101.auginter.client.interaction.animation.type.AnimationSpawnEgg;
import com.pau101.auginter.client.interaction.item.ItemPredicate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class InteractionSpawnEgg implements Interaction, AnimationSupplier<Vec3d> {
	private final ItemPredicate spawnEgg = ItemPredicate.of(Items.SPAWN_EGG);

	@Override
	public InitiationResult<?> applies(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
		if (spawnEgg.test(stack)) {
			BlockPos pos = mouseOver.getBlockPos();
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() == Blocks.MOB_SPAWNER) {
				return InitiationResult.fail();
			}
			BlockPos front = pos.offset(mouseOver.sideHit);
			double top = getTop(world, front);
			Vec3d spawnPos = new Vec3d(front.getX() + 0.5, front.getY() + top, front.getZ() + 0.5);
			return InitiationResult.success(this, spawnPos);
		}
		return InitiationResult.fail();
	}

	@Override
	public ImmutableList<AnimationSupplier<?>> getAnimationSuppliers() {
		return ImmutableList.of(this);
	}

	@Override
	public String getName() {
		return "Place Spawn Egg";
	}

	@Override
	public Animation create(World world, EntityPlayer player, ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Vec3d pos) {
		return new AnimationSpawnEgg<ActionBlock.Data>(stack, actionBarSlot, hand, mouseOver, spawnEgg, new ActionBlock(), pos) {
			@Override
			protected ActionBlock.Data getActionData() {
				return new ActionBlock.Data(getMouseOver(), getStack(), getHand());
			}
		};
	}

	// ItemMonsterPlacer#func_190909_a
	private static double getTop(World world, BlockPos pos) {
		AxisAlignedBB bounds = new AxisAlignedBB(pos).offset(0, -1, 0);
		List<AxisAlignedBB> boxes = world.getCollisionBoxes(null, bounds);
		if (boxes.isEmpty()) {
			return 0;
		}
		double top = bounds.minY;
		for (AxisAlignedBB box : boxes) {
			top = Math.max(box.maxY, top);
		}
		return top - pos.getY();
	}
}
