package com.pau101.auginter.client.interaction.animation.type;

import java.util.Random;
import java.util.function.Predicate;

import com.pau101.auginter.client.interaction.action.Action;
import com.pau101.auginter.client.interaction.animation.AnimationConsumed;
import com.pau101.auginter.client.interaction.math.MatrixStack;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class AnimationSpawnEgg<D> extends AnimationConsumed<D> {
	private final Vec3d pos;

	public AnimationSpawnEgg(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Predicate<ItemStack> itemPredicate, Action<D> action, Vec3d pos) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate, action);
		this.pos = pos;
	}

	@Override
	protected int getDuration() {
		return 40;
	}

	@Override
	protected void onActionSuccess(Minecraft mc, World world, EntityPlayer player, ItemStack stack) {
		Random rng = world.rand;
		int num = rng.nextInt(4) + 8;
		while (num --> 0) {
			double x = pos.xCoord + rng.nextDouble() * 0.5 - 0.25;
			double y = pos.yCoord + rng.nextDouble() * 0.5 + 0.1;
			double z = pos.zCoord + rng.nextDouble() * 0.5 - 0.25;
			double mx = rng.nextDouble() * 0.22 - 0.11;
			double my = rng.nextDouble() * 0.3;
			double mz = rng.nextDouble() * 0.22 - 0.11;
			world.spawnParticle(EnumParticleTypes.ITEM_CRACK, x, y, z, mx, my, mz, Item.getIdFromItem(getStack().getItem()), getStack().getMetadata());	
		}
	}

	@Override
	public void transform(MatrixStack matrix, Minecraft mc, World world, EntityPlayer player, float yaw, boolean isLeft, float delta) {
		untranslatePlayer(matrix, player, delta);
		final float scale = 0.75F;
		final float halfItem = scale * 0.5F;
		float tick = getTick(delta);
		float percent = tick / getDuration();
		matrix.translate(pos.xCoord, pos.yCoord + halfItem, pos.zCoord);
		int dropTickEnd = getDuration() * 4 / 10;
		int shakeTickEnd = dropTickEnd + getDuration() * 6 / 10;
		if (tick < dropTickEnd) {
			float bobTime = tick / dropTickEnd;
			matrix.translate(0, MathHelper.abs(MathHelper.sin(bobTime * Mth.TAU)) * 0.6 * (1 - bobTime), 0);
		}
		matrix.rotate(-yaw, 0, 1, 0);
		int dropTickMid = dropTickEnd / 2;
		if (tick >= dropTickMid && tick < shakeTickEnd) {
			float shakeTime = (tick - dropTickMid) / (shakeTickEnd - dropTickMid);
			matrix.translate(0, -halfItem, 0);
			float amount = ((MathHelper.cos(shakeTime * Mth.PI) + 1) / 2);
			matrix.rotate(MathHelper.sin(shakeTime * Mth.TAU * 2) * amount * 20, 0, 0, 1);
			matrix.translate(0, halfItem, 0);
		}
		matrix.rotate(Mth.lerp(player.prevRotationPitch, player.rotationPitch, delta) / 2, 1, 0, 0);
		matrix.rotate(180, 0, 1, 0);
		matrix.scale(scale, scale, scale);
		matrix.translate(-0.5 / 16, -0.5 / 16, 0);
	}
}
