package com.pau101.auginter.client.interaction.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public final class ItemPredicateFluidHandler implements ItemPredicate {
	public static final ItemPredicate INSTANCE = new ItemPredicateFluidHandler();

	private ItemPredicateFluidHandler() {}

	@Override
	public boolean test(ItemStack stack) {
		return stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
	}
}
