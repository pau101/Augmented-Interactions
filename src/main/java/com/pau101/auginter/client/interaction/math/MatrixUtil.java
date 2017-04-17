package com.pau101.auginter.client.interaction.math;

import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/*
 * Copy vecmath 1.5.2 SVD source attachment here since it does not match 1.5.2 binaries that contain a bug
 * which produces NaN, but also the source matrix3 to quaternion was still wrong so it was been replaced.
 */
public final class MatrixUtil {
	private static final double EPS = 1E-10;

	private MatrixUtil() {}

	public static Vector3d getScale(Matrix4d mat) {
		double[] scale = new double[3];
		getScaleRotate(mat, scale, new double[9]);
		return new Vector3d(scale);
	}

	// http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/
	public static void getQuat(Matrix4d mat, Quat4d q1) {
		double[] tmp_rot = new double[9];
		double[] tmp_scale = new double[3];
		getScaleRotate(mat, tmp_scale, tmp_rot);
		double m00 = tmp_rot[0];
		double m01 = tmp_rot[1];
		double m02 = tmp_rot[2];
		double m10 = tmp_rot[3];
		double m11 = tmp_rot[4];
		double m12 = tmp_rot[5];
		double m20 = tmp_rot[6];
		double m21 = tmp_rot[7];
		double m22 = tmp_rot[8];
		double tr = m00 + m11 + m22;
		if (tr > 0) {
			double s = Math.sqrt(1 + tr) * 2;
			q1.w = 0.25 * s;
			q1.x = (m21 - m12) / s;
			q1.y = (m02 - m20) / s;
			q1.z = (m10 - m01) / s;
		} else if ((m00 > m11) && (m00 > m22)) {
			double s = Math.sqrt(1 + m00 - m11 - m22) * 2;
			q1.w = (m21 - m12) / s;
			q1.x = 0.25 * s;
			q1.y = (m01 + m10) / s;
			q1.z = (m02 + m20) / s;
		} else if (m11 > m22) {
			double s = Math.sqrt(1 + m11 - m00 - m22) * 2;
			q1.w = (m02 - m20) / s;
			q1.x = (m01 + m10) / s;
			q1.y = 0.25 * s;
			q1.z = (m12 + m21) / s;
		} else {
			double s = Math.sqrt(1 + m22 - m00 - m11) * 2;
			q1.w = (m10 - m01) / s;
			q1.x = (m02 + m20) / s;
			q1.y = (m12 + m21) / s;
			q1.z = 0.25 * s;
		}
	}

	private static void getScaleRotate(Matrix4d mat, double scales[], double rots[]) {
		double[] tmp = new double[9]; // scratch matrix
		tmp[0] = mat.m00;
		tmp[1] = mat.m01;
		tmp[2] = mat.m02;
		tmp[3] = mat.m10;
		tmp[4] = mat.m11;
		tmp[5] = mat.m12;
		tmp[6] = mat.m20;
		tmp[7] = mat.m21;
		tmp[8] = mat.m22;
		compute_svd(tmp, scales, rots);
		return;
	}

