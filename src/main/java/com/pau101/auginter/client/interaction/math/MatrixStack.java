package com.pau101.auginter.client.interaction.math;

import javax.vecmath.Matrix4d;

public interface MatrixStack {
	void push();

	void pop();

	void translate(double x, double y, double z);

	void rotate(double angle, double x, double y, double z);

	void scale(double x, double y, double z);

	void mul(Matrix4d matrix);

	void loadIdentity();
}
