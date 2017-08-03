package com.pau101.auginter.client.interaction.animation.type;

import java.util.function.Predicate;

import com.pau101.auginter.client.interaction.action.ActionBlock;
import com.pau101.auginter.client.interaction.animation.AnimationConsumed;
import com.pau101.auginter.client.interaction.math.MatrixStack;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class AnimationFireCharge extends AnimationConsumed<ActionBlock.Data> {
	public AnimationFireCharge(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Predicate<ItemStack> itemPredicate) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate, new ActionBlock());
	}

	@Override
	protected ActionBlock.Data getActionData() {
		return new ActionBlock.Data(getMouseOver(), getStack(), getHand());
	}

	@Override
	protected int getTransformDuration() {
		return getDuration();
	}

	@Override
	protected int getDuration() {
		return 6;
	}

	@Override
	public void transform(MatrixStack matrix, Minecraft mc, World world, EntityPlayer player, float yaw, boolean isLeft, float delta) {
		untranslatePlayer(matrix, player, delta);
		RayTraceResult mouseOver = getMouseOver();
		Vec3d pos = mouseOver.hitVec;
		matrix.translate(pos.x, pos.y, pos.z);
		matrix.rotate(-yaw, 0, 1, 0);
		matrix.rotate(Mth.lerp(player.prevRotationPitch, player.rotationPitch, delta), 1, 0, 0);
		matrix.rotate(200, 0, 1, 0);
		matrix.scale(0.7, 0.7, 0.7);
		matrix.translate(-0.5 / 16, -0.5 / 16, 0);
	}
}