	private static void compute_svd(double[] m, double[] outScale, double[] outRot) {
		int i, j;
		double g, scale;
		double[] u1 = new double[9];
		double[] v1 = new double[9];
		double[] t1 = new double[9];
		double[] t2 = new double[9];
		double[] tmp = t1;
		double[] single_values = t2;
		double[] rot = new double[9];
		double[] e = new double[3];
		double[] scales = new double[3];
		int converged, negCnt = 0;
		double cs, sn;
		double c1, c2, c3, c4;
		double s1, s2, s3, s4;
		double cl1, cl2, cl3;
		for (i = 0; i < 9; i++) {
			rot[i] = m[i];
		}
		// u1
		if (m[3] * m[3] < EPS) {
			u1[0] = 1;
			u1[1] = 0;
			u1[2] = 0;
			u1[3] = 0;
			u1[4] = 1;
			u1[5] = 0;
			u1[6] = 0;
			u1[7] = 0;
			u1[8] = 1;
		} else if (m[0] * m[0] < EPS) {
			tmp[0] = m[0];
			tmp[1] = m[1];
			tmp[2] = m[2];
			m[0] = m[3];
			m[1] = m[4];
			m[2] = m[5];
			m[3] = -tmp[0]; // zero
			m[4] = -tmp[1];
			m[5] = -tmp[2];
			u1[0] = 0;
			u1[1] = 1;
			u1[2] = 0;
			u1[3] = -1;
			u1[4] = 0;
			u1[5] = 0;
			u1[6] = 0;
			u1[7] = 0;
			u1[8] = 1;
		} else {
			g = 1 / Math.sqrt(m[0] * m[0] + m[3] * m[3]);
			c1 = m[0] * g;
			s1 = m[3] * g;
			tmp[0] = c1 * m[0] + s1 * m[3];
			tmp[1] = c1 * m[1] + s1 * m[4];
			tmp[2] = c1 * m[2] + s1 * m[5];
			m[3] = -s1 * m[0] + c1 * m[3]; // zero
			m[4] = -s1 * m[1] + c1 * m[4];
			m[5] = -s1 * m[2] + c1 * m[5];
			m[0] = tmp[0];
			m[1] = tmp[1];
			m[2] = tmp[2];
			u1[0] = c1;
			u1[1] = s1;
			u1[2] = 0;
			u1[3] = -s1;
			u1[4] = c1;
			u1[5] = 0;
			u1[6] = 0;
			u1[7] = 0;
			u1[8] = 1;
		}
		// u2
		if (m[6] * m[6] < EPS) {} else if (m[0] * m[0] < EPS) {
			tmp[0] = m[0];
			tmp[1] = m[1];
			tmp[2] = m[2];
			m[0] = m[6];
			m[1] = m[7];
			m[2] = m[8];
			m[6] = -tmp[0]; // zero
			m[7] = -tmp[1];
			m[8] = -tmp[2];
			tmp[0] = u1[0];
			tmp[1] = u1[1];
			tmp[2] = u1[2];
			u1[0] = u1[6];
			u1[1] = u1[7];
			u1[2] = u1[8];
			u1[6] = -tmp[0]; // zero
			u1[7] = -tmp[1];
			u1[8] = -tmp[2];
		} else {
			g = 1 / Math.sqrt(m[0] * m[0] + m[6] * m[6]);
			c2 = m[0] * g;
			s2 = m[6] * g;
			tmp[0] = c2 * m[0] + s2 * m[6];
			tmp[1] = c2 * m[1] + s2 * m[7];
			tmp[2] = c2 * m[2] + s2 * m[8];
			m[6] = -s2 * m[0] + c2 * m[6];
			m[7] = -s2 * m[1] + c2 * m[7];
			m[8] = -s2 * m[2] + c2 * m[8];
			m[0] = tmp[0];
			m[1] = tmp[1];
			m[2] = tmp[2];
			tmp[0] = c2 * u1[0];
			tmp[1] = c2 * u1[1];
			u1[2] = s2;
			tmp[6] = -u1[0] * s2;
			tmp[7] = -u1[1] * s2;
			u1[8] = c2;
			u1[0] = tmp[0];
			u1[1] = tmp[1];
			u1[6] = tmp[6];
			u1[7] = tmp[7];
		}
		// v1
		if (m[2] * m[2] < EPS) {
			v1[0] = 1;
			v1[1] = 0;
			v1[2] = 0;
			v1[3] = 0;
			v1[4] = 1;
			v1[5] = 0;
			v1[6] = 0;
			v1[7] = 0;
			v1[8] = 1;
		} else if (m[1] * m[1] < EPS) {
			tmp[2] = m[2];
			tmp[5] = m[5];
			tmp[8] = m[8];
			m[2] = -m[1];
			m[5] = -m[4];
			m[8] = -m[7];
			m[1] = tmp[2]; // zero
			m[4] = tmp[5];
			m[7] = tmp[8];
			v1[0] = 1;
			v1[1] = 0;
			v1[2] = 0;
			v1[3] = 0;
			v1[4] = 0;
			v1[5] = -1;
			v1[6] = 0;
			v1[7] = 1;
			v1[8] = 0;
		} else {
			g = 1 / Math.sqrt(m[1] * m[1] + m[2] * m[2]);
			c3 = m[1] * g;
			s3 = m[2] * g;
			tmp[1] = c3 * m[1] + s3 * m[2]; // can assign to m[1]?
			m[2] = -s3 * m[1] + c3 * m[2]; // zero
			m[1] = tmp[1];
			tmp[4] = c3 * m[4] + s3 * m[5];
			m[5] = -s3 * m[4] + c3 * m[5];
			m[4] = tmp[4];
			tmp[7] = c3 * m[7] + s3 * m[8];
			m[8] = -s3 * m[7] + c3 * m[8];
			m[7] = tmp[7];
			v1[0] = 1;
			v1[1] = 0;
			v1[2] = 0;
			v1[3] = 0;
			v1[4] = c3;
			v1[5] = -s3;
			v1[6] = 0;
			v1[7] = s3;
			v1[8] = c3;
		}
		// u3
		if (m[7] * m[7] < EPS) {} else if (m[4] * m[4] < EPS) {
			tmp[3] = m[3];
			tmp[4] = m[4];
			tmp[5] = m[5];
			m[3] = m[6]; // zero
			m[4] = m[7];
			m[5] = m[8];
			m[6] = -tmp[3]; // zero
			m[7] = -tmp[4]; // zero
			m[8] = -tmp[5];
			tmp[3] = u1[3];
			tmp[4] = u1[4];
			tmp[5] = u1[5];
			u1[3] = u1[6];
			u1[4] = u1[7];
			u1[5] = u1[8];
			u1[6] = -tmp[3]; // zero
			u1[7] = -tmp[4];
			u1[8] = -tmp[5];
		} else {
			g = 1 / Math.sqrt(m[4] * m[4] + m[7] * m[7]);
			c4 = m[4] * g;
			s4 = m[7] * g;
			tmp[3] = c4 * m[3] + s4 * m[6];
			m[6] = -s4 * m[3] + c4 * m[6]; // zero
			m[3] = tmp[3];
			tmp[4] = c4 * m[4] + s4 * m[7];
			m[7] = -s4 * m[4] + c4 * m[7];
			m[4] = tmp[4];
			tmp[5] = c4 * m[5] + s4 * m[8];
			m[8] = -s4 * m[5] + c4 * m[8];
			m[5] = tmp[5];
			tmp[3] = c4 * u1[3] + s4 * u1[6];
			u1[6] = -s4 * u1[3] + c4 * u1[6];
			u1[3] = tmp[3];
			tmp[4] = c4 * u1[4] + s4 * u1[7];
			u1[7] = -s4 * u1[4] + c4 * u1[7];
			u1[4] = tmp[4];
			tmp[5] = c4 * u1[5] + s4 * u1[8];
			u1[8] = -s4 * u1[5] + c4 * u1[8];
			u1[5] = tmp[5];
		}
		single_values[0] = m[0];
		single_values[1] = m[4];
		single_values[2] = m[8];
		e[0] = m[1];
		e[1] = m[5];
		if (e[0] * e[0] < EPS && e[1] * e[1] < EPS) {} else {
			compute_qr(single_values, e, u1, v1);
		}
		scales[0] = single_values[0];
		scales[1] = single_values[1];
		scales[2] = single_values[2];
		// Do some optimization here. If scale is unity, simply return the rotation matric.
		if (almostEqual(Math.abs(scales[0]), 1) && almostEqual(Math.abs(scales[1]), 1) && almostEqual(Math.abs(scales[2]), 1)) {
			// System.out.println("Scale components almost to 1");
			for (i = 0; i < 3; i++) {
				if (scales[i] < 0) {
					negCnt++;
				}
			}
			if (negCnt == 0 || negCnt == 2) {
				// System.out.println("Optimize!!");
				outScale[0] = outScale[1] = outScale[2] = 1;
				for (i = 0; i < 9; i++) {
					outRot[i] = rot[i];
				}
				return;
			}
		}
		transpose_mat(u1, t1);
		transpose_mat(v1, t2);
		svdReorder(m, t1, t2, scales, outRot, outScale);
	}

