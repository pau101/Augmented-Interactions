package com.pau101.auginter.client.interaction;

import com.pau101.auginter.client.interaction.animation.Animation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public final class InitiationResult<D> {
	private final AnimationSupplier<D> animation;

	private final Type type;

	private final boolean allowBlockActivation;

	private final D data;

	private InitiationResult(AnimationSupplier<D> animation, Type type) {
		this(animation, type, true);
	}

	private InitiationResult(AnimationSupplier<D> animation, Type type, D data) {
		this(animation, type, true, data);
	}

	private InitiationResult(AnimationSupplier<D> animation, Type type, boolean allowBlockActivation) {
		this(animation, type, allowBlockActivation, null);
	}

	private InitiationResult(AnimationSupplier<D> animation, Type type, boolean allowBlockActivation, D data) {
		this.animation = animation;
		this.type = type;
		this.allowBlockActivation = allowBlockActivation;
		this.data = data;
	}

	public Type getType() {
		return type;
	}

	public boolean allowBlockActivation() {
		return allowBlockActivation;
	}

	public Animation createAnimation(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult result) {
		if (animation == null) {
			throw new IllegalStateException("No animation supplier for failure");
		}
		return animation.create(world, player, stack, slot, hand, result, data);
	}

	public enum Type {
		SUCCESS, FAIL;
	}

	public static InitiationResult<Void> result(AnimationSupplier<Void> initator, boolean isSuccess) {
		return new InitiationResult<>(initator, isSuccess ? Type.SUCCESS : Type.FAIL, null);
	}

	public static InitiationResult<Void> success(AnimationSupplier<Void> initator) {
		return success(initator, null);
	}

	public static <D> InitiationResult<D> success(AnimationSupplier<D> initator, D data) {
		return new InitiationResult<D>(initator, Type.SUCCESS, data);
	}

	public static <D> InitiationResult<D> fail() {
		return new InitiationResult<>(null, Type.FAIL, false, null);
	}

	public static InitiationResult<Void> successBlock(AnimationSupplier<Void> initator) {
		return new InitiationResult<>(initator, Type.SUCCESS, false, null);
	}

	public static <D> InitiationResult<D> successBlock(AnimationSupplier<D> initator, D data) {
		return new InitiationResult<>(initator, Type.SUCCESS, false, data);
	}
}
