package com.pau101.auginter.client.interaction.animation.type;

import com.pau101.auginter.client.interaction.action.Action;
import com.pau101.auginter.client.interaction.animation.AnimationDurated;
import com.pau101.auginter.client.interaction.item.ItemPredicate;
import com.pau101.auginter.client.interaction.math.MatrixStack;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public abstract class AnimationBucketFill<D> extends AnimationDurated<D> {
	public AnimationBucketFill(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, ItemPredicate itemPredicate, Action<D> action) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate, action);
	}

	@Override
	protected int getActionTick() {
		return getDuration() * 3 / 5;
	}

	@Override
	protected int getDuration() {
		return 24;
	}

	@Override
	public void transform(MatrixStack matrix, Minecraft mc, World world, EntityPlayer player, float yaw, boolean isLeft, float delta) {
		untranslatePlayer(matrix, player, delta);
		BlockPos pos = getMouseOver().getBlockPos();
		float percent = getTick(delta) / getDuration();
		float sin = (MathHelper.sin(percent * Mth.TAU + Mth.PI / 2) + 1) / 2;
		matrix.translate(pos.getX() + 0.5, pos.getY() + 0.6 + sin * 2, pos.getZ() + 0.5);
		matrix.rotate(-yaw, 0, 1, 0);
		matrix.rotate(Mth.lerp(player.prevRotationPitch, player.rotationPitch, delta) / 2, 1, 0, 0);
		matrix.rotate(-percent * 270 + Math.max((percent - 0.45F), 0) / (1 - 0.45F) * 420, 0, 0, 1);
		matrix.rotate(180, 0, 1, 0);
		matrix.scale(0.85, 0.85, 0.85);
	}
}
