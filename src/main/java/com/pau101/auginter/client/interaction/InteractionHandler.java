package com.pau101.auginter.client.interaction;

import java.lang.reflect.Method;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.pau101.auginter.client.interaction.render.AnimationRenderer;
import com.pau101.auginter.client.interaction.type.InteractionBucketDrain;
import com.pau101.auginter.client.interaction.type.InteractionBucketFill;
import com.pau101.auginter.client.interaction.type.InteractionCauldron;
import com.pau101.auginter.client.interaction.type.InteractionDye;
import com.pau101.auginter.client.interaction.type.InteractionFireCharge;
import com.pau101.auginter.client.interaction.type.InteractionFlintAndSteel;
import com.pau101.auginter.client.interaction.type.InteractionRecord;
import com.pau101.auginter.client.interaction.type.InteractionShears;
import com.pau101.auginter.client.interaction.type.InteractionSpawnEgg;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public final class InteractionHandler {
	private final Minecraft mc = Minecraft.getMinecraft();

	private final Multimap<InteractionType, Interaction> interactions = HashMultimap.create();

	private final LoadingCache<Block, Boolean> blockImplementsOnActivated = CacheBuilder.newBuilder()
		.build(CacheLoader.from(block -> {
			for (Class<?> cls = block.getClass(); cls != null && cls != Block.class; cls = cls.getSuperclass()) {
				if (getOnBlockActivated(cls, "func_180639_a") != null || getOnBlockActivated(cls, "onBlockActivated") != null) {
					return true;
				}
			}
			return false;
		}));

	private final ImmutableMap<RayTraceResult.Type, InteractionType> raytraceInteractionTypeLookup = ImmutableMap.<RayTraceResult.Type, InteractionType>builder()
		.put(RayTraceResult.Type.BLOCK, InteractionType.BLOCK)
		.put(RayTraceResult.Type.ENTITY, InteractionType.ENTITY)
		.build();

	private final AnimationRenderer renderer;

	private final AnimationWarden animationWarden;

	private final KeyBinding skipAnimation;

	public InteractionHandler(AnimationRenderer renderer, AnimationWarden animationWarden, KeyBinding skipAnimation) {
		this.renderer = renderer;
		this.animationWarden = animationWarden;
		this.skipAnimation = skipAnimation;
		register(InteractionType.ENTITY, new InteractionShears());
		register(InteractionType.BLOCK, new InteractionFlintAndSteel());
		register(InteractionType.USE, new InteractionBucketFill());
		register(InteractionType.USE, new InteractionBucketDrain());
		register(InteractionType.BLOCK, new InteractionCauldron());
		register(InteractionType.BLOCK, new InteractionFireCharge());
		register(InteractionType.BLOCK, new InteractionSpawnEgg());
		register(InteractionType.ENTITY, new InteractionDye());
		register(InteractionType.BLOCK, new InteractionRecord());
	}

	private void register(InteractionType type, Interaction interaction) {
		interactions.put(type, interaction);
	}

	public ImmutableCollection<Interaction> getInteractions() {
		return ImmutableList.copyOf(interactions.values());
	}

	public boolean rightClickMouse(EnumHand hand) {
		if (skipAnimation.isKeyDown()) {
			return false;
		}
		World world = mc.world;
		EntityPlayer player = mc.player;
		RayTraceResult mouseOver = mc.objectMouseOver;
		InteractionType type = raytraceInteractionTypeLookup.get(mouseOver.typeOfHit);
		InitiationResult<?> result = null;
		if (type != null) {
			result = getInteraction(world, player, hand, mouseOver, interactions.get(type));
		}
		if (result == null) {
			type = InteractionType.USE;
			result = getInteraction(world, player, hand, mouseOver, interactions.get(type));
		}
		if (result != null) {
			if (!result.isEnabled(animationWarden)) {
				return false;
			}
			ItemStack stack = player.getHeldItem(hand);
			if (mouseOver.getBlockPos() != null && result.allowBlockActivation()) {
				boolean act = blockImplementsOnActivated.getUnchecked(mc.world.getBlockState(mouseOver.getBlockPos()).getBlock());
				if (act && (!player.isSneaking() || stack.getItem().doesSneakBypassUse(stack, world, mouseOver.getBlockPos(), player))) {
					return false;
				}
			}
			renderer.start(hand, result.createAnimation(world, player, stack, hand == EnumHand.OFF_HAND ? -1 : player.inventory.currentItem, hand, mouseOver));
			return true;
		}
		return false;
	}

	private InitiationResult<?> getInteraction(World world, EntityPlayer player, EnumHand hand, RayTraceResult mouseOver, Iterable<Interaction> interactions) {
		ItemStack stack = player.getHeldItem(hand);
		int slot = hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : -1;
		for (Interaction interaction : interactions) {
			InitiationResult<?> result = interaction.applies(world, player, stack, slot, hand, mouseOver);
			if (result.getType() == InitiationResult.Type.SUCCESS) {
				return result;
			}
		}
		return null;
	}

	private static Method getOnBlockActivated(Class<?> cls, String name) {
		try {
			return cls.getDeclaredMethod(name, World.class, BlockPos.class, IBlockState.class, EntityPlayer.class, EnumHand.class, EnumFacing.class, float.class, float.class, float.class);
		} catch (NoSuchMethodException | SecurityException e) {
			return null;
		}
	}
}
