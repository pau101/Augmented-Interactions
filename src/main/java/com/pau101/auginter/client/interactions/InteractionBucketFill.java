package com.pau101.auginter.client.interactions;

import com.pau101.auginter.client.interaction.InteractionDuratedUse;
import com.pau101.auginter.client.interaction.MatrixStack;
import com.pau101.auginter.client.interaction.Mth;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class InteractionBucketFill extends InteractionDuratedUse {
	public InteractionBucketFill(ItemStack used, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver) {
		super(used, actionBarSlot, hand, mouseOver);
	}

	@Override
	protected int getUseTick() {
		return getDuration() * 3 / 5;
	}

	@Override
	protected int getDuration() {
		return 24;
	}

	@Override
	public boolean isDifferentItem(ItemStack first, ItemStack second) {
		return first.func_190926_b() != second.func_190926_b() || (first.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) != second.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null));
	}

	@Override
	public void update(EntityPlayer player, ItemStack stack, boolean isEquipped) {
		super.update(player, stack, isEquipped);
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
		matrix.rotate(-percent * 270 + Math.max((percent - 0.45F), 0) / (1 - 0.45F) * 420, 0, 0, 1);
		matrix.rotate(180, 0, 1, 0);
		matrix.scale(0.85, 0.85, 0.85);
	}
}
