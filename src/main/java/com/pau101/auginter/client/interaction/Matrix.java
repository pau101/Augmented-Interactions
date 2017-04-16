package com.pau101.auginter.client.interaction;

import java.util.Stack;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public final class Matrix implements MatrixStack {
	private final Stack<Matrix4d> matrixStack;

	public Matrix() {
		matrixStack = new Stack<Matrix4d>();
		Matrix4d mat = new Matrix4d();
		mat.setIdentity();
		matrixStack.push(mat);
	}

	private Matrix4d getMatrix() {
		return new Matrix4d();
	}

	private Vector3d getVector(double x, double y, double z) {
		return new Vector3d(x, y, z);
	}

	private AxisAngle4d getAxisAngle(double x, double y, double z, double angle) {
		return new AxisAngle4d(x, y, z, angle);
	}

	@Override
	public void push() {
		Matrix4d mat = getMatrix();
		mat.set(matrixStack.peek());
		matrixStack.push(mat);
	}

	@Override
	public void pop() {
		if (matrixStack.size() < 2) {
			throw new RuntimeException("StackUnderflow");
		}
		matrixStack.pop();
	}

	@Override
	public void translate(double x, double y, double z) {
		Matrix4d mat = matrixStack.peek();
		Matrix4d translation = getMatrix();
		translation.setIdentity();
		Vector3d vector = getVector(x, y, z);
		translation.setTranslation(vector);
		mat.mul(translation);
	}

	@Override
	public void rotate(double angle, double x, double y, double z) {
		Matrix4d mat = matrixStack.peek();
		Matrix4d rotation = getMatrix();
		rotation.setIdentity();
		AxisAngle4d axisAngle = getAxisAngle(x, y, z, Math.toRadians(angle));
		rotation.setRotation(axisAngle);
		mat.mul(rotation);
	}

	@Override
	public void scale(double x, double y, double z) {
		Matrix4d mat = matrixStack.peek();
		Matrix4d scale = getMatrix();
		scale.m00 = x;
		scale.m11 = y;
		scale.m22 = z;
		scale.m33 = 1;
		mat.mul(scale);
	}

	@Override
	public void mul(Matrix4d other) {
		matrixStack.peek().mul(other);
	}

	@Override
	public void loadIdentity() {
		matrixStack.peek().setIdentity();
	}

	public void rotate(Quat4d quat) {
		Matrix4d mat = matrixStack.peek();
		Matrix4d rotation = getMatrix();
		rotation.set(quat);
		mat.mul(rotation);
	}

	public void perspective(double fovy, double aspect, double zNear, double zFar) {
		double radians = Math.toRadians(fovy / 2);
		double deltaZ = zFar - zNear;
		double sine = Math.sin(radians);
		if (deltaZ == 0 || sine == 0 || aspect == 0) {
			return;
		}
		double cotangent = Math.cos(radians) / sine;
		Matrix4d mat = matrixStack.peek();
		Matrix4d perspective = getMatrix();
		perspective.m00 = cotangent / aspect;
		perspective.m11 = cotangent;
		perspective.m22 = -(zFar + zNear) / deltaZ;
		perspective.m32 = -1;
		perspective.m23 = -2 * zNear * zFar / deltaZ;
		mat.mul(perspective);
	}

	public void transform(Point3f point) {
		Matrix4d mat = matrixStack.peek();
		mat.transform(point);
	}

	public void transform(Vector3f point) {
		Matrix4d mat = matrixStack.peek();
		mat.transform(point);
	}

	public Point3f getTranslation() {
		Matrix4d mat = matrixStack.peek();
		Point3f translation = new Point3f();
		mat.transform(translation);
		return translation;
	}

	public Quat4f getRotation() {
		Matrix4d mat = matrixStack.peek();
		Quat4f rotation = new Quat4f();
		mat.get(rotation);
		return rotation;
	}

	public Matrix4d getTransform() {
		return new Matrix4d(matrixStack.peek());
	}

	public void getTransform(Matrix4d mat) {
		mat.set(matrixStack.peek());
	}
}
