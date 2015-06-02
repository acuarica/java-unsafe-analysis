package ch.usi.inf.sape.unsafeanalysis.analysis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;

public class UnsafeAnalysis {

	public static class UnsafeEntry {
		public final String className;
		public final String methodName;
		public final String methodDesc;
		public final String owner;
		public final String name;
		public final String desc;
		public final MavenArtifact artifact;

		public UnsafeEntry(String className, String methodName,
				String methodDesc, String owner, String name, String desc,
				MavenArtifact artifact) {
			this.className = className;
			this.methodName = methodName;
			this.methodDesc = methodDesc;
			this.owner = owner;
			this.name = name;
			this.desc = desc;
			this.artifact = artifact;
		}

		@Override
		public String toString() {
			return String.format("{ %s, %s, %s, %s, %s, %s }", className,
					methodName, methodDesc, owner, name, desc);
		}
	}

	private static class UnsafeVisitor extends ClassVisitor {

		private final List<UnsafeEntry> matches;
		private final MavenArtifact artifact;
		private String className;

		public UnsafeVisitor(List<UnsafeEntry> matches, MavenArtifact a) {
			super(Opcodes.ASM5);

			this.matches = matches;
			this.artifact = a;
		}

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			className = name;
			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public MethodVisitor visitMethod(int access, final String methodName,
				final String methodDesc, String signature, String[] exceptions) {

			MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {
				@Override
				public void visitMethodInsn(int opcode, String owner,
						String name, String desc, boolean itf) {
					if ("sun/misc/Unsafe".equals(owner)) {
						matches.add(new UnsafeEntry(className, methodName,
								methodDesc, owner, name, desc, artifact));
					}
				}

				@Override
				public void visitFieldInsn(int opcode, String owner,
						String name, String desc) {
					if ("sun/misc/Unsafe".equals(owner)) {
						matches.add(new UnsafeEntry(className, methodName,
								methodDesc, owner, name, desc, artifact));
					}
				};

				@Override
				public void visitLdcInsn(Object cst) {
					if (cst instanceof String) {
						String value = (String) cst;
						if ("sun/misc/Unsafe".equals(value)) {
							matches.add(new UnsafeEntry(className, methodName,
									methodDesc, "sun/misc/Unsafe",
									"sun/misc/Unsafe", "literal", artifact));
						}
					}
				}
			};

			return mv;
		}
	}

	public static List<UnsafeEntry> searchClassFile(byte[] classFile,
			MavenArtifact a) {
		List<UnsafeEntry> matches = new ArrayList<UnsafeEntry>();

		searchClassFile(classFile, matches, a);

		return matches;
	}

	public static List<UnsafeEntry> searchJarFile(byte[] jarFileBuffer,
			MavenArtifact a) throws IOException {
		List<UnsafeEntry> matches = new ArrayList<UnsafeEntry>();

		ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(
				jarFileBuffer));

		ZipEntry entry;
		while ((entry = zip.getNextEntry()) != null) {
			if (!entry.getName().endsWith(".class")) {
				continue;
			}

			ByteArrayOutputStream classfile = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];

			int len = 0;
			while ((len = zip.read(buffer)) > 0) {
				classfile.write(buffer, 0, len);
			}

			UnsafeAnalysis.searchClassFile(classfile.toByteArray(), matches, a);
		}

		return matches;
	}

	public static List<UnsafeEntry> searchJarFile(String jarFileName,
			MavenArtifact a) throws IOException {
		byte[] jarFileBuffer = Files.readAllBytes(Paths.get(jarFileName));

		return searchJarFile(jarFileBuffer, a);
	}

	private static void searchClassFile(byte[] classFile,
			List<UnsafeEntry> matches, MavenArtifact a) {
		ClassReader cr = new ClassReader(classFile);
		UnsafeVisitor uv = new UnsafeVisitor(matches, a);
		cr.accept(uv, 0);
	}
}
