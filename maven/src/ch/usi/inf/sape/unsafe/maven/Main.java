package ch.usi.inf.sape.unsafe.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
			final MavenIndex index = build(Check.class);

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

		private static class DumpThread extends Thread {
			private final Mirror mirror;
			private final List<String> queue;
			private final Log log;

			public DumpThread(Mirror mirror, List<String> queue, Log log) {
				this.mirror = mirror;
				this.queue = queue;
				this.log = log;
			}

			@Override
			public void run() {
				try {
					for (String path : queue) {
						download(path, mirror, log);
					}

				} catch (IOException e) {
					log.log("Exception in DumpThread: ");

					e.printStackTrace(log.out);
					throw new RuntimeException(e);
				}
			}

			private static void download(String path, Mirror mirror, Log log)
					throws IOException {

				String localPath = "db/" + path;

				if (!new File(localPath).exists()) {
					log.log("Processing %s", path);

					try {
						byte[] response = mirror.download(path, log);

						new File(new File(localPath).getParent()).mkdirs();
						FileOutputStream fos = new FileOutputStream(localPath);
						fos.write(response);
						fos.close();

					} catch (FileNotFoundException e) {
						log.log("File not found %s on mirror", path);
					}
				} else {
					log.log("Skipping %s", path);
				}
			}

		}

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
			log.log("Dumping map...");

			HashMap<Integer, List<String>> queues = new HashMap<Integer, List<String>>();
			for (int i = 0; i < numberOfThreads; i++) {
				queues.put(i, new ArrayList<String>());
			}

			int j = 0;
			for (Artifact a : index) {
				queues.get(j % numberOfThreads).add(a.getPath());

				j++;
			}

			for (int i = 0; i < numberOfThreads; i++) {
				String logFileName = "db/logindex-" + i + ".log";
				log.log("Opening log %s...", logFileName);

				new File(new File(logFileName).getParent()).mkdirs();

				Thread t = new DumpThread(mirrors[i % mirrors.length],
						queues.get(i), new Log(new PrintStream(logFileName)));
				t.start();
			}
		}
	}

	public static class Analyse {
		public static void main(String[] args) throws Exception {
			final MavenIndex index = build(Analyse.class);

			int i = 0;
			for (Artifact a : index) {
				i++;

				String path = a.getPath();

				try {
					List<UnsafeEntry> matches = UnsafeAnalysis
							.searchJarFile("db/" + path);

					UnsafeAnalysis.printMatchesCsv(System.out, matches);

					// String localPathCsv = localPath + ".csv";
					//
					// try (PrintStream out = new PrintStream(localPathCsv)) {
					// UnsafeAnalysis.printMatchesCsv(out, matches);
					// }
					//
					// // List<UnsafeEntry> matches = UnsafeAnalysis
					// // .searchJarFile(response);
					// // saveCsv(matches, localPath);
				} catch (NoSuchFileException e) {
					log.log("File not found %s (%dth)", path, i);
				}
			}
		}
	}
}