	private static final boolean almostEqual(double a, double b) {
		if (a == b) {
			return true;
		}
		final double EPSILON_ABSOLUTE = 1e-6;
		final double EPSILON_RELATIVE = 1e-4;
		double diff = Math.abs(a - b);
		double absA = Math.abs(a);
		double absB = Math.abs(b);
		double max = absA >= absB ? absA : absB;
		if (diff < EPSILON_ABSOLUTE) {
			return true;
		}
		if (diff / max < EPSILON_RELATIVE) {
			return true;
		}
		return false;
	}

	private static void transpose_mat(double[] in, double[] out) {
		out[0] = in[0];
		out[1] = in[3];
		out[2] = in[6];
		out[3] = in[1];
		out[4] = in[4];
		out[5] = in[7];
		out[6] = in[2];
		out[7] = in[5];
		out[8] = in[8];
	}

	private static void svdReorder(double[] m, double[] t1, double[] t2, double[] scales, double[] outRot, double[] outScale) {
		int[] out = new int[3];
		int[] in = new int[3];
		int in0, in1, in2, index, i;
		double[] mag = new double[3];
		double[] rot = new double[9];
		// check for rotation information in the scales
		if (scales[0] < 0) { // move the rotation info to rotation matrix
			scales[0] = -scales[0];
			t2[0] = -t2[0];
			t2[1] = -t2[1];
			t2[2] = -t2[2];
		}
		if (scales[1] < 0) { // move the rotation info to rotation matrix
			scales[1] = -scales[1];
			t2[3] = -t2[3];
			t2[4] = -t2[4];
			t2[5] = -t2[5];
		}
		if (scales[2] < 0) { // move the rotation info to rotation matrix
			scales[2] = -scales[2];
			t2[6] = -t2[6];
			t2[7] = -t2[7];
			t2[8] = -t2[8];
		}
		mat_mul(t1, t2, rot);
		// check for equal scales case and do not reorder
		if (almostEqual(Math.abs(scales[0]), Math.abs(scales[1])) && almostEqual(Math.abs(scales[1]), Math.abs(scales[2]))) {
			for (i = 0; i < 9; i++) {
				outRot[i] = rot[i];
			}
			for (i = 0; i < 3; i++) {
				outScale[i] = scales[i];
			}
		} else {
			// sort the order of the results of SVD
			if (scales[0] > scales[1]) {
				if (scales[0] > scales[2]) {
					if (scales[2] > scales[1]) {
						out[0] = 0;
						out[1] = 2;
						out[2] = 1; // xzy
					} else {
						out[0] = 0;
						out[1] = 1;
						out[2] = 2; // xyz
					}
				} else {
					out[0] = 2;
					out[1] = 0;
					out[2] = 1; // zxy
				}
			} else { // y > x
				if (scales[1] > scales[2]) {
					if (scales[2] > scales[0]) {
						out[0] = 1;
						out[1] = 2;
						out[2] = 0; // yzx
					} else {
						out[0] = 1;
						out[1] = 0;
						out[2] = 2; // yxz
					}
				} else {
					out[0] = 2;
					out[1] = 1;
					out[2] = 0; // zyx
				}
			}
			// sort the order of the input matrix
			mag[0] = m[0] * m[0] + m[1] * m[1] + m[2] * m[2];
			mag[1] = m[3] * m[3] + m[4] * m[4] + m[5] * m[5];
			mag[2] = m[6] * m[6] + m[7] * m[7] + m[8] * m[8];
			if (mag[0] > mag[1]) {
				if (mag[0] > mag[2]) {
					if (mag[2] > mag[1]) {
						// 0 - 2 - 1
						in0 = 0;
						in2 = 1;
						in1 = 2;// xzy
					} else {
						// 0 - 1 - 2
						in0 = 0;
						in1 = 1;
						in2 = 2; // xyz
					}
				} else {
					// 2 - 0 - 1
					in2 = 0;
					in0 = 1;
					in1 = 2; // zxy
				}
			} else { // y > x 1>0
				if (mag[1] > mag[2]) {
					if (mag[2] > mag[0]) {
						// 1 - 2 - 0
						in1 = 0;
						in2 = 1;
						in0 = 2; // yzx
					} else {
						// 1 - 0 - 2
						in1 = 0;
						in0 = 1;
						in2 = 2; // yxz
					}
				} else {
					// 2 - 1 - 0
					in2 = 0;
					in1 = 1;
					in0 = 2; // zyx
				}
			}
			index = out[in0];
			outScale[0] = scales[index];
			index = out[in1];
			outScale[1] = scales[index];
			index = out[in2];
			outScale[2] = scales[index];
			index = out[in0];
			outRot[0] = rot[index];
			index = out[in0] + 3;
			outRot[0 + 3] = rot[index];
			index = out[in0] + 6;
			outRot[0 + 6] = rot[index];
			index = out[in1];
			outRot[1] = rot[index];
			index = out[in1] + 3;
			outRot[1 + 3] = rot[index];
			index = out[in1] + 6;
			outRot[1 + 6] = rot[index];
			index = out[in2];
			outRot[2] = rot[index];
			index = out[in2] + 3;
			outRot[2 + 3] = rot[index];
			index = out[in2] + 6;
			outRot[2 + 6] = rot[index];
		}
	}

