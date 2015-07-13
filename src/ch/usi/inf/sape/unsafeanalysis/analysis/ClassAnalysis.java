package ch.usi.inf.sape.unsafeanalysis.analysis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.usi.inf.sape.unsafeanalysis.index.Inserter;
import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;

public class ClassAnalysis {

	private static class ExtractVisitor extends ClassVisitor {

		private final MavenArtifact a;
		private String className;
		private Inserter cls;
		private Inserter method;
		private Inserter callsite;
		private Inserter fieldaccess;
		private Inserter literal;

		public ExtractVisitor(MavenArtifact a, Connection c)
				throws SQLException {
			super(Opcodes.ASM5);

			this.a = a;

			cls = new Inserter(c,
					"insert into class (gid, aid, ver, clsname, supername) values (?, ?, ?, ?, ?)");

			method = new Inserter(
					c,
					"insert into method (gid, aid, ver, clsname, methodname, methoddesc) values (?, ?, ?, ?, ?, ?)");

			callsite = new Inserter(
					c,
					"insert into callsite (gid, aid, ver, clsname,methodname, methoddesc ,targetclass,targetmethod,targetdesc) values (?, ?, ?,  ?, ?, ?,  ?, ?, ?)");

			fieldaccess = new Inserter(
					c,
					"insert into fieldaccess (gid, aid, ver, clsname,methodname, methoddesc ,targetclass,targetfield,targetdesc) values (?, ?, ?,  ?, ?, ?,  ?, ?, ?)");

			literal = new Inserter(
					c,
					"insert into literal (gid, aid, ver, clsname,methodname, methoddesc , literal) values (?, ?, ?,  ?, ?, ?,  ?)");

			// a == null ? ""
			// : a.groupId, a == null ? ""
			// : a.artifactId, a == null ? ""
			// : a.version
		}

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			className = name;
			super.visit(version, access, name, signature, superName, interfaces);

			cls.insert(a.groupId, a.artifactId, a.version, name, superName);
		}

		@Override
		public MethodVisitor visitMethod(int access, final String methodName,
				final String methodDesc, String signature, String[] exceptions) {

			method.insert(a.groupId, a.artifactId, a.version, className,
					methodName, methodDesc);

			MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {
				@Override
				public void visitMethodInsn(int opcode, String owner,
						String name, String desc, boolean itf) {
					callsite.insert(a.groupId, a.artifactId, a.version,
							className, methodName, methodDesc, owner, name,
							desc);
				}

				@Override
				public void visitFieldInsn(int opcode, String owner,
						String name, String desc) {
					fieldaccess.insert(a.groupId, a.artifactId, a.version,
							className, methodName, methodDesc, owner, name,
							desc);
				};

				@Override
				public void visitLdcInsn(Object cst) {
					if (cst instanceof String) {
						String value = (String) cst;
						literal.insert(a.groupId, a.artifactId, a.version,
								className, methodName, methodDesc, value);
					}
				}
			};

			return mv;
		}
	}

	private static void searchClassFile(byte[] classFile, MavenArtifact a,
			Connection c) throws SQLException {
		ClassReader cr = new ClassReader(classFile);
		ExtractVisitor uv = new ExtractVisitor(a, c);
		cr.accept(uv, 0);
	}

	private static void searchJarFile(byte[] jarFileBuffer, MavenArtifact a,
			Connection c) throws IOException, SQLException {
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

			searchClassFile(classfile.toByteArray(), a, c);
		}
	}

	public static void searchJarFile(String jarFileName, MavenArtifact a,
			Connection c) throws IOException, SQLException {
		byte[] jarFileBuffer = Files.readAllBytes(Paths.get(jarFileName));

		searchJarFile(jarFileBuffer, a, c);
	}
}
