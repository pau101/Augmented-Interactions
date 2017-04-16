package com.pau101.auginter.client.interaction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

public abstract class InteractionDurated extends Interaction {
	protected int prevTick;

	protected int tick;

	public InteractionDurated(ItemStack used, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver) {
		super(used, actionBarSlot, hand, mouseOver);
	}

	protected abstract int getUseTick();

	protected abstract void use();

	protected abstract int getDuration();

	@Override
	protected boolean shouldTransform() {
		return super.shouldTransform() && tick < getTransformDuration();
	}

	@Override
	public boolean isDone(EntityPlayer player, ItemStack stack) {
		return super.isDone(player, stack) || tick >= getDuration();
	}

	protected final float getTick(float delta) {
		return Mth.lerp(prevTick, tick, delta);
	}

	@Override
	public void updatePrev() {
		super.updatePrev();
		prevTick = tick;
	}

	@Override
	public void update(EntityPlayer player, ItemStack stack, boolean isEquipped) {
		super.update(player, stack, isEquipped);
		int duration = getDuration(), tDuration = getTransformDuration();
		if (tick < duration && isEquipped) {
			if (tick == getUseTick()) {
				use();
			}
			tick++;
			if (duration - tDuration > tDuration && tick >= duration - tDuration && transform > 0) {
				transform--;
			}
		}
		/*if (tick == duration) {
			tick = 0;
			transform = 0;
		}*/
	}
}
