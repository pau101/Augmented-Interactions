package com.pau101.auginter.client.interactions;

import com.pau101.auginter.client.interaction.InteractionDuratedUse;
import com.pau101.auginter.client.interaction.MatrixStack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public abstract class InteractionBucket extends InteractionDuratedUse {
	public InteractionBucket(ItemStack used, int actionBarSlot, EnumHand hand, RayTraceResult mouseOver) {
		super(used, actionBarSlot, hand, mouseOver);
	}

	@Override
	public final boolean isDifferentItem(ItemStack first, ItemStack second) {
		return first.func_190926_b() != second.func_190926_b() || (first.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) != second.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null));
	}
}
