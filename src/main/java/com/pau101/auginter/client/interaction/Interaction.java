package com.pau101.auginter.client.interaction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

public abstract class Interaction {
	private static final int TRANSFORM_DURATION = 6;

	protected final ItemStack stack;

	protected final int slot;

	protected final EnumHand hand;

	protected final RayTraceResult mouseOver;

	protected int prevTransform;

	protected int transform;

	public Interaction(ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
		this.stack = stack;
		this.slot = slot;
		this.hand = hand;
		this.mouseOver = mouseOver;
	}

	public final ItemStack getStack() {
		return stack;
	}

	public final int getSlot() {
		return slot;
	}

	public final EnumHand getHand() {
		return hand;
	}

	protected boolean shouldTransform() {
		return transform < getTransformDuration();
	}

	protected int getTransformDuration() {
		return TRANSFORM_DURATION;
	}

	public final float getTransform(float delta) {
		return Mth.lerp(prevTransform, transform, delta) / getTransformDuration();
	}

	public boolean isDone(EntityPlayer player, ItemStack stack) {
		return isDifferentItem(this.stack, stack);
	}

	public boolean isDifferentItem(ItemStack first, ItemStack second) {
		return (first == null) != (second == null) || first != null && second != null && first.getItem() != second.getItem();
	}

	public void updatePrev() {
		prevTransform = transform;
	}

	public void update(EntityPlayer player, ItemStack held, boolean isEquipped) {
		if (isEquipped && shouldTransform()) {
			transform++;
		}
	}

	public abstract void transform(MatrixStack matrix, EntityPlayer player, float yaw, boolean isLeft, float delta);

	protected final void untranslatePlayer(MatrixStack matrix, EntityPlayer player, float delta) {
		double px = Mth.lerp(player.lastTickPosX, player.posX, delta);
		double py = Mth.lerp(player.lastTickPosY, player.posY, delta);
		double pz = Mth.lerp(player.lastTickPosZ, player.posZ, delta);
		matrix.translate(-px, -py, -pz);
	}
}