	private static void mat_mul(double[] m1, double[] m2, double[] m3) {
		int i;
		double[] tmp = new double[9];
		tmp[0] = m1[0] * m2[0] + m1[1] * m2[3] + m1[2] * m2[6];
		tmp[1] = m1[0] * m2[1] + m1[1] * m2[4] + m1[2] * m2[7];
		tmp[2] = m1[0] * m2[2] + m1[1] * m2[5] + m1[2] * m2[8];
		tmp[3] = m1[3] * m2[0] + m1[4] * m2[3] + m1[5] * m2[6];
		tmp[4] = m1[3] * m2[1] + m1[4] * m2[4] + m1[5] * m2[7];
		tmp[5] = m1[3] * m2[2] + m1[4] * m2[5] + m1[5] * m2[8];
		tmp[6] = m1[6] * m2[0] + m1[7] * m2[3] + m1[8] * m2[6];
		tmp[7] = m1[6] * m2[1] + m1[7] * m2[4] + m1[8] * m2[7];
		tmp[8] = m1[6] * m2[2] + m1[7] * m2[5] + m1[8] * m2[8];
		for (i = 0; i < 9; i++) {
			m3[i] = tmp[i];
		}
	}

	private static int compute_qr(double[] s, double[] e, double[] u, double[] v) {
		int i, j, k;
		boolean converged;
		double shift, ssmin, ssmax, r;
		double[] cosl = new double[2];
		double[] cosr = new double[2];
		double[] sinl = new double[2];
		double[] sinr = new double[2];
		double[] m = new double[9];
		double utemp, vtemp;
		double f, g;
		final int MAX_INTERATIONS = 10;
		final double CONVERGE_TOL = 4.89E-15;
		double c_b48 = 1;
		double c_b71 = -1;
		int first;
		converged = false;
		first = 1;
		if (Math.abs(e[1]) < CONVERGE_TOL || Math.abs(e[0]) < CONVERGE_TOL) {
			converged = true;
		}
		for (k = 0; k < MAX_INTERATIONS && !converged; k++) {
			shift = compute_shift(s[1], e[1], s[2]);
			f = (Math.abs(s[0]) - shift) * (d_sign(c_b48, s[0]) + shift / s[0]);
			g = e[0];
			r = compute_rot(f, g, sinr, cosr, 0, first);
			f = cosr[0] * s[0] + sinr[0] * e[0];
			e[0] = cosr[0] * e[0] - sinr[0] * s[0];
			g = sinr[0] * s[1];
			s[1] = cosr[0] * s[1];
			r = compute_rot(f, g, sinl, cosl, 0, first);
			first = 0;
			s[0] = r;
			f = cosl[0] * e[0] + sinl[0] * s[1];
			s[1] = cosl[0] * s[1] - sinl[0] * e[0];
			g = sinl[0] * e[1];
			e[1] = cosl[0] * e[1];
			r = compute_rot(f, g, sinr, cosr, 1, first);
			e[0] = r;
			f = cosr[1] * s[1] + sinr[1] * e[1];
			e[1] = cosr[1] * e[1] - sinr[1] * s[1];
			g = sinr[1] * s[2];
			s[2] = cosr[1] * s[2];
			r = compute_rot(f, g, sinl, cosl, 1, first);
			s[1] = r;
			f = cosl[1] * e[1] + sinl[1] * s[2];
			s[2] = cosl[1] * s[2] - sinl[1] * e[1];
			e[1] = f;
			// update u matrices
			utemp = u[0];
			u[0] = cosl[0] * utemp + sinl[0] * u[3];
			u[3] = -sinl[0] * utemp + cosl[0] * u[3];
			utemp = u[1];
			u[1] = cosl[0] * utemp + sinl[0] * u[4];
			u[4] = -sinl[0] * utemp + cosl[0] * u[4];
			utemp = u[2];
			u[2] = cosl[0] * utemp + sinl[0] * u[5];
			u[5] = -sinl[0] * utemp + cosl[0] * u[5];
			utemp = u[3];
			u[3] = cosl[1] * utemp + sinl[1] * u[6];
			u[6] = -sinl[1] * utemp + cosl[1] * u[6];
			utemp = u[4];
			u[4] = cosl[1] * utemp + sinl[1] * u[7];
			u[7] = -sinl[1] * utemp + cosl[1] * u[7];
			utemp = u[5];
			u[5] = cosl[1] * utemp + sinl[1] * u[8];
			u[8] = -sinl[1] * utemp + cosl[1] * u[8];
			// update v matrices
			vtemp = v[0];
			v[0] = cosr[0] * vtemp + sinr[0] * v[1];
			v[1] = -sinr[0] * vtemp + cosr[0] * v[1];
			vtemp = v[3];
			v[3] = cosr[0] * vtemp + sinr[0] * v[4];
			v[4] = -sinr[0] * vtemp + cosr[0] * v[4];
			vtemp = v[6];
			v[6] = cosr[0] * vtemp + sinr[0] * v[7];
			v[7] = -sinr[0] * vtemp + cosr[0] * v[7];
			vtemp = v[1];
			v[1] = cosr[1] * vtemp + sinr[1] * v[2];
			v[2] = -sinr[1] * vtemp + cosr[1] * v[2];
			vtemp = v[4];
			v[4] = cosr[1] * vtemp + sinr[1] * v[5];
			v[5] = -sinr[1] * vtemp + cosr[1] * v[5];
			vtemp = v[7];
			v[7] = cosr[1] * vtemp + sinr[1] * v[8];
			v[8] = -sinr[1] * vtemp + cosr[1] * v[8];
			m[0] = s[0];
			m[1] = e[0];
			m[2] = 0;
			m[3] = 0;
			m[4] = s[1];
			m[5] = e[1];
			m[6] = 0;
			m[7] = 0;
			m[8] = s[2];
			if (Math.abs(e[1]) < CONVERGE_TOL || Math.abs(e[0]) < CONVERGE_TOL) {
				converged = true;
			}
		}
		if (Math.abs(e[1]) < CONVERGE_TOL) {
			compute_2X2(s[0], e[0], s[1], s, sinl, cosl, sinr, cosr, 0);
			utemp = u[0];
			u[0] = cosl[0] * utemp + sinl[0] * u[3];
			u[3] = -sinl[0] * utemp + cosl[0] * u[3];
			utemp = u[1];
			u[1] = cosl[0] * utemp + sinl[0] * u[4];
			u[4] = -sinl[0] * utemp + cosl[0] * u[4];
			utemp = u[2];
			u[2] = cosl[0] * utemp + sinl[0] * u[5];
			u[5] = -sinl[0] * utemp + cosl[0] * u[5];
			// update v matrices
			vtemp = v[0];
			v[0] = cosr[0] * vtemp + sinr[0] * v[1];
			v[1] = -sinr[0] * vtemp + cosr[0] * v[1];
			vtemp = v[3];
			v[3] = cosr[0] * vtemp + sinr[0] * v[4];
			v[4] = -sinr[0] * vtemp + cosr[0] * v[4];
			vtemp = v[6];
			v[6] = cosr[0] * vtemp + sinr[0] * v[7];
			v[7] = -sinr[0] * vtemp + cosr[0] * v[7];
		} else {
			compute_2X2(s[1], e[1], s[2], s, sinl, cosl, sinr, cosr, 1);
			utemp = u[3];
			u[3] = cosl[0] * utemp + sinl[0] * u[6];
			u[6] = -sinl[0] * utemp + cosl[0] * u[6];
			utemp = u[4];
			u[4] = cosl[0] * utemp + sinl[0] * u[7];
			u[7] = -sinl[0] * utemp + cosl[0] * u[7];
			utemp = u[5];
			u[5] = cosl[0] * utemp + sinl[0] * u[8];
			u[8] = -sinl[0] * utemp + cosl[0] * u[8];
			// update v matrices
			vtemp = v[1];
			v[1] = cosr[0] * vtemp + sinr[0] * v[2];
			v[2] = -sinr[0] * vtemp + cosr[0] * v[2];
			vtemp = v[4];
			v[4] = cosr[0] * vtemp + sinr[0] * v[5];
			v[5] = -sinr[0] * vtemp + cosr[0] * v[5];
			vtemp = v[7];
			v[7] = cosr[0] * vtemp + sinr[0] * v[8];
			v[8] = -sinr[0] * vtemp + cosr[0] * v[8];
		}
		return 0;
	}

