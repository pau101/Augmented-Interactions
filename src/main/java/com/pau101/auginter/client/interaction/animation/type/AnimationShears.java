package com.pau101.auginter.client.interaction.animation.type;

import java.util.function.Predicate;

import com.pau101.auginter.client.interaction.action.ActionEntity;
import com.pau101.auginter.client.interaction.animation.AnimationDurated;
import com.pau101.auginter.client.interaction.math.MatrixStack;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class AnimationShears extends AnimationDurated<ActionEntity.Data> {
	private static final float WIDTH = 0.7F;

	private static final float LENGTH = 0.85F;

	public AnimationShears(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Predicate<ItemStack> itemPredicate) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate, new ActionEntity());
	}

	@Override
	protected ActionEntity.Data getActionData() {
		return new ActionEntity.Data(getMouseOver(), getHand());
	}

	@Override
	protected int getActionTick(Minecraft mc, World world, EntityPlayer player) {
		return getDuration() - getTransformDuration();
	}

	@Override
	protected int getDuration() {
		return 36;
	}

	@Override
	public void update(Minecraft mc, World world, EntityPlayer player, ItemStack held) {
		super.update(mc, world, player, held);
		float transform = getTransform(1);
		if (transform == 1) {
			Entity entity = getMouseOver().entityHit;
			float percent = getPercent(1);
			float rotation = getRotation(percent);
			float sin = MathHelper.sin(rotation);
			float cos = MathHelper.cos(rotation);
			float elv = getElevation(percent, transform) + 0.15F;
			float entityYaw;
			if (entity instanceof EntityLivingBase) {
				entityYaw = ((EntityLivingBase) entity).renderYawOffset;
			} else {
				entityYaw = entity.rotationYaw;
			}
			Vec3d pos = new Vec3d(cos * (WIDTH - 0.2F), 0, sin * (LENGTH - 0.2F)).rotateYaw((float) -Math.toRadians(entityYaw));
			double x = entity.posX + pos.xCoord;
			double y = entity.posY + entity.height + elv;
			double z = entity.posZ + pos.zCoord;
			if (world.rand.nextBoolean()) {
				IBlockState state = Blocks.WOOL.getDefaultState();
				if (entity instanceof EntitySheep) {
					state = state.withProperty(BlockColored.COLOR, ((EntitySheep) entity).getFleeceColor());
				}
				int num = world.rand.nextInt(3) + 2;
				while (num --> 0) {
					double px = x + world.rand.nextFloat() * 0.1F - 0.05F;
					double py = y + world.rand.nextFloat() * 0.1F - 0.05F;
					double pz = z + world.rand.nextFloat() * 0.1F - 0.05F;
					world.spawnParticle(EnumParticleTypes.BLOCK_DUST, px, py, pz, 0, 0, 0, Block.getStateId(state));	
				}
			}
			if (getTick() % 3 == 0 && world.rand.nextFloat() < 0.8F) {
				world.playSound(x, y, z, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1, 0.9F + world.rand.nextFloat() * 0.3F, false);
			}
		}
	}

	@Override
	public void transform(MatrixStack matrix, Minecraft mc, World world, EntityPlayer player, float yaw, boolean isLeft, float delta) {
		Entity entity = getMouseOver().entityHit;
		double x = Mth.lerp(entity.lastTickPosX, entity.posX, delta);
		double y = Mth.lerp(entity.lastTickPosY, entity.posY, delta);
		double z = Mth.lerp(entity.lastTickPosZ, entity.posZ, delta);
		float percent = getPercent(delta);
		float rotation = getRotation(percent);
		float transform = getTransform(delta);
		float elv = getElevation(percent, transform);
		float sin = MathHelper.sin(rotation);
		float cos = MathHelper.cos(rotation);
		untranslatePlayer(matrix, player, delta);
		matrix.translate(x, y + entity.height + elv, z);
		float entityPrevYaw, entityYaw;
		if (entity instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) entity;
			entityPrevYaw = living.prevRenderYawOffset;
			entityYaw = living.renderYawOffset;
		} else {
			entityPrevYaw = entity.prevRotationYaw;
			entityYaw = entity.rotationYaw;
		}
		matrix.rotate(-Mth.lerp(entityPrevYaw, entityYaw, delta), 0, 1, 0);
		matrix.translate(cos * WIDTH, 0, sin * LENGTH);
		matrix.rotate(-Math.toDegrees(rotation) + 180, 0, 1, 0);
		matrix.rotate(35 - elv * 50, 0, 0, 1);
		matrix.rotate(-90, 1, 0, 0);
		matrix.rotate(-45 - MathHelper.cos(percent * 8 * Mth.TAU) * 15, 0, 0, 1);
		matrix.scale(0.5, 0.5, 0.5);
		matrix.translate(0.35F, 0.35F, 0);
	}

	private float getPercent(float delta) {
		return getTick(delta) / getDuration();
	}

	private float getRotation(float percent) {
		return percent * Mth.TAU + Mth.PI / 2;
	}

	private float getElevation(float percent, float transform) {
		return MathHelper.cos(percent * 3.5F * Mth.TAU + 0.4F) * 0.15F - 0.1F + (1 - transform);
	}
}