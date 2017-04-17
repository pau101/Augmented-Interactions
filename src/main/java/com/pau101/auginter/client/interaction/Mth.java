package com.pau101.auginter.client.interaction;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public final class Mth {
	public static final float PI = (float) Math.PI;

	public static final float TAU = PI * 2;

	private static final int MATRIX_SIZE = 4;

	private static final FloatBuffer MATRIX_BUF = BufferUtils.createFloatBuffer(MATRIX_SIZE * MATRIX_SIZE);

	private Mth() {}

	public static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	public static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	public static Matrix4d lerp(Matrix4d a, Matrix4d b, double t) {
		Quat4d aRot = new Quat4d();
		Quat4d bRot = new Quat4d();
		MatrixUtil.getQuat(a, aRot);
		MatrixUtil.getQuat(b, bRot);
		Vector3d aTrans = new Vector3d();
		Vector3d bTrans = new Vector3d();
		a.get(aTrans);
		b.get(bTrans);
		Vector3d aScale = MatrixUtil.getScale(a);
		Vector3d bScale = MatrixUtil.getScale(b);
		aRot.interpolate(bRot, t);
		aTrans.interpolate(bTrans, t);
		aScale.interpolate(bScale, t);
		Matrix4d mat = new Matrix4d();
		mat.set(aTrans);
		Matrix4d scratch = new Matrix4d();
		scratch.set(aRot);
		mat.mul(scratch);
		scratch.setZero();
		scratch.m00 = aScale.x;
		scratch.m11 = aScale.y;
		scratch.m22 = aScale.z;
		scratch.m33 = 1;
		mat.mul(scratch);
		return mat;
	}

	public static Matrix4d lerpPerspective(Matrix4d a, Matrix4d b, double t) {
		double aFov = Math.asin(1 / Math.sqrt(a.m11 * a.m11 + 1));
		double bFov = Math.asin(1 / Math.sqrt(b.m11 * b.m11 + 1));
		double aAspect = a.m11 / a.m00;
		double bAspect = b.m11 / b.m00;
		double aZNear = a.m23 / (a.m22 - 1);
		double aZFar = a.m23 / (a.m22 + 1);
		double bZFar = b.m23 / (b.m22 + 1);
		double bZNear = b.m23 / (b.m22 - 1);
		double fov = Mth.lerp(aFov, bFov, t);
		double aspect = Mth.lerp(aAspect, bAspect, t);
		double zNear = Mth.lerp(aZNear, bZNear, t);
		double zFar = Mth.lerp(bZFar, bZFar, t);
		double deltaZ = zFar - zNear;
		double cotangent = 1 / Math.tan(fov);
		Matrix4d mat = new Matrix4d();
		mat.m00 = cotangent / aspect;
		mat.m11 = cotangent;
		mat.m22 = -(zFar + zNear) / deltaZ;
		mat.m32 = -1;
		mat.m23 = -2 * zNear * zFar / deltaZ;
		return mat;
	}

	public static void multMatrix(Matrix4d mat) {
		for (int i = 0; i < MATRIX_SIZE * MATRIX_SIZE; i++) {
			MATRIX_BUF.put(i, (float) mat.getElement(i % MATRIX_SIZE, i / MATRIX_SIZE));
		}
		GlStateManager.multMatrix(MATRIX_BUF);
	}

	public static Matrix4d getMatrix(int matrix) {
		GL11.glGetFloat(matrix, MATRIX_BUF);
		Matrix4d mat = new Matrix4d();
		for (int i = 0; i < 16; i++) {
			mat.setElement(i % 4, i / 4, MATRIX_BUF.get(i));
		}
		return mat;
	}

	public static Quat4d getQuat(float x, float y, float z) {
		float rx = (float) Math.toRadians(x);
		float ry = (float) Math.toRadians(y);
		float rz = (float) Math.toRadians(z);
		float xs = MathHelper.sin(rx / 2);
		float xc = MathHelper.cos(rx / 2);
		float ys = MathHelper.sin(ry / 2);
		float yc = MathHelper.cos(ry / 2);
		float zs = MathHelper.sin(rz / 2);
		float zc = MathHelper.cos(rz / 2);
		return new Quat4d(xs * yc * zc + xc * ys * zs, xc * ys * zc - xs * yc * zs, xs * ys * zc + xc * yc * zs, xc * yc * zc - xs * ys * zs);
	}
}
