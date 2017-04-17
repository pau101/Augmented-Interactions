package com.pau101.auginter.client.interaction.animation.type;

import java.util.Random;

import com.pau101.auginter.client.interaction.action.Action;
import com.pau101.auginter.client.interaction.animation.AnimationDurated;
import com.pau101.auginter.client.interaction.item.ItemPredicate;
import com.pau101.auginter.client.interaction.math.MatrixStack;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public abstract class AnimationBucketDrain<D> extends AnimationDurated<D> {
	private final BlockPos fluidPos;

	public AnimationBucketDrain(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, ItemPredicate itemPredicate, Action<D> action, BlockPos fluidPos) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate, action);
		this.fluidPos = fluidPos;
	}

	@Override
	protected int getActionTick() {
		return getDuration() / 2;
	}

	@Override
	protected int getDuration() {
		return 20;
	}

	@Override
	protected void onActionSuccess(Minecraft mc, World world, EntityPlayer player, ItemStack stack) {
		Random rng = world.rand;
		Item item = getStack().getItem();
		if (item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET) {
			EnumParticleTypes particle;
			int num;
			if (item == Items.WATER_BUCKET) {
				particle = EnumParticleTypes.WATER_SPLASH;
				num = rng.nextInt(5) + 6;
			} else {
				particle = EnumParticleTypes.LAVA;
				num = rng.nextInt(2) + 3;
			}
			while (num --> 0) {
				double x = fluidPos.getX() + 0.5 + rng.nextDouble() * 0.3 - 0.15;
				double y = fluidPos.getY() + 0.8 + rng.nextDouble() * 0.2;
				double z = fluidPos.getZ() + 0.5 + rng.nextDouble() * 0.3 - 0.15;
				double mx = rng.nextDouble() * 0.08 - 0.04;
				double my = rng.nextDouble() * 0.15 + 0.1;
				double mz = rng.nextDouble() * 0.08 - 0.04;
				world.spawnParticle(particle, x, y, z, mx, my, mz);
			}
		}
	}

	@Override
	public void transform(MatrixStack matrix, Minecraft mc, World world, EntityPlayer player, float yaw, boolean isLeft, float delta) {
		untranslatePlayer(matrix, player, delta);
		float percent = getTick(delta) / getDuration();
		float sin = (MathHelper.sin(percent * Mth.TAU + Mth.PI / 2) + 1) / 2;
		matrix.translate(fluidPos.getX() + 0.5, fluidPos.getY() + 0.7 + sin * 2, fluidPos.getZ() + 0.5);
		matrix.rotate(-yaw, 0, 1, 0);
		matrix.rotate(Mth.lerp(player.prevRotationPitch, player.rotationPitch, delta) / 2, 1, 0, 0);
		matrix.rotate(-percent * 220, 0, 0, 1);
		matrix.rotate(180, 0, 1, 0);
		matrix.scale(0.85, 0.85, 0.85);
	}
}
