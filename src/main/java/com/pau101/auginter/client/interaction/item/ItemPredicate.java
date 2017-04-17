package com.pau101.auginter.client.interaction.item;

import java.util.function.Predicate;

import net.minecraft.item.ItemStack;

public interface ItemPredicate extends Predicate<ItemStack> {
	@Override
	boolean test(ItemStack stack);
}
