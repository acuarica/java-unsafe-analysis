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

import ch.usi.inf.sape.mavendb.MavenArtifact;

public class ExtractAnalysis {

	public static class Stats {
		public long noJars;
		public long noClasses;
		public long noMethods;
		public long noCallSites;
		public long noFieldAcceses;
		public long noStringLiterals;
	}

	public static class ExtractEntry {
		public final String className;
		public final String methodName;
		public final String methodDesc;
		public final String owner;
		public final String name;
		public final String desc;
		public final MavenArtifact artifact;

		public ExtractEntry(String className, String methodName,
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

	private static class ExtractVisitor extends ClassVisitor {

		private final List<ExtractEntry> matches;
		private final MavenArtifact artifact;
		private String className;
		private Stats stats;

		public ExtractVisitor(List<ExtractEntry> matches, MavenArtifact a,
				Stats stats) {
			super(Opcodes.ASM5);

			this.matches = matches;
			this.artifact = a;
			this.stats = stats;
		}

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			stats.noClasses++;

			className = name;
			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public MethodVisitor visitMethod(int access, final String methodName,
				final String methodDesc, String signature, String[] exceptions) {
			stats.noMethods++;

			MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {
				@Override
				public void visitMethodInsn(int opcode, String owner,
						String name, String desc, boolean itf) {
					stats.noCallSites++;

					// matches.add(new ExtractEntry(className, methodName,
					// methodDesc, owner, name, desc, artifact));

				}

				@Override
				public void visitFieldInsn(int opcode, String owner,
						String name, String desc) {
					stats.noFieldAcceses++;

					// matches.add(new ExtractEntry(className, methodName,
					// methodDesc, owner, name, desc, artifact));

				};

				@Override
				public void visitLdcInsn(Object cst) {
					if (cst instanceof String) {
						stats.noStringLiterals++;

						// String value = (String) cst;
						// if ("sun/misc/Unsafe".equals(value)) {
						// matches.add(new UnsafeEntry(className, methodName,
						// methodDesc, "sun/misc/Unsafe",
						// "sun/misc/Unsafe", "literal", artifact));
						// }
					}
				}
			};

			return mv;
		}
	}

	public static List<ExtractEntry> searchClassFile(byte[] classFile,
			MavenArtifact a, Stats stats) {
		List<ExtractEntry> matches = new ArrayList<ExtractEntry>();

		searchClassFile(classFile, matches, a, stats);

		return matches;
	}

	public static List<ExtractEntry> searchJarFile(byte[] jarFileBuffer,
			MavenArtifact a, Stats stats) throws IOException {

		stats.noJars++;

		List<ExtractEntry> matches = new ArrayList<ExtractEntry>();

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

			ExtractAnalysis.searchClassFile(classfile.toByteArray(), matches,
					a, stats);
		}

		return matches;
	}

	public static List<ExtractEntry> searchJarFile(String jarFileName,
			MavenArtifact a, Stats stats) throws IOException {
		byte[] jarFileBuffer = Files.readAllBytes(Paths.get(jarFileName));

		return searchJarFile(jarFileBuffer, a, stats);
	}

	private static void searchClassFile(byte[] classFile,
			List<ExtractEntry> matches, MavenArtifact a, Stats stats) {
		ClassReader cr = new ClassReader(classFile);
		ExtractVisitor uv = new ExtractVisitor(matches, a, stats);
		cr.accept(uv, 0);
	}

	public static void printStats(PrintStream out, Stats stats) {
		out.format("noJars: %,d\n", stats.noJars);
		out.format("noClasses: %,d\n", stats.noClasses);
		out.format("noMethods: %,d\n", stats.noMethods);
		out.format("noCallSites: %,d\n", stats.noCallSites);
		out.format("noFieldAcceses: %,d\n", stats.noFieldAcceses);
		out.format("noStringLiterals: %,d\n", stats.noStringLiterals);
	}

	public static void printMatchesCsv(PrintStream out,
			List<ExtractEntry> matches) {
		// out.println("className, methodName, methodDesc, owner, name, desc, groupId, artifactId, version, size, ext");
		//
		// for (ExtractEntry entry : matches) {
		// out.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s\n",
		// entry.className, entry.methodName, entry.methodDesc,
		// entry.owner, entry.name, entry.desc,
		// entry.artifact == null ? "" : entry.artifact.groupId,
		// entry.artifact == null ? "" : entry.artifact.artifactId,
		// entry.artifact == null ? "" : entry.artifact.version,
		// entry.artifact == null ? "" : entry.artifact.size,
		// entry.artifact == null ? "" : entry.artifact.ext);
		// }
	}
}
