package com.pau101.auginter.client.interaction.animation.type;

import java.util.function.Predicate;

import com.pau101.auginter.client.interaction.action.Action;
import com.pau101.auginter.client.interaction.action.ActionBlock;
import com.pau101.auginter.client.interaction.animation.AnimationConsumed;
import com.pau101.auginter.client.interaction.math.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public final class AnimationInsertRecord extends AnimationConsumed<ActionBlock.Data> {
	public AnimationInsertRecord(ItemStack stack, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver, Predicate<ItemStack> itemPredicate, Action<ActionBlock.Data> action) {
		super(stack, actionBarSlot, hand, mouseOver, itemPredicate, action);
	}

	@Override
	protected ActionBlock.Data getActionData() {
		return new ActionBlock.Data(getMouseOver(), getStack(), getHand());
	}

	@Override
	protected int getDuration() {
		return 16;
	}

	@Override
	public void transform(MatrixStack matrix, Minecraft mc, World world, EntityPlayer player, float yaw, boolean isLeft, float delta) {
		BlockPos pos = getMouseOver().getBlockPos();
		untranslatePlayer(matrix, player, delta);
		matrix.translate(pos.getX() + 0.5, pos.getY() + 1.7 - getTick(delta) / getDuration(), pos.getZ() + 0.5);
		matrix.rotate(90 , 0, 1, 0);
		matrix.scale(0.65, 0.65, 0.65);
	}
}