	private static double max(double a, double b) {
		return a > b ? a : b;
	}

	private static double min(double a, double b) {
		if (a < b) {
			return a;
		} else {
			return b;
		}
	}

	private static double d_sign(double a, double b) {
		double x;
		x = a >= 0 ? a : -a;
		return b >= 0 ? x : -x;
	}

	private static double compute_shift(double f, double g, double h) {
		double d__1, d__2;
		double fhmn, fhmx, c, fa, ga, ha, as, at, au;
		double ssmin;
		fa = Math.abs(f);
		ga = Math.abs(g);
		ha = Math.abs(h);
		fhmn = min(fa, ha);
		fhmx = max(fa, ha);
		if (fhmn == 0) {
			ssmin = 0;
			if (fhmx == 0) {} else {
				d__1 = min(fhmx, ga) / max(fhmx, ga);
			}
		} else {
			if (ga < fhmx) {
				as = fhmn / fhmx + 1;
				at = (fhmx - fhmn) / fhmx;
				d__1 = ga / fhmx;
				au = d__1 * d__1;
				c = 2 / (Math.sqrt(as * as + au) + Math.sqrt(at * at + au));
				ssmin = fhmn * c;
			} else {
				au = fhmx / ga;
				if (au == 0) {
					ssmin = fhmn * fhmx / ga;
				} else {
					as = fhmn / fhmx + 1;
					at = (fhmx - fhmn) / fhmx;
					d__1 = as * au;
					d__2 = at * au;
					c = 1 / (Math.sqrt(d__1 * d__1 + 1) + Math.sqrt(d__2 * d__2 + 1));
					ssmin = fhmn * c * au;
					ssmin += ssmin;
				}
			}
		}
		return ssmin;
	}

