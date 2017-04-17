package com.pau101.auginter.client.interaction.animation.type;

import com.pau101.auginter.client.interaction.action.ActionBlock;
import com.pau101.auginter.client.interaction.animation.AnimationDurated;
import com.pau101.auginter.client.interaction.item.ItemPredicate;
import com.pau101.auginter.client.interaction.math.MatrixStack;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class AnimationFlintAndSteel extends AnimationDurated<ActionBlock.Data> {
	public AnimationFlintAndSteel(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, ItemPredicate itemPredicate) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate, new ActionBlock());
	}

	@Override
	protected ActionBlock.Data getActionData() {
		return new ActionBlock.Data(getMouseOver(), getStack(), getHand());
	}

	@Override
	protected int getActionTick() {
		return getDuration() / 2;
	}

	@Override
	protected int getDuration() {
		return 28;
	}

	@Override
	public void update(Minecraft mc, World world, EntityPlayer player, ItemStack stack) {
		super.update(mc, world, player, stack);
		int tick = getTick();
		RayTraceResult mouseOver = getMouseOver();
		if (tick >= getTransformDuration() && tick < getDuration() - getTransformDuration() && (tick - getTransformDuration()) % ((getDuration() - getTransformDuration() * 2) / 2) == 0) {
			Vec3d pos = mouseOver.hitVec.add(new Vec3d(mouseOver.sideHit.getDirectionVec()).scale(0.4F));
			Vec3d vec = player.getLook(1);
			int num = world.rand.nextInt(3) + 2;
			while (num --> 0) {
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.xCoord, pos.yCoord, pos.zCoord, 0, 0, 0);
			}
			BlockPos ignitePos = mouseOver.getBlockPos().offset(mouseOver.sideHit);
			if (tick != getActionTick() || player.world.getBlockState(ignitePos).getBlock() != Blocks.FIRE) {
				world.playSound(pos.xCoord, pos.yCoord, pos.zCoord, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, 0.8F + world.rand.nextFloat() * 0.4F, false);
			}
		}
	}

	@Override
	public void transform(MatrixStack matrix, Minecraft mc, World world, EntityPlayer player, float yaw, boolean isLeft, float delta) {
		untranslatePlayer(matrix, player, delta);
		RayTraceResult mouseOver = getMouseOver();
		Vec3d pos = mouseOver.hitVec.add(new Vec3d(mouseOver.sideHit.getDirectionVec()).scale(0.4F));
		matrix.translate(pos.xCoord, pos.yCoord, pos.zCoord);
		matrix.rotate(-yaw, 0, 1, 0);
		float percent = (getTick(delta) - getTransformDuration()) / (getDuration() - getTransformDuration() * 2);
		boolean starting = percent >= 0 && percent <= 1;
		if (starting) {
			float sin = (float) Math.pow(MathHelper.sin(percent * 2 * Mth.PI), 4);
			matrix.translate(0, sin * 0.12F, 0);
		}
		matrix.rotate(Mth.lerp(player.prevRotationPitch, player.rotationPitch, delta), 1, 0, 0);
		if (starting) {
			float sin = MathHelper.sin(percent * 2 * Mth.TAU - Mth.PI);
			matrix.rotate((sin * 50) - 25, 0, 1, 0);
			matrix.rotate(-sin * 10, 0, 0, 1);
		}
		matrix.rotate(180, 0, 1, 0);
		matrix.scale(0.45, 0.45, 0.45);
	}
}
