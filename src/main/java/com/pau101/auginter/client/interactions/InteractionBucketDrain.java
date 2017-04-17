package com.pau101.auginter.client.interactions;

import java.util.Random;

import com.pau101.auginter.client.interaction.MatrixStack;
import com.pau101.auginter.client.interaction.Mth;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class InteractionBucketDrain extends InteractionBucket {
	public InteractionBucketDrain(ItemStack used, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver) {
		super(used, actionBarSlot, hand, mouseOver);
	}

	@Override
	protected int getUseTick() {
		return getDuration() / 2;
	}

	@Override
	protected int getDuration() {
		return 20;
	}

	@Override
	protected void onUse() {
		World world = Minecraft.getMinecraft().world;
		Random rng = world.rand;
		BlockPos pos = mouseOver.getBlockPos();
		Block block = world.getBlockState(pos).getBlock();
		if (block == Blocks.FLOWING_WATER || block == Blocks.FLOWING_LAVA) {
			EnumParticleTypes particle;
			int num;
			if (block == Blocks.FLOWING_WATER) {
				particle = EnumParticleTypes.WATER_SPLASH;
				num = rng.nextInt(5) + 6;
			} else {
				particle = EnumParticleTypes.LAVA;
				num = rng.nextInt(2) + 3;
			}
			while (num --> 0) {
				double x = pos.getX() + 0.5 + rng.nextDouble() * 0.3 - 0.15;
				double y = pos.getY() + 0.7 + rng.nextDouble() * 0.2;
				double z = pos.getZ() + 0.5 + rng.nextDouble() * 0.3 - 0.15;
				double mx = rng.nextDouble() * 0.08 - 0.04;
				double my = rng.nextDouble() * 0.15 + 0.1;
				double mz = rng.nextDouble() * 0.08 - 0.04;
				world.spawnParticle(particle, x, y, z, mx, my, mz);
			}
		}
	}

	@Override
	public void transform(MatrixStack matrix, EntityPlayer player, float yaw, boolean isLeft, float delta) {
		untranslatePlayer(matrix, player, delta);
		BlockPos pos = mouseOver.getBlockPos();
		float percent = getTick(delta) / getDuration();
		float sin = (MathHelper.sin(percent * Mth.TAU + Mth.PI / 2) + 1) / 2;
		matrix.translate(pos.getX() + 0.5, pos.getY() + 0.6 + sin * 2, pos.getZ() + 0.5);
		matrix.rotate(-yaw, 0, 1, 0);
		matrix.rotate(Mth.lerp(player.prevRotationPitch, player.rotationPitch, delta) / 2, 1, 0, 0);
		matrix.rotate(-percent * 220, 0, 0, 1);
		matrix.rotate(180, 0, 1, 0);
		matrix.scale(0.85, 0.85, 0.85);
	}
}
