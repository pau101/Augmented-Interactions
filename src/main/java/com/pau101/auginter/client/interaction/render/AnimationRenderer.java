package com.pau101.auginter.client.interaction.render;

import java.util.ArrayDeque;
import java.util.Iterator;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.pau101.auginter.client.interaction.animation.Animation;
import com.pau101.auginter.client.interaction.math.GLMatrix;
import com.pau101.auginter.client.interaction.math.Matrix;
import com.pau101.auginter.client.interaction.math.MatrixStack;
import com.pau101.auginter.client.interaction.math.Mth;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public final class AnimationRenderer {
	private static final Matrix4d FLIP_X;

	static {
		FLIP_X = new Matrix4d();
		FLIP_X.setIdentity();
		FLIP_X.m00 = -1;
	}

	private final Minecraft mc = Minecraft.getMinecraft();

	private final Multimap<EnumHand, Animation> animations = HashMultimap.create();

	// RenderWorldLastEvent is a terrible place to render things so we do this
	private final Particle renderHook = new Particle(null, 0, 0, 0, 0, 0, 0) {
		@Override
		public int getFXLayer() {
			return 3;
		}

		@Override
		public void onUpdate() {}

		@Override
		public void move(double x, double y, double z) {}

		@Override
		public void setPosition(double x, double y, double z) {}

		@Override
		public void renderParticle(BufferBuilder buf, Entity nil, float delta, float x, float z, float yz, float xy, float xz) {
			render(delta);
		}
	};

	public AnimationRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void start(EnumHand hand, Animation animation) {
		animations.put(hand, animation);
	}

	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent event) {
		EntityPlayer player = mc.player;
		if (!mc.isGamePaused() && player != null) {
			if (event.phase == TickEvent.Phase.START) {
				tickStart();
			} else {
				tickEnd(player);
				injectRenderHook();
			}
		}
	}

	@SubscribeEvent
	public void renderSpecificHand(RenderSpecificHandEvent event) {
		if (animations.get(event.getHand()).size() > 0 && shouldRender(mc.player)) {
			event.setCanceled(true);
		}
	}

	private void tickStart() {
		for (Animation inter : animations.values()) {
			inter.updatePrev();
		}
	}

	private void tickEnd(EntityPlayer player) {
		ItemRenderer renderer = mc.getItemRenderer();
		Iterator<Animation> anims = animations.values().iterator();
		ItemStack main = renderer.itemStackMainHand;
		ItemStack off = renderer.itemStackOffHand;
		World world = mc.world;
		while (anims.hasNext()) {
			Animation anim = anims.next();
			EnumHand hand = anim.getHand();
			anim.update(mc, world, player, player.getHeldItem(hand));
			if (anim.isDone(mc, world, player, hand == EnumHand.MAIN_HAND ? main.isEmpty() ? player.getHeldItem(hand) : main : off.isEmpty() ? player.getHeldItem(hand) : off)) {
				anims.remove();
			}
		}
	}

	private void injectRenderHook() {
		ArrayDeque<Particle> layer = mc.effectRenderer.fxLayers[renderHook.getFXLayer()][0];
		if (!layer.contains(renderHook)) {
			layer.addFirst(renderHook);
		}
	}

	private boolean shouldRender(EntityPlayer player) {
		return player != null && player == mc.getRenderViewEntity() && mc.gameSettings.thirdPersonView == 0 && !mc.gameSettings.hideGUI && !player.isPlayerSleeping() && !mc.playerController.isSpectator();
	}

	private void render(float delta) {
		EntityPlayerSP player = mc.player;
		if (animations.size() > 0 && shouldRender(player)) {
			World world = mc.world;
			RenderHelper.enableStandardItemLighting();
			// setLightmap
			int light = world.getCombinedLight(new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ), 0);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (light & 0xFFFF), (light >> 16));
			GlStateManager.color(1, 1, 1);
			ItemRenderer renderer = mc.getItemRenderer();
			for (Animation anim : animations.values()) {
				if (anim.isVisible()) {
					render(world, player, renderer, anim, delta);	
				}
			}
			RenderHelper.disableStandardItemLighting();
		}
	}

	private void render(World world, EntityPlayerSP player, ItemRenderer renderer, Animation anim, float delta) {
		boolean isMainHand = anim.getHand() == EnumHand.MAIN_HAND;
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
		if (stack.isEmpty()) {
			stack = player.getHeldItem(anim.getHand());
		}
		ItemStack heldStack = player.getHeldItem(anim.getHand());
		float equip, renderEquip;
		if (anim.getHand() == EnumHand.MAIN_HAND && anim.getSlot() == player.inventory.currentItem && heldStack.isEmpty() || anim.doItemsMatch(stack, heldStack)) {
			equip = 1 - anim.getTransform(delta);
			renderEquip = 0;
		} else {
			equip = 1 - Mth.lerp(pep, ep, delta);
			renderEquip = ep == 1 ? 0 : 1;
		}
		GlStateManager.pushMatrix();
		float yaw = Mth.lerp(player.prevRotationYaw, player.rotationYaw, delta);
		if (equip == 0) {
			anim.transform(GLMatrix.INSTANCE, mc, world, player, yaw, isLeft, delta);
		} else {
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.pushMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			interpolateWorldToHeld(world, player, stack, yaw, isLeft, isMainHand, anim::transform, equip, renderEquip, delta);
		}
		mc.getRenderItem().renderItem(stack, player, TransformType.NONE, false);
		GlStateManager.popMatrix();
		if (equip != 0) {
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}
	}

	private void interpolateWorldToHeld(World world, EntityPlayerSP player, ItemStack stack, float yaw, boolean isLeft, boolean isMainHand, ModelViewTransform worldTransform, float t, float regularEquip, float delta) {
		Matrix matrix = new Matrix();
		matrix.loadIdentity();
		matrix.mul(Mth.getMatrix(GL11.GL_MODELVIEW_MATRIX));
		worldTransform.transform(matrix, mc, world, player, yaw, isLeft, delta);
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
		Pair<? extends IBakedModel, Matrix4f> pair = model.handlePerspective(transform);
		if (pair.getRight() != null) {
			Matrix4d mat = new Matrix4d(pair.getRight());
			if (isLeft) {
				mat.mul(FLIP_X, mat);
				mat.mul(mat, FLIP_X);
			}
			matrix.mul(mat);
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

	@FunctionalInterface
	private interface ModelViewTransform {
		void transform(MatrixStack matrix, Minecraft mc, World world, EntityPlayer player, float yaw, boolean isLeft, float delta);
	}
}
