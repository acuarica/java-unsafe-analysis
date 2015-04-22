package ch.usi.inf.sape.unsafe.maven;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.usi.inf.sape.mavendb.MavenArtifact;

public class CassandraAnalysis {

	public static class CassandraEntry {
		public final String className;
		public final String methodName;
		public final String methodDesc;
		public final String owner;
		public final String name;
		public final String desc;
		public final MavenArtifact artifact;

		public CassandraEntry(String className, String methodName,
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

	private static final List<String> ds = Arrays.asList(
			"me.prettyprint.cassandra.", "com.datastax.driver.",
			"com.netflix.astyanax.");

	private static class UnsafeVisitor extends ClassVisitor {

		private final List<CassandraEntry> matches;
		private final MavenArtifact artifact;
		private String className;

		public UnsafeVisitor(List<CassandraEntry> matches, MavenArtifact a) {
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

		private static boolean contains(String owner) {
			for (String d : ds) {

				if (owner.startsWith(d)) {
					return true;
				}

				if (owner.replace('/', '.').startsWith(d)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public MethodVisitor visitMethod(int access, final String methodName,
				final String methodDesc, String signature, String[] exceptions) {

			MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {
				@Override
				public void visitMethodInsn(int opcode, String owner,
						String name, String desc, boolean itf) {
					if (contains(owner)) {
						matches.add(new CassandraEntry(className, methodName,
								methodDesc, owner, name, desc, artifact));
					}
				}

				@Override
				public void visitFieldInsn(int opcode, String owner,
						String name, String desc) {
					if (contains(owner)) {
						matches.add(new CassandraEntry(className, methodName,
								methodDesc, owner, name, desc, artifact));
					}
				};

				@Override
				public void visitLdcInsn(Object cst) {
					if (cst instanceof String) {
						String value = (String) cst;
						if (contains(value)) {
							matches.add(new CassandraEntry(className,
									methodName, methodDesc, value, value,
									"literal", artifact));
						}
					}
				}
			};

			return mv;
		}
	}

	public static List<CassandraEntry> searchClassFile(byte[] classFile,
			MavenArtifact a) {
		List<CassandraEntry> matches = new ArrayList<CassandraEntry>();

		searchClassFile(classFile, matches, a);

		return matches;
	}

	public static List<CassandraEntry> searchJarFile(byte[] jarFileBuffer,
			MavenArtifact a) throws IOException {
		List<CassandraEntry> matches = new ArrayList<CassandraEntry>();

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

			CassandraAnalysis.searchClassFile(classfile.toByteArray(), matches,
					a);
		}

		return matches;
	}

	public static List<CassandraEntry> searchJarFile(String jarFileName,
			MavenArtifact a) throws IOException {
		byte[] jarFileBuffer = Files.readAllBytes(Paths.get(jarFileName));

		return searchJarFile(jarFileBuffer, a);
	}

	private static void searchClassFile(byte[] classFile,
			List<CassandraEntry> matches, MavenArtifact a) {
		ClassReader cr = new ClassReader(classFile);
		UnsafeVisitor uv = new UnsafeVisitor(matches, a);
		cr.accept(uv, 0);
	}

	public static void printMatchesCsv(PrintStream out,
			List<CassandraEntry> matches) {
		out.println("className, methodName, methodDesc, owner, name, desc, groupId, artifactId, version, size, ext");

		for (CassandraEntry entry : matches) {
			out.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s\n",
					entry.className, entry.methodName, entry.methodDesc,
					entry.owner, entry.name, entry.desc,
					entry.artifact == null ? "" : entry.artifact.groupId,
					entry.artifact == null ? "" : entry.artifact.artifactId,
					entry.artifact == null ? "" : entry.artifact.version,
					entry.artifact == null ? "" : entry.artifact.size,
					entry.artifact == null ? "" : entry.artifact.ext);
		}
	}
}
