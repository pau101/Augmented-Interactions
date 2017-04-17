package com.pau101.auginter.client.interaction;

import com.pau101.auginter.client.interaction.animation.Animation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public final class InitiationResult<D> {
	private final Interaction<D> initiator;

	private final Type type;

	private final D data;

	public InitiationResult(Interaction<D> initiator, Type type) {
		this(initiator, type, null);
	}

	public InitiationResult(Interaction<D> initiator, Type type, D data) {
		this.initiator = initiator;
		this.type = type;
		this.data = data;
	}

	public Type getType() {
		return type;
	}

	public boolean allowBlockActivation(World world, BlockPos pos, IBlockState state) {
		return initiator.allowBlockActivation(world, pos, state);
	}

	public Animation create(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult result) {
		return initiator.create(world, player, stack, slot, hand, result, data);
	}

	public enum Type {
		SUCCESS, FAIL;
	}

	public static InitiationResult<Void> result(Interaction<Void> initator, boolean isSuccess) {
		return new InitiationResult<>(initator, isSuccess ? Type.SUCCESS : Type.FAIL, null);
	}

	public static InitiationResult<Void> success(Interaction<Void> initator) {
		return success(initator, null);
	}

	public static <D> InitiationResult<D> success(Interaction<D> initator, D data) {
		return new InitiationResult<D>(initator, Type.SUCCESS, data);
	}

	public static <D> InitiationResult<D> fail(Interaction<D> initator) {
		return fail(initator, null);
	}

	public static <D> InitiationResult<D> fail(Interaction<D> initator, D data) {
		return new InitiationResult<D>(initator, Type.FAIL, data);
	}
}
