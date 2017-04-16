package com.pau101.auginter.client.interaction;

import java.lang.reflect.Method;
import java.util.Iterator;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.pau101.auginter.client.interactions.InteractionFlintAndSteel;
import com.pau101.auginter.client.interactions.InteractionShears;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.oredict.OreDictionary;

public final class InteractionRenderer {
	private static final Matrix4d FLIP_X;

	static {
		FLIP_X = new Matrix4d();
		FLIP_X.setIdentity();
		FLIP_X.m00 = -1;
	}

	private final Minecraft mc = Minecraft.getMinecraft();

	private final Multimap<InteractionType, InteractionRegistration> interactionRegistry = HashMultimap.create();

	private final Multimap<EnumHand, Interaction> interactions = HashMultimap.create();

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

	public InteractionRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
		register(InteractionType.ENTITY, (stack, slot, hand, mouseOver) -> {
			Entity entity = mouseOver.entityHit;
			if (stack.getItem() == Items.SHEARS && entity instanceof IShearable && entity instanceof EntityLivingBase) {
				return ((IShearable) entity).isShearable(stack, entity.world, new BlockPos(entity));
			}
			return false;
		}, InteractionShears::new);
		register(InteractionType.BLOCK, (stack, slot, hand, mouseOver) -> {
			if (stack.getItem() == Items.FLINT_AND_STEEL) {
				if (mc.player.isSneaking() && !stack.getItem().doesSneakBypassUse(stack, mc.world, mouseOver.getBlockPos(), mc.player)) {
					return true;
				}
				return !blockImplementsOnActivated.getUnchecked(mc.world.getBlockState(mouseOver.getBlockPos()).getBlock());
			}
			return false;
		}, InteractionFlintAndSteel::new);
	}

	private void register(Item item, InteractionType type, InteractionFactory interaction) {
		register(item, OreDictionary.WILDCARD_VALUE, type, interaction);
	}

	private void register(Item item, int meta, InteractionType type, InteractionFactory interaction) {
		register(new ItemStack(item, 1, meta), type, interaction);
	}

	private void register(Block block, InteractionType type, InteractionFactory interaction) {
		register(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE), type, interaction);
	}

	private void register(IBlockState state, InteractionType type, InteractionFactory interaction) {
		register(new ItemStack(state.getBlock(), 1, state.getBlock().damageDropped(state)), type, interaction);
	}

	private void register(ItemStack stack, InteractionType type, InteractionFactory interaction) {
		register(type, (s, a, h, m) -> OreDictionary.itemMatches(stack, s, false), interaction);
	}

	private void register(InteractionType type, InteractionPredicate predicate, InteractionFactory interaction) {
		interactionRegistry.put(type, new InteractionRegistration(predicate, interaction));
	}

	private boolean hasInteractions() {
		return interactions.size() > 0;
	}

	public void start(EnumHand hand, Interaction interaction) {
		interactions.put(hand, interaction);
	}

	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent event) {
		EntityPlayer player = mc.player;
		if (!mc.isGamePaused() && player != null) {
			if (event.phase == TickEvent.Phase.START) {
				tickStart();
			} else {
				tickEnd(player);
			}
		}
	}

	@SubscribeEvent
	public void renderSpecificHand(RenderSpecificHandEvent event) {
		if (shouldRender(mc.player)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void render(RenderWorldLastEvent event) {
		EntityPlayerSP player = mc.player;
		if (shouldRender(player)) {
			render(player, event.getPartialTicks());
		}
	}

	public boolean rightClickMouse(EnumHand hand) {
		RayTraceResult mouseOver = mc.objectMouseOver;
		InteractionType type = raytraceInteractionTypeLookup.get(mouseOver.typeOfHit);
		if (type != null) {
			Interaction interaction = getInteraction(mc.player, hand, mouseOver, interactionRegistry.get(type));
			if (interaction != null) {
				start(hand, interaction);
				return true;
			}
		}
		return false;
	}

	private Interaction getInteraction(EntityPlayer player, EnumHand hand, RayTraceResult mouseOver, Iterable<InteractionRegistration> regs) {
		ItemStack stack = player.getHeldItem(hand);
		int slot = hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : -1;
		for (InteractionRegistration reg : regs) {
			if (reg.applies(stack, slot, hand, mouseOver)) {
				return reg.createInteration(stack, slot, hand, mouseOver);
			}
		}
		return null;
	}

	private void tickStart() {
		for (Interaction inter : interactions.values()) {
			inter.updatePrev();
		}
	}

	private void tickEnd(EntityPlayer player) {
		ItemRenderer renderer = mc.getItemRenderer();
		ItemStack mainHand = renderer.itemStackMainHand;
		ItemStack offHand = renderer.itemStackOffHand;
		Iterator<Interaction> inters = interactions.values().iterator();
		while (inters.hasNext()) {
			Interaction interaction = inters.next();
			EnumHand hand = interaction.getHand();
			interaction.update(player, player.getHeldItem(hand), hand == EnumHand.OFF_HAND || interaction.getSlot() == player.inventory.currentItem);
			if (interaction.isDone(player, hand == EnumHand.MAIN_HAND ? mainHand : offHand)) {
				inters.remove();
			}
		}
	}

	private boolean shouldRender(EntityPlayer player) {
		return hasInteractions() && player != null && player == mc.getRenderViewEntity() && mc.gameSettings.thirdPersonView == 0 && !mc.gameSettings.hideGUI && !player.isPlayerSleeping() && !mc.playerController.isSpectator();
	}

	private void render(EntityPlayerSP player, float delta) {
		mc.entityRenderer.enableLightmap();
		GlStateManager.enableFog();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableRescaleNormal();
		// setLightmap
		int light = mc.world.getCombinedLight(new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ), 0);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (light & 0xFFFF), (light >> 16));
		for (Interaction inter : interactions.values()) {
			render(player, inter, delta);
		}
		mc.entityRenderer.disableLightmap();
		GlStateManager.disableFog();
	}

	private void render(EntityPlayerSP player, Interaction interaction, float delta) {
		ItemRenderer renderer = mc.getItemRenderer();
		boolean isMainHand = interaction.hand == EnumHand.MAIN_HAND;
		boolean isLeft = player.getPrimaryHand() == EnumHandSide.RIGHT != isMainHand;
		ItemStack stack;
		float pep, ep;
		if (isMainHand) {
			stack = renderer.itemStackMainHand;
			pep = renderer.prevEquippedProgressMainHand;
			ep = renderer.equippedProgressMainHand;
		} else {
			stack = renderer.itemStackOffHand;
			pep = renderer.prevEquippedProgressOffHand;
			ep = renderer.equippedProgressOffHand;
		}
		float equip;
		if (interaction.isDifferentItem(stack, player.getHeldItem(interaction.hand))) {
			equip = 1 - Mth.lerp(pep, ep, delta);
		} else {
			equip = 1 - interaction.getTransform(delta);
		}
		GlStateManager.pushMatrix();
		float yaw = Mth.lerp(player.prevRotationYaw, player.rotationYaw, delta);
		if (equip == 0) {
			interaction.transform(GLMatrix.INSTANCE, player, yaw, isLeft, delta);
		} else {
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.pushMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			float renderEquip = ep == 1 ? 0 : 1;
			interpolateWorldToHeld(player, stack, yaw, isLeft, isMainHand, interaction::transform, equip, renderEquip, delta);
		}
		mc.getRenderItem().renderItem(stack, player, TransformType.NONE, false);
		GlStateManager.popMatrix();
		if (equip != 0) {
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}
	}

	private void interpolateWorldToHeld(EntityPlayerSP player, ItemStack stack, float yaw, boolean isLeft, boolean isMainHand, ModelViewTransform worldTransform, float t, float regularEquip, float delta) {
		Matrix matrix = new Matrix();
		matrix.loadIdentity();
		matrix.mul(Mth.getMatrix(GL11.GL_MODELVIEW_MATRIX));
		worldTransform.transform(matrix, player, yaw, isLeft, delta);
		Matrix4d modelView1 = matrix.getTransform();
		Matrix4d perspective1 = Mth.getMatrix(GL11.GL_PROJECTION_MATRIX);
		float fov = 70;
		Entity entity = mc.getRenderViewEntity();
		if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHealth() <= 0) {
			float deathTime = ((EntityLivingBase) entity).deathTime + delta;
			fov /= (1 - 500 / (deathTime + 500)) * 2 + 1;
		}
		IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.world, entity, delta);
		if (state.getMaterial() == Material.WATER) {
			fov *= 6 / 7F;
		}
		fov = ForgeHooksClient.getFOVModifier(mc.entityRenderer, entity, state, delta, fov);
		float farPlaneDistance = mc.gameSettings.renderDistanceChunks * 16;
		float swing = player.getSwingProgress(delta);
		float thisSwing = isMainHand ? swing : 0;
		float armPitch = player.prevRenderArmPitch + (player.renderArmPitch - player.prevRenderArmPitch) * delta;
		float armYaw = player.prevRenderArmYaw + (player.renderArmYaw - player.prevRenderArmYaw) * delta;
		float swingX = -0.4F * MathHelper.sin(MathHelper.sqrt(thisSwing) * Mth.PI);
		float swingY = 0.2F * MathHelper.sin(MathHelper.sqrt(thisSwing) * Mth.TAU);
		float swingZ = -0.2F * MathHelper.sin(thisSwing * Mth.PI);
		float swingAmt = MathHelper.sin(thisSwing * thisSwing * Mth.PI);
		float swingAng = MathHelper.sin(MathHelper.sqrt(thisSwing) * Mth.PI);
		int side = isLeft ? -1 : 1;
		matrix.loadIdentity();
		if (mc.gameSettings.anaglyph) {
			matrix.translate(-(EntityRenderer.anaglyphField * 2 - 1) * 0.07F, 0, 0);
		}
		matrix.perspective(fov, mc.displayWidth / (float) mc.displayHeight, 0.05F, farPlaneDistance * 2);
		Matrix4d perspective2 = matrix.getTransform();
		matrix.loadIdentity();
		if (mc.gameSettings.anaglyph) {
			matrix.translate((EntityRenderer.anaglyphField * 2 - 1) * 0.1F, 0, 0);
		}
		// hurtCameraEffect
		if (entity instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) entity;
			if (living.getHealth() < 0) {
				matrix.rotate(40 - 8000 / (living.deathTime + delta + 200), 0, 0, 1);
			}
			float hurtTime = living.hurtTime - delta;
			if (hurtTime >= 0) {
				hurtTime /= living.maxHurtTime;
				hurtTime = MathHelper.sin(hurtTime * hurtTime * hurtTime * hurtTime * Mth.PI);
				float attackYaw = living.attackedAtYaw;
				GlStateManager.rotate(-attackYaw, 0, 1, 0);
				GlStateManager.rotate(-hurtTime * 14, 0, 0, 1);
				GlStateManager.rotate(attackYaw, 0, 1, 0);
			}
		}
		// applyBobbing
		if (mc.gameSettings.viewBobbing && entity instanceof EntityPlayer) {
			float walkDelta = player.distanceWalkedModified - player.prevDistanceWalkedModified;
			float walked = -(player.distanceWalkedModified + walkDelta * delta);
			float camYaw = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * delta;
			float camPitch = player.prevCameraPitch + (player.cameraPitch - player.prevCameraPitch) * delta;
			matrix.translate(MathHelper.sin(walked * Mth.PI) * camYaw * 0.5F, -Math.abs(MathHelper.cos(walked * Mth.PI) * camYaw), 0);
			matrix.rotate(MathHelper.sin(walked * Mth.PI) * camYaw * 3, 0, 0, 1);
			matrix.rotate(Math.abs(MathHelper.cos(walked * Mth.PI - 0.2F) * camYaw) * 5 + camPitch, 1, 0, 0);
		}
		// rotateArm
		matrix.rotate((player.rotationPitch - armPitch) * 0.1F, 1, 0, 0);
		matrix.rotate((player.rotationYaw - armYaw) * 0.1F, 0, 1, 0);
		// renderItemInFirstPerson + transformSideFirstPerson
		matrix.translate(side * swingX + side * 0.56F, swingY - 0.52F - regularEquip * 0.6F, swingZ - 0.72F);
		// transformFirstPerson
		matrix.rotate(side * (45 + swingAmt * -20), 0, 1, 0);
		matrix.rotate(side * swingAng * -20, 0, 0, 1);
		matrix.rotate(swingAng * -80, 1, 0, 0);
		matrix.rotate(side * -45, 0, 1, 0);
		IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(stack, mc.world, player);
		TransformType transform = isLeft ? TransformType.FIRST_PERSON_LEFT_HAND : TransformType.FIRST_PERSON_RIGHT_HAND;
		if (model instanceof IPerspectiveAwareModel) {
			Pair<? extends IBakedModel, Matrix4f> pair = ((IPerspectiveAwareModel) model).handlePerspective(transform);
			if (pair.getRight() != null) {
				Matrix4d mat = new Matrix4d(pair.getRight());
				if (isLeft) {
					mat.mul(FLIP_X, mat);
					mat.mul(mat, FLIP_X);
				}
				matrix.mul(mat);
			}
		} else {
			ItemTransformVec3f vec = model.getItemCameraTransforms().getTransform(transform);
			if (vec != ItemTransformVec3f.DEFAULT) {
				matrix.translate(side * (ItemCameraTransforms.offsetTranslateX + vec.translation.x), ItemCameraTransforms.offsetTranslateY + vec.translation.y, ItemCameraTransforms.offsetTranslateZ + vec.translation.z);
				float rx = ItemCameraTransforms.offsetRotationX + vec.rotation.x;
				float ry = ItemCameraTransforms.offsetRotationY + vec.rotation.y;
				float rz = ItemCameraTransforms.offsetRotationZ + vec.rotation.z;
				if (isLeft) {
					ry = -ry;
					rz = -rz;
				}
				matrix.rotate(Mth.getQuat(rx, ry, rz));
				matrix.scale(ItemCameraTransforms.offsetScaleX + vec.scale.x, ItemCameraTransforms.offsetScaleY + vec.scale.y, ItemCameraTransforms.offsetScaleZ + vec.scale.z);
			}
		}
		Matrix4d modelView2 = matrix.getTransform();
		Matrix4d modelView = Mth.lerp(modelView1, modelView2, t);
		Matrix4d perspective = Mth.lerpPerspective(perspective1, perspective2, t);
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.loadIdentity();
		Mth.multMatrix(perspective);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.loadIdentity();
		Mth.multMatrix(modelView);
	}

	private static Method getOnBlockActivated(Class<?> cls, String name) {
		try {
			return cls.getDeclaredMethod(name, World.class, BlockPos.class, IBlockState.class, EntityPlayer.class, EnumHand.class, EnumFacing.class, float.class, float.class, float.class);
		} catch (NoSuchMethodException | SecurityException e) {
			return null;
		}
	}

	@FunctionalInterface
	private interface ModelViewTransform {
		void transform(MatrixStack matrix, EntityPlayer player, float yaw, boolean isLeft, float delta);
	}

	@FunctionalInterface
	public interface InteractionPredicate {
		boolean applies(ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver);
	}

	@FunctionalInterface
	public interface InteractionFactory {
		Interaction create(ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver);
	}

	private final class InteractionRegistration {
		private final InteractionPredicate predicate;

		private final InteractionFactory interaction;

		public InteractionRegistration(InteractionPredicate stack, InteractionFactory interaction) {
			this.predicate = stack;
			this.interaction = interaction;
		}

		public boolean applies(ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
			return predicate.applies(stack, slot, hand, mouseOver);
		}

		public Interaction createInteration(ItemStack stack, int slot, EnumHand hand, RayTraceResult mouseOver) {
			return interaction.create(stack, slot, hand, mouseOver);
		}
	}
}
