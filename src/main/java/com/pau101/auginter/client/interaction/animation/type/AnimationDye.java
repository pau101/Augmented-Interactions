package com.pau101.auginter.client.interaction.animation.type;

import java.util.Random;
import java.util.function.Predicate;

import com.pau101.auginter.client.interaction.action.ActionEntity;
import com.pau101.auginter.client.interaction.animation.AnimationConsumed;
import com.pau101.auginter.client.interaction.math.MatrixStack;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class AnimationDye extends AnimationConsumed<ActionEntity.Data> {
	private final Vec3d relativeHit;

	public AnimationDye(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Predicate<ItemStack> itemPredicate) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate, new ActionEntity());
		relativeHit = mouseOver.hitVec.subtract(mouseOver.entityHit.getPositionVector());
	}

	@Override
	protected ActionEntity.Data getActionData() {
		return new ActionEntity.Data(getMouseOver(), getHand());
	}

	@Override
	protected int getTransformDuration() {
		return getDuration();
	}

	@Override
	protected int getDuration() {
		return 5;
	}

	@Override
	protected void onActionSuccess(Minecraft mc, World world, EntityPlayer player, ItemStack stack) {
		Random rng = world.rand;
		int num = rng.nextInt(4) + 5;
		Entity entity = getMouseOver().entityHit;
		Vec3d sheep = entity.getPositionVector().add(relativeHit);
		Vec3d eye = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3d vec = eye.subtract(sheep).normalize().scale(0.3);
		while (num --> 0) {
			double x = sheep.xCoord + rng.nextDouble() * 0.2 - 0.01;
			double y = sheep.yCoord + rng.nextDouble() * 0.2 - 0.01;
			double z = sheep.zCoord + rng.nextDouble() * 0.2 - 0.01;
			double mx = vec.xCoord + rng.nextDouble() * 0.05 - 0.025;
			double my = vec.yCoord + rng.nextDouble() * 0.05;
			double mz = vec.zCoord + rng.nextDouble() * 0.05 - 0.025;
			world.spawnParticle(EnumParticleTypes.ITEM_CRACK, x, y, z, mx, my, mz, Item.getIdFromItem(getStack().getItem()), getStack().getMetadata());	
		}
	}

	@Override
	public void transform(MatrixStack matrix, Minecraft mc, World world, EntityPlayer player, float yaw, boolean isLeft, float delta) {
		Entity entity = getMouseOver().entityHit;
		double x = Mth.lerp(entity.lastTickPosX, entity.posX, delta);
		double y = Mth.lerp(entity.lastTickPosY, entity.posY, delta);
		double z = Mth.lerp(entity.lastTickPosZ, entity.posZ, delta);
		untranslatePlayer(matrix, player, delta);
		matrix.translate(x + relativeHit.xCoord, y + relativeHit.yCoord, z + relativeHit.zCoord);
		matrix.rotate(-yaw, 0, 1, 0);
		matrix.rotate(Mth.lerp(player.prevRotationPitch, player.rotationPitch, delta), 1, 0, 0);
		matrix.rotate(180, 0, 1, 0);
		matrix.scale(0.7, 0.7, 0.7);
		matrix.translate(-0.5 / 16, -0.5 / 16, 0);
	}
}
