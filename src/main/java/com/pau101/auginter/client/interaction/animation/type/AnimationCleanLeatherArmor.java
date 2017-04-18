package com.pau101.auginter.client.interaction.animation.type;

import java.util.Random;
import java.util.function.Predicate;

import com.pau101.auginter.client.interaction.action.ActionBlock;
import com.pau101.auginter.client.interaction.action.ActionBlock.Data;
import com.pau101.auginter.client.interaction.animation.AnimationDurated;
import com.pau101.auginter.client.interaction.math.MatrixStack;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public final class AnimationCleanLeatherArmor extends AnimationDurated<ActionBlock.Data> {
	public AnimationCleanLeatherArmor(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Predicate<ItemStack> itemPredicate) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate, new ActionBlock());
	}

	@Override
	protected final Data getActionData() {
		return new ActionBlock.Data(getMouseOver(), getStack(), getHand());
	}

	@Override
	protected int getActionTick(Minecraft mc, World world, EntityPlayer player) {
		return getDuration() / 2;
	}

	@Override
	protected int getDuration() {
		return 30;
	}

	@Override
	public void update(Minecraft mc, World world, EntityPlayer player, ItemStack stack) {
		super.update(mc, world, player, stack);
		if (getTick() > getActionTick(mc, world, player) + getDuration() / 8 && getTick() - getActionTick(mc, world, player) < getDuration() / 3 && getTick() % 2 == 0) {
			Random rng = world.rand;
			int num = rng.nextInt(3) + 3;
			BlockPos pos = getMouseOver().getBlockPos();
			float swingPercent = (getTick() - getActionTick(mc, world, player)) / (float) (getDuration() - getActionTick(mc, world, player));
			while (num --> 0) {
				double x = pos.getX() + 0.5 + rng.nextDouble() * 0.4 - 0.2;
				double y = pos.getY() + 0.8 + swingPercent + rng.nextDouble() * 0.3;
				double z = pos.getZ() + 0.5 + rng.nextDouble() * 0.4 - 0.2;
				double mx = rng.nextDouble() * 0.1 - 0.05;
				double mz = rng.nextDouble() * 0.1 - 0.05;
				world.spawnParticle(EnumParticleTypes.WATER_SPLASH, x, y, z, mx, 0, mz);
			}
		}
		// Can't use onActionSuccess because BlockCauldron doesn't return true for this interaction on the client
		if (getTick() == getActionTick(mc, world, player)) {
			BlockPos pos = getMouseOver().getBlockPos();
			world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1, 0.8F + world.rand.nextFloat() * 0.4F, false);
		}
	}

	@Override
	public void transform(MatrixStack matrix, Minecraft mc, World world, EntityPlayer player, float yaw, boolean isLeft, float delta) {
		untranslatePlayer(matrix, player, delta);
		BlockPos pos = getMouseOver().getBlockPos();
		float tick = getTick(delta);
		float percent = tick / getDuration();
		float sin = (float) Math.pow(MathHelper.sin(percent * Mth.PI + Mth.PI / 2), 2);
		float swing;
		if (getTick() >= getActionTick(mc, world, player)) {
			float swingPercent = (tick - getActionTick(mc, world, player)) / (getDuration() - getActionTick(mc, world, player));
			swing = MathHelper.sin(swingPercent * Mth.TAU * 4) * 30 * swingPercent;
		} else {
			swing = 0;
		}
		matrix.translate(pos.getX() + 0.5, pos.getY() + 0.5 + sin * 2, pos.getZ() + 0.5);
		matrix.rotate(-yaw, 0, 1, 0);
		matrix.rotate(Mth.lerp(player.prevRotationPitch, player.rotationPitch, delta) / 2, 1, 0, 0);
		matrix.rotate(swing, 0, 0, 1);
		matrix.rotate(180, 0, 1, 0);
		matrix.scale(0.85, 0.85, 0.85);
	}
}