	private static int compute_2X2(double f, double g, double h, double[] single_values, double[] snl, double[] csl, double[] snr, double[] csr, int index) {
		double c_b3 = 2;
		double c_b4 = 1;
		double d__1;
		int pmax;
		double temp;
		boolean swap;
		double a, d, l, m, r, s, t, tsign, fa, ga, ha;
		double ft, gt, ht, mm;
		boolean gasmal;
		double tt, clt, crt, slt, srt;
		double ssmin, ssmax;
		ssmax = single_values[0];
		ssmin = single_values[1];
		clt = 0;
		crt = 0;
		slt = 0;
		srt = 0;
		tsign = 0;
		ft = f;
		fa = Math.abs(ft);
		ht = h;
		ha = Math.abs(h);
		pmax = 1;
		if (ha > fa) {
			swap = true;
		} else {
			swap = false;
		}
		if (swap) {
			pmax = 3;
			temp = ft;
			ft = ht;
			ht = temp;
			temp = fa;
			fa = ha;
			ha = temp;
		}
		gt = g;
		ga = Math.abs(gt);
		if (ga == 0) {
			single_values[1] = ha;
			single_values[0] = fa;
			clt = 1;
			crt = 1;
			slt = 0;
			srt = 0;
		} else {
			gasmal = true;
			if (ga > fa) {
				pmax = 2;
				if (fa / ga < EPS) {
					gasmal = false;
					ssmax = ga;
					if (ha > 1) {
						ssmin = fa / (ga / ha);
					} else {
						ssmin = fa / ga * ha;
					}
					clt = 1;
					slt = ht / gt;
					srt = 1;
					crt = ft / gt;
				}
			}
			if (gasmal) {
				d = fa - ha;
				if (d == fa) {
					l = 1;
				} else {
					l = d / fa;
				}
				m = gt / ft;
				t = 2 - l;
				mm = m * m;
				tt = t * t;
				s = Math.sqrt(tt + mm);
				if (l == 0) {
					r = Math.abs(m);
				} else {
					r = Math.sqrt(l * l + mm);
				}
				a = (s + r) * 0.5;
				if (ga > fa) {
					pmax = 2;
					if (fa / ga < EPS) {
						gasmal = false;
						ssmax = ga;
						if (ha > 1) {
							ssmin = fa / (ga / ha);
						} else {
							ssmin = fa / ga * ha;
						}
						clt = 1;
						slt = ht / gt;
						srt = 1;
						crt = ft / gt;
					}
				}
				if (gasmal) {
					d = fa - ha;
					if (d == fa) {
						l = 1;
					} else {
						l = d / fa;
					}
					m = gt / ft;
					t = 2 - l;
					mm = m * m;
					tt = t * t;
					s = Math.sqrt(tt + mm);
					if (l == 0) {
						r = Math.abs(m);
					} else {
						r = Math.sqrt(l * l + mm);
					}
					a = (s + r) * 0.5;
					ssmin = ha / a;
					ssmax = fa * a;
					if (mm == 0) {
						if (l == 0) {
							t = d_sign(c_b3, ft) * d_sign(c_b4, gt);
						} else {
							t = gt / d_sign(d, ft) + m / t;
						}
					} else {
						t = (m / (s + t) + m / (r + l)) * (a + 1);
					}
					l = Math.sqrt(t * t + 4);
					crt = 2 / l;
					srt = t / l;
					clt = (crt + srt * m) / a;
					slt = ht / ft * srt / a;
				}
			}
			if (swap) {
				csl[0] = srt;
				snl[0] = crt;
				csr[0] = slt;
				snr[0] = clt;
			} else {
				csl[0] = clt;
				snl[0] = slt;
				csr[0] = crt;
				snr[0] = srt;
			}
			if (pmax == 1) {
				tsign = d_sign(c_b4, csr[0]) * d_sign(c_b4, csl[0]) * d_sign(c_b4, f);
			}
			if (pmax == 2) {
				tsign = d_sign(c_b4, snr[0]) * d_sign(c_b4, csl[0]) * d_sign(c_b4, g);
			}
			if (pmax == 3) {
				tsign = d_sign(c_b4, snr[0]) * d_sign(c_b4, snl[0]) * d_sign(c_b4, h);
			}
			single_values[index] = d_sign(ssmax, tsign);
			d__1 = tsign * d_sign(c_b4, f) * d_sign(c_b4, h);
			single_values[index + 1] = d_sign(ssmin, d__1);
		}
		return 0;
	}

