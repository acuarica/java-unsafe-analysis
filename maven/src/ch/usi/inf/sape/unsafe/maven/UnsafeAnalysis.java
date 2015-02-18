package ch.usi.inf.sape.unsafe.maven;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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

public class UnsafeAnalysis {

	public static class UnsafeEntry {
		public String className;
		public String methodName;
		public String methodDesc;
		public String owner;
		public String name;
		public String desc;

		public UnsafeEntry(String className, String methodName,
				String methodDesc, String owner, String name, String desc) {
			this.className = className;
			this.methodName = methodName;
			this.methodDesc = methodDesc;
			this.owner = owner;
			this.name = name;
			this.desc = desc;
		}

		@Override
		public String toString() {
			return String.format("{ %s, %s, %s, %s, %s, %s }", className,
					methodName, methodDesc, owner, name, desc);
		}
	}

	private static class UnsafeVisitor extends ClassVisitor {

		private List<UnsafeEntry> matches;

		private String className;

		public UnsafeVisitor(List<UnsafeEntry> matches) {
			super(Opcodes.ASM5);

			this.matches = matches;
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
								methodDesc, owner, name, desc));
					}
				}
			};

			return mv;
		}
	}

	public static List<UnsafeEntry> searchClassFile(byte[] classFile) {
		List<UnsafeEntry> matches = new ArrayList<UnsafeEntry>();

		searchClassFile(classFile, matches);

		return matches;
	}

	public static List<UnsafeEntry> searchJarFile(byte[] jarFileBuffer)
			throws IOException {
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

			UnsafeAnalysis.searchClassFile(classfile.toByteArray(), matches);
		}

		return matches;
	}

	public static List<UnsafeEntry> searchJarFile(String jarFileName)
			throws IOException {
		byte[] jarFileBuffer = Files.readAllBytes(Paths.get(jarFileName));

		return searchJarFile(jarFileBuffer);
	}

	private static void searchClassFile(byte[] classFile,
			List<UnsafeEntry> matches) {
		ClassReader cr = new ClassReader(classFile);
		UnsafeVisitor uv = new UnsafeVisitor(matches);
		cr.accept(uv, 0);
	}

	public static void printMatchesCsv(PrintStream out,
			List<UnsafeEntry> matches) {

		for (UnsafeEntry entry : matches) {
			out.format("\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\"\n",
					entry.className, entry.methodName, entry.methodDesc,
					entry.owner, entry.name, entry.desc);
		}
	}
}
