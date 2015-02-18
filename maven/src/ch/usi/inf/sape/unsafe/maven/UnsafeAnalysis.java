package ch.usi.inf.sape.unsafe.maven;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class UnsafeAnalysis {

	private static class UnsafeVisitor extends ClassVisitor {

		public boolean found = false;

		public UnsafeVisitor() {
			super(Opcodes.ASM5);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {

			MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {
				@Override
				public void visitMethodInsn(int opcode, String owner,
						String name, String desc, boolean itf) {
					if (owner == "sun/misc/Unsafe") {
						found = true;
					}
				}
			};
			return mv;
		}
	}

	public static boolean search(byte[] classfile) {
		ClassReader cr = new ClassReader(classfile);
		UnsafeVisitor uv = new UnsafeVisitor();
		cr.accept(uv, 0);

		return uv.found;
	}
}
