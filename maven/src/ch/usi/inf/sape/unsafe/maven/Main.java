package ch.usi.inf.sape.unsafe.maven;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.lucene.search.IndexSearcher;

import ch.usi.inf.sape.unsafe.maven.UnsafeAnalysis.UnsafeEntry;

public class Main {

	private static final Log log = new Log(System.err);

	private static void showSummary(MavenIndex index) {
		log.log("uniqueArtifactsCount: %,d", index.uniqueArtifactsCount);
		log.log("totalSize: %,d MB", index.totalSize / (1024 * 1024));
		log.log("lastVersionJarsSize: %,d MB", index.lastVersionJarsSize
				/ (1024 * 1024));
		log.log("mmindate: %s", index.mmindate);
		log.log("mmindate: %s", index.mmaxdate);
		log.log("mmindate: %s", index.imindate);
		log.log("mmindate: %s", index.imaxdate);
		log.log("Root groups list (%d): %s", index.rootGroupsList.size(),
				index.rootGroupsList);
		log.log("Field set (%d): %s", index.fieldSet.size(), index.fieldSet);
		log.log("Extension set (%d): %s", index.extSet.size(), index.extSet);
	}

	private static MavenIndex build(Class<?> mainClass) throws Exception {
		log.log("Running class %s", mainClass);

		final IndexSearcher searcher = new IndexSearcher("index/");

		log.log("Maven Index contains %,d documents", searcher.maxDoc());

		final MavenIndex index = MavenIndex.build(searcher);
		showSummary(index);

		return index;
	}

	public static class Check {
		public static void main(String args[]) throws Exception {
			build(Check.class);
		}
	}

	public static class Build {
		public static void main(String args[]) throws Exception {
			final MavenIndex index = build(Build.class);

			log.log("Serializing index... ");

			String fileName = "db/index.list";
			new File(new File(fileName).getParent()).mkdirs();
			try (PrintStream out = new PrintStream(fileName)) {
				index.print(out);
			}

			log.log("DONE");
		}
	}

	public static class Download {
		public static void main(String[] args) throws Exception {
			final MavenIndex index = build(Download.class);

			final int retries = 10;
			final Mirror[] mirrors = new Mirror[2];
			mirrors[0] = new Mirror("http://mirrors.ibiblio.org/maven2/",
					retries);
			mirrors[1] = new Mirror(
					"http://maven.antelink.com/content/repositories/central/",
					retries);

			int numberOfThreads = 8;

			Downloader.downloadAll(index, mirrors, numberOfThreads, log);
		}
	}

	public static class Analyse {
		public static void main(String[] args) throws Exception {
			final MavenIndex index = build(Analyse.class);

			List<UnsafeEntry> allMatches = new LinkedList<UnsafeEntry>();

			int i = 0;
			for (Artifact a : index) {
				i++;

				String path = a.getPath();

				try {
					List<UnsafeEntry> matches = UnsafeAnalysis
							.searchJarFile("db/" + path);

					allMatches.addAll(matches);
				} catch (NoSuchFileException e) {
					log.log("File not found %s (%dth)", path, i);
				} catch (ZipException e) {
					log.log("Zip exception for %s (%dth): %s", path, i,
							e.getMessage());
				} catch (Exception e) {
					log.log("Exception for %s (%dth): %s", path, i,
							e.getMessage());
				}
			}

			String localPathCsv = "db/unsafe-maven.csv";

			try (PrintStream out = new PrintStream(localPathCsv)) {
				UnsafeAnalysis.printMatchesCsv(out, allMatches);
			}
		}
	}
}
