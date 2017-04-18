package com.pau101.auginter.client.interaction.type;

import java.util.function.Predicate;

import com.pau101.auginter.client.interaction.AnimationSupplier;
import com.pau101.auginter.client.interaction.InitiationResult;
import com.pau101.auginter.client.interaction.Interaction;
import com.pau101.auginter.client.interaction.action.ActionBlock;
import com.pau101.auginter.client.interaction.animation.type.AnimationBottleDrain;
import com.pau101.auginter.client.interaction.animation.type.AnimationBottleFill;
import com.pau101.auginter.client.interaction.animation.type.AnimationBucketDrain;
import com.pau101.auginter.client.interaction.animation.type.AnimationBucketFill;
import com.pau101.auginter.client.interaction.animation.type.AnimationCleanLeatherArmor;
import com.pau101.auginter.client.interaction.item.ItemPredicate;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public final class InteractionCauldron implements Interaction {
	private final ItemPredicate bucket = s -> s.getItem() == Items.BUCKET; 

	private final ItemPredicate waterBucket = s -> s.getItem() == Items.WATER_BUCKET; 

	private final Predicate<ItemStack> bucketOrWater = bucket.or(waterBucket);

	private final ItemPredicate bottle = s -> s.getItem() == Items.GLASS_BOTTLE; 

	private final ItemPredicate waterBottle = s -> s.getItem() == Items.POTIONITEM && PotionUtils.getPotionFromItem(s) == PotionTypes.WATER;

	private final Predicate<ItemStack> bottleOrWater = bottle.or(waterBottle);

	private final ItemPredicate leatherArmor = s -> s.getItem() instanceof ItemArmor && ((ItemArmor) s.getItem()).getArmorMaterial() == ArmorMaterial.LEATHER;

	private final Predicate<ItemStack> coloredLeatherArmor = leatherArmor.and(s -> ((ItemArmor) s.getItem()).hasColor(s));

	private final AnimationSupplier<Void> bucketDrain = (world, player, stack, actionBarSlot, hand, mouseOver, data) -> {
		return new AnimationBucketDrain<ActionBlock.Data>(stack, actionBarSlot, hand, mouseOver, bucketOrWater, new ActionBlock(), mouseOver.getBlockPos()) {
			@Override
			protected ActionBlock.Data getActionData() {
				return new ActionBlock.Data(getMouseOver(), getStack(), getHand());
			}
		};
	};

	private final AnimationSupplier<Void> bucketFill = (world, player, stack, actionBarSlot, hand, mouseOver, data) -> {
		return new AnimationBucketFill<ActionBlock.Data>(stack, actionBarSlot, hand, mouseOver, bucketOrWater, new ActionBlock()) {
			@Override
			protected ActionBlock.Data getActionData() {
				return new ActionBlock.Data(getMouseOver(), getStack(), getHand());
			}
		};
	};

	private final AnimationSupplier<Float> bottleDrain = (world, player, stack, actionBarSlot, hand, mouseOver, level) -> {
		return new AnimationBottleDrain(stack, actionBarSlot, hand, mouseOver, bottleOrWater, level);
	};

	private final AnimationSupplier<Float> bottleFill = (world, player, stack, actionBarSlot, hand, mouseOver, level) -> {
		return new AnimationBottleFill(stack, actionBarSlot, hand, mouseOver, bottleOrWater, level);
	};

	private final AnimationSupplier<Void> cleanLeatherArmor = (world, player, stack, actionBarSlot, hand, mouseOver, data) -> {
		return new AnimationCleanLeatherArmor(stack, actionBarSlot, hand, mouseOver, leatherArmor);
	};

	@Override
	public InitiationResult<?> applies(World world, EntityPlayer player, ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
		IBlockState state = world.getBlockState(mouseOver.getBlockPos());
		if (isCauldron(state)) {
			int level = state.getValue(BlockCauldron.LEVEL);
			if (waterBucket.test(stack)) {
				if (level < 3) {
					return InitiationResult.successBlock(bucketDrain);
				}
			} else if (bucket.test(stack)) {
				if (level == 3) {
					return InitiationResult.successBlock(bucketFill);
				}
			} else if (bottle.test(stack)) {
				if (level > 0) {
					return InitiationResult.successBlock(bottleFill, (level - 1) / 3F);
				}
			} else if (waterBottle.test(stack)) {
				if (level < 3) {
					return InitiationResult.successBlock(bottleDrain, level / 3F);
				}
			} else if (coloredLeatherArmor.test(stack)) {
				if (level > 0) {
					return InitiationResult.successBlock(cleanLeatherArmor);
				}
			}
			// TODO: clean banner
		}
		return InitiationResult.fail();
	}

	private boolean isCauldron(IBlockState state) {
		return state.getBlock() == Blocks.CAULDRON;
	}
}