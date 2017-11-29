package net.amygdalum.testrecorder.asm;

import static net.amygdalum.testrecorder.util.ByteCode.isStatic;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class GetThis extends AbstractInsnListBuilder {

	private MethodNode methodNode;

	public GetThis(MethodNode methodNode) {
		this.methodNode = methodNode;
	}

	@Override
	public InsnList build() {
		return isStatic(methodNode)
			? list(new InsnNode(ACONST_NULL))
			: list(new VarInsnNode(ALOAD, 0));
	}

}