	private static double compute_rot(double f, double g, double[] sin, double[] cos, int index, int first) {
		int i__1;
		double d__1, d__2;
		double cs, sn;
		int i;
		double scale;
		int count;
		double f1, g1;
		double r;
		final double safmn2 = 2.002083095183101E-146;
		final double safmx2 = 4.994797680505588E+145;
		if (g == 0) {
			cs = 1;
			sn = 0;
			r = f;
		} else if (f == 0) {
			cs = 0;
			sn = 1;
			r = g;
		} else {
			f1 = f;
			g1 = g;
			scale = max(Math.abs(f1), Math.abs(g1));
			if (scale >= safmx2) {
				count = 0;
				while (scale >= safmx2) {
					++count;
					f1 *= safmn2;
					g1 *= safmn2;
					scale = max(Math.abs(f1), Math.abs(g1));
				}
				r = Math.sqrt(f1 * f1 + g1 * g1);
				cs = f1 / r;
				sn = g1 / r;
				i__1 = count;
				for (i = 1; i <= count; ++i) {
					r *= safmx2;
				}
			} else if (scale <= safmn2) {
				count = 0;
				while (scale <= safmn2) {
					++count;
					f1 *= safmx2;
					g1 *= safmx2;
					scale = max(Math.abs(f1), Math.abs(g1));
				}
				r = Math.sqrt(f1 * f1 + g1 * g1);
				cs = f1 / r;
				sn = g1 / r;
				i__1 = count;
				for (i = 1; i <= count; ++i) {
					r *= safmn2;
				}
			} else {
				r = Math.sqrt(f1 * f1 + g1 * g1);
				cs = f1 / r;
				sn = g1 / r;
			}
			if (Math.abs(f) > Math.abs(g) && cs < 0) {
				cs = -cs;
				sn = -sn;
				r = -r;
			}
		}
		sin[index] = sn;
		cos[index] = cs;
		return r;
	}
}
