package ch.usi.inf.sape.unsafe.maven;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.lucene.search.IndexSearcher;

import ch.usi.inf.sape.unsafe.maven.MavenIndex.Artifact;

public class Main {

	private static void log(String format, Object... args) {
		System.err.format("[ " + format + " ]\n", args);
	}

	private static boolean analyseArtifact(byte[] cf) throws IOException {
		ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(cf));

		boolean found = false;
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

			if (UnsafeAnalysis.search(classfile.toByteArray())) {
				found = true;
			}
		}

		return found;
	}

	private static void processArtifact(String path, Mirror mirror)
			throws IOException {

		String localPath = "db/" + path;
		String localPathDone = localPath + ".done";

		if (!new File(localPathDone).exists()) {
			log("Processing %s", path);

			byte[] response = mirror.download(path);
			boolean save = analyseArtifact(response);

			new File(new File(localPath).getParent()).mkdirs();

			if (save) {
				FileOutputStream fos = new FileOutputStream(localPath);
				fos.write(response);
				fos.close();
			}

			new FileOutputStream(localPathDone).close();
		} else {
			log("Skipping %s", path);
		}
	}

	private static void dumpMap(HashMap<String, Artifact> map, Mirror mirror)
			throws IOException {
		log("Dumping map...");

		for (Entry<String, Artifact> entry : map.entrySet()) {
			Artifact a = entry.getValue();
			processArtifact(a.getPath(), mirror);
		}
	}

	public static void main(String[] args) throws Exception {
		final IndexSearcher searcher = new IndexSearcher("index/");
		final Mirror mirror = new Mirror("http://mirrors.ibiblio.org/maven2/");

		processArtifact(
				"org/infinispan/infinispan-osgi/7.1.0.Final/infinispan-osgi-7.1.0.Final.jar",
				mirror);

		log("Analysing database with %,d documents...", searcher.maxDoc());

		MavenIndex index = MavenIndex.build(searcher);

		log("uniqueArtifactsCount: %,d", index.uniqueArtifactsCount);
		log("totalSize: %,d MB", index.totalSize / (1024 * 1024));
		log("lastVersionJarsSize: %,d MB", index.lastVersionJarsSize
				/ (1024 * 1024));
		log("mmindate: %s", index.mmindate);
		log("mmindate: %s", index.mmaxdate);
		log("mmindate: %s", index.imindate);
		log("mmindate: %s", index.imaxdate);
		log("Root groups list (%d): %s", index.rootGroupsList.size(),
				index.rootGroupsList);
		log("Extension set (%d): %s", index.extSet.size(), index.extSet);

		dumpMap(index.map, mirror);
	}
}
