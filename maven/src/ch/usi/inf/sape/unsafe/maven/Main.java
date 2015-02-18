package ch.usi.inf.sape.unsafe.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
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

				// List<UnsafeEntry> matches = UnsafeAnalysis
				// .searchJarFile(response);
				// log.log("sun.misc.Unsafe found, saving %s", path);
				// saveCsv(matches, localPath);

			} catch (FileNotFoundException e) {
				log.log("File not found %s on mirror", path);
			}
		} else {
			log.log("Skipping %s", path);
		}
	}

	private static class DumpThread extends Thread {
		private MavenIndex index;
		private Mirror mirror;
		private String prefix;
		private Log log;

		public DumpThread(MavenIndex index, Mirror mirror, String prefix,
				Log log) {
			this.index = index;
			this.mirror = mirror;
			this.prefix = prefix;
			this.log = log;
		}

		@Override
		public void run() {
			try {

				for (Entry<String, Artifact> entry : index.map.entrySet()) {
					if (entry.getKey().startsWith(prefix)) {
						Artifact a = entry.getValue();
						processArtifact(a.getPath(), mirror, log);
					}
				}

			} catch (IOException e) {
				log.log("Exception in DumpThread: ");

				e.printStackTrace(log.out);
				throw new RuntimeException(e);
			}
		}
	}

	private static void dumpMap(MavenIndex index, Mirror mirror, Log log)
			throws IOException, InterruptedException {
		log.log("Dumping map...");

		int numberOfThreads = 5;

		Log[] ls = new Log[numberOfThreads];

		for (int i = 0; i < numberOfThreads; i++) {
			String logFileName = "db/logindex-" + i + ".log";
			log.log("Opening log %s...", logFileName);

			new File(new File(logFileName).getParent()).mkdirs();
			ls[i] = new Log(new PrintStream(logFileName));
		}

		Deque<String> qs = new ArrayDeque<String>(index.rootGroupsList);

		Thread[] ts = new Thread[numberOfThreads];

		while (!qs.isEmpty()) {
			for (int i = 0; i < numberOfThreads; i++) {
				Thread t = ts[i];
				if (t == null || !t.isAlive()) {
					String prefix = qs.pop();

					log.log("Dumping map for prefix %s...", prefix);
					ts[i] = new DumpThread(index, mirror, prefix, ls[i]);
					ts[i].start();

					break;
				}
			}

			Thread.sleep(2000);
		}
	}

	public static void main(String[] args) throws Exception {
		final IndexSearcher searcher = new IndexSearcher("index/");
		final Mirror mirror = new Mirror("http://mirrors.ibiblio.org/maven2/");

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

		dumpMap(index, mirror, log);
	}
}
