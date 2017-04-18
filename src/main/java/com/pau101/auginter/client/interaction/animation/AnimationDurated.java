package com.pau101.auginter.client.interaction.animation;

import java.util.function.Predicate;

import com.pau101.auginter.client.interaction.action.Action;
import com.pau101.auginter.client.interaction.math.Mth;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public abstract class AnimationDurated<D> extends Animation {
	private final Action<D> action;

	private int prevTick;

	private int tick;

	private UseState state = UseState.WAITING;

	public AnimationDurated(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Predicate<ItemStack> itemPredicate, Action<D> action) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate);
		this.action = action;
	}

	protected abstract D getActionData();

	protected abstract int getActionTick(Minecraft mc, World world, EntityPlayer player);

	protected abstract int getDuration();

	@Override
	protected boolean shouldTransform() {
		return super.shouldTransform() && tick < getTransformDuration();
	}

	@Override
	public boolean isDone(EntityPlayer player, ItemStack stack) { 
		return super.isDone(player, stack) || tick >= getDuration();
	}

	protected boolean shouldReverseTransform() {
		return true;
	}

	protected final UseState getUseState() {
		return state;
	}

	protected final int getTick() {
		return tick;
	}

	protected final float getTick(float delta) {
		return Mth.lerp(prevTick, tick, delta);
	}

	protected void onActionSuccess(Minecraft mc, World world, EntityPlayer player, ItemStack stack) {}

	@Override
	public void updatePrev() {
		super.updatePrev();
		prevTick = tick;
	}

	@Override
	public void update(Minecraft mc, World world, EntityPlayer player, ItemStack stack) {
		super.update(mc, world, player, stack);
		int duration = getDuration(), tDuration = getTransformDuration();
		if (tick < duration) {
			tick++;
			if (tick == getActionTick(mc, world, player)) {
				if (action.perform(mc, getActionData())) {
					state = UseState.USE_SUCCEEDED;	
					onActionSuccess(mc, world, player, stack);
				} else {
					state = UseState.USE_FAILED;
				}
			}
			if (shouldReverseTransform() && duration - tDuration > tDuration && tick >= duration - tDuration && getTransform() > 0) {
				decrementTransform();
			}
		}
	}

	protected enum UseState {
		WAITING, USE_SUCCEEDED, USE_FAILED;
	}
}
