package com.pau101.auginter.client.interaction.math;

import javax.vecmath.Matrix4d;

import net.minecraft.client.renderer.GlStateManager;

public final class GLMatrix implements MatrixStack {
	public static final GLMatrix INSTANCE = new GLMatrix();

	private GLMatrix() {}

	@Override
	public void push() {
		GlStateManager.pushMatrix();
	}

	@Override
	public void pop() {
		GlStateManager.popMatrix();
	}

	@Override
	public void translate(double x, double y, double z) {
		GlStateManager.translate(x, y, z);
	}

	@Override
	public void rotate(double angle, double x, double y, double z) {
		GlStateManager.rotate((float) angle, (float) x, (float) y, (float) z);
	}

	@Override
	public void scale(double x, double y, double z) {
		GlStateManager.scale(x, y, z);
	}

	@Override
	public void mul(Matrix4d matrix) {
		Mth.multMatrix(matrix);
	}

	@Override
	public void loadIdentity() {
		GlStateManager.loadIdentity();
	}

}
