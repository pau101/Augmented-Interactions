package com.pau101.auginter.client.interaction.item;

import java.util.function.Predicate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ItemPredicate extends Predicate<ItemStack> {
	@Override
	boolean test(ItemStack stack);

	static ItemPredicate of(Item item) {
		return s -> s.getItem() == item;
	}
}
