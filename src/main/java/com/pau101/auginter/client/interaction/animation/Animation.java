package com.pau101.auginter.client.interaction.animation;

import java.util.function.Predicate;

import com.pau101.auginter.client.interaction.math.MatrixStack;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public abstract class Animation {
	private static final int TRANSFORM_DURATION = 6;

	private final ItemStack stack;

	private final int slot;

	private final EnumHand hand;

	private final RayTraceResult mouseOver;

	private final Predicate<ItemStack> itemPredicate;

	private int prevTransform;

	private int transform;

	public Animation(ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver, Predicate<ItemStack> itemPredicate) {
		this.stack = stack.copy();
		this.slot = slot;
		this.hand = hand;
		this.mouseOver = mouseOver;
		this.itemPredicate = itemPredicate;
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

	public final RayTraceResult getMouseOver() {
		return mouseOver;
	}

	public final int getTransform() {
		return transform;
	}

	protected final void decrementTransform() {
		transform--;
		if (transform < 0) {
			transform = 0;
		}
	}

	public final float getTransform(float delta) {
		return Mth.lerp(prevTransform, transform, delta) / getTransformDuration();
	}

	public final boolean doItemsMatch(ItemStack first, ItemStack second) {
		return itemPredicate.test(first) && itemPredicate.test(second);
	}

	protected int getTransformDuration() {
		return TRANSFORM_DURATION;
	}

	protected boolean shouldTransform() {
		return transform < getTransformDuration();
	}

	public boolean isDone(Minecraft mc, World world, EntityPlayer player, ItemStack stack) {
		return !itemPredicate.test(stack);
	}

	public boolean isVisible() {
		return true;
	}

	public void updatePrev() {
		prevTransform = transform;
	}

	public void update(Minecraft mc, World world, EntityPlayer player, ItemStack held) {
		if (shouldTransform()) {
			transform++;
		}
	}

	public abstract void transform(MatrixStack matrix, Minecraft mc, World world, EntityPlayer player, float yaw, boolean isLeft, float delta);

	protected final void untranslatePlayer(MatrixStack matrix, EntityPlayer player, float delta) {
		double px = Mth.lerp(player.lastTickPosX, player.posX, delta);
		double py = Mth.lerp(player.lastTickPosY, player.posY, delta);
		double pz = Mth.lerp(player.lastTickPosZ, player.posZ, delta);
		matrix.translate(-px, -py, -pz);
	}
}
