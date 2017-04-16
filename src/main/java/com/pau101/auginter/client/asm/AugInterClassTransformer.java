package com.pau101.auginter.client.asm;

import java.util.function.BiFunction;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class AugInterClassTransformer implements IClassTransformer {
	private static final String MINECRAFT = "net.minecraft.client.Minecraft";

	private static final String MINECRAFT_OBF = "bes";

	private static final String RIGHT_CLICK_MOUSE_NAME = "rightClickMouse";

	private static final String RIGHT_CLICK_MOUSE_NAME_OBF = "ax";

	private static final String RIGHT_CLICK_MOUSE_DESC = "()V";

	private static final String AUGINTER = "com/pau101/auginter/AugmentedInteractions";

	private static final String AUGINTER_RIGHT_CLICK_MOUSE = "rightClickMouse";

	private static final String AUGINTER_RIGHT_CLICK_MOUSE_DESC = "(Lnet/minecraft/util/EnumHand;)Z";

	private static final String AUGINTER_RIGHT_CLICK_MOUSE_DESC_OBF = "(Lri;)Z";

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		boolean obf;
		if ((obf = MINECRAFT_OBF.equals(name)) || MINECRAFT.equals(name)) {
			return transform(bytes, obf, this::transformMinecraft);
		}
		return bytes;
	}

	private byte[] transform(byte[] bytes, boolean obf, BiFunction<ClassNode, Boolean, ClassNode> transformer) {
		return writeClass(transformer.apply(readClass(bytes), obf));
	}

	private ClassNode readClass(byte[] bytes) {
		ClassNode cls = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(cls, 0);
		return cls;
	}

	private byte[] writeClass(ClassNode cls) {
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cls.accept(writer);
		return writer.toByteArray();
	}

	private ClassNode transformMinecraft(ClassNode cls, boolean obf) {
		String name = obf ? RIGHT_CLICK_MOUSE_NAME_OBF : RIGHT_CLICK_MOUSE_NAME;
		boolean foundMethod = false, failed = true;
		for (MethodNode method : cls.methods) {
			if (name.equals(method.name) && RIGHT_CLICK_MOUSE_DESC.equals(method.desc)) {
				InsnList insns = method.instructions;
				for (int i = 0; i < insns.size(); i++) {
					AbstractInsnNode node = insns.get(i);
					if (node.getOpcode() == Opcodes.IFNULL) {
						InsnList call = new InsnList();
						call.add(new VarInsnNode(Opcodes.ALOAD, 4));
						call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, AUGINTER, AUGINTER_RIGHT_CLICK_MOUSE, AUGINTER_RIGHT_CLICK_MOUSE_DESC, false));
						LabelNode label = new LabelNode(new Label());
						call.add(new JumpInsnNode(Opcodes.IFEQ, label));
						call.add(new InsnNode(Opcodes.RETURN));
						call.add(label);
						insns.insert(node, call);
						failed = false;
						break;
					}
				}
				foundMethod = true;
				break;
			}
		}
		if (failed) {
			throw new RuntimeException("Failed to transform Minecraft#rightClickMouse because it could not find " + (foundMethod ? "opcode" : "method"));
		}
		return cls;
	}
}
