package ch.usi.inf.sape.unsafe.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.search.IndexSearcher;

import ch.usi.inf.sape.unsafe.maven.MavenIndex.Artifact;
import ch.usi.inf.sape.unsafe.maven.UnsafeAnalysis.UnsafeEntry;

public class Main {

	private static void processArtifact(String path, Mirror mirror, Log log)
			throws IOException {

		String localPath = "db/" + path;
		String localPathDone = localPath + ".done";

		if (!new File(localPathDone).exists()) {
			log.log("Processing %s", path);

			new File(new File(localPath).getParent()).mkdirs();

			try {
				byte[] response = mirror.download(path, log);
				List<UnsafeEntry> matches = UnsafeAnalysis
						.searchJarFile(response);

				boolean save = matches.size() > 0;
				if (save) {
					log.log("sun.misc.Unsafe found, saving %s", path);

					FileOutputStream fos = new FileOutputStream(localPath);
					fos.write(response);
					fos.close();
				}

				new FileOutputStream(localPathDone).close();
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
			for (Entry<String, Artifact> entry : index.map.entrySet()) {
				if (entry.getKey().startsWith(prefix)) {
					Artifact a = entry.getValue();
					try {
						processArtifact(a.getPath(), mirror, log);
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private static void dumpMap(MavenIndex index, Mirror mirror, Log log)
			throws IOException, InterruptedException {
		log.log("Dumping map...");

		Deque<String> qs = new ArrayDeque<String>(index.rootGroupsList);

		List<Thread> ts = new ArrayList<Thread>();

		while (!qs.isEmpty()) {
			while (ts.size() < 5 && !qs.isEmpty()) {
				String prefix = qs.pop();

				Log flog = new Log(new PrintStream("db/logindex-" + prefix
						+ ".log"));

				log.log("Dumping map for prefix %s...", prefix);

				Thread t = new DumpThread(index, mirror, prefix, flog);
				ts.add(t);
				t.start();
			}

			Thread.sleep(2000);
			log.lognl(".");

			for (Iterator<Thread> it = ts.iterator(); it.hasNext();) {
				Thread t = it.next();
				if (!t.isAlive()) {
					it.remove();
				}
			}
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
