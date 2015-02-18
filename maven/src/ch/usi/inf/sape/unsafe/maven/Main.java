package ch.usi.inf.sape.unsafe.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.search.IndexSearcher;

import ch.usi.inf.sape.unsafe.maven.MavenIndex.Artifact;
import ch.usi.inf.sape.unsafe.maven.UnsafeAnalysis.UnsafeEntry;

public class Main {

	private static void saveCsv(List<UnsafeEntry> matches, String localPath)
			throws FileNotFoundException {
		String localPathCsv = localPath + ".csv";

		try (PrintStream out = new PrintStream(localPathCsv)) {
			UnsafeAnalysis.printMatchesCsv(out, matches);
		}

		// List<UnsafeEntry> matches = UnsafeAnalysis
		// .searchJarFile(response);
		// saveCsv(matches, localPath);

	}

	private static void processArtifact(String path, Mirror mirror, Log log)
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
					processArtifact(path, mirror, log);
				}

			} catch (IOException e) {
				log.log("Exception in DumpThread: ");

				e.printStackTrace(log.out);
				throw new RuntimeException(e);
			}
		}
	}

	private static void dumpMap(MavenIndex index, Mirror mirrors[], Log log,
			int numberOfThreads) throws IOException, InterruptedException {
		log.log("Dumping map...");

		HashMap<Integer, List<String>> queues = new HashMap<Integer, List<String>>();
		for (int i = 0; i < numberOfThreads; i++) {
			queues.put(i, new ArrayList<String>());
		}

		int j = 0;
		for (Entry<String, Artifact> entry : index.map.entrySet()) {
			Artifact a = entry.getValue();
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

		// Deque<String> qs = new ArrayDeque<String>(index.rootGroupsList);
		//
		// Thread[] ts = new Thread[numberOfThreads];
		//
		// while (!qs.isEmpty()) {
		// for (int i = 0; i < numberOfThreads; i++) {
		// Thread t = ts[i];
		// if (t == null || !t.isAlive()) {
		// String prefix = qs.pop();
		//
		// log.log("Dumping map for prefix %s...", prefix);
		// ts[i] = new DumpThread(index, mirror, prefix, ls[i]);
		// ts[i].start();
		//
		// break;
		// }
		// }
		//
		// Thread.sleep(2000);
		// }
	}

	public static void main(String[] args) throws Exception {
		final IndexSearcher searcher = new IndexSearcher("index/");

		Log log = new Log(System.err);

		log.log("Analysing database with %,d documents...", searcher.maxDoc());

		MavenIndex index = MavenIndex.build(searcher);

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
		log.log("Extension set (%d): %s", index.extSet.size(), index.extSet);

		final int retries = 10;
		final Mirror[] mirrors = new Mirror[2];
		mirrors[0] = new Mirror("http://mirrors.ibiblio.org/maven2/", retries);
		mirrors[1] = new Mirror(
				"http://maven.antelink.com/content/repositories/central/",
				retries);

		dumpMap(index, mirrors, log, 8);
	}
}
