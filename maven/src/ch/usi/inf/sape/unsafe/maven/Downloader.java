package ch.usi.inf.sape.unsafe.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.usi.inf.sape.mavendb.MavenArtifact;
import ch.usi.inf.sape.mavendb.MavenIndex;
import ch.usi.inf.sape.util.Log;
import ch.usi.inf.sape.util.Mirror;

public class Downloader {

	private static class DumpThread extends Thread {
		private final Mirror[] mirrors;
		private final List<MavenArtifact> queue;
		private final int mirrorStart;
		private final Log log;

		public DumpThread(Mirror[] mirrors, List<MavenArtifact> queue,
				int mirrorStart, Log log) {
			this.mirrors = mirrors;
			this.queue = queue;
			this.mirrorStart = mirrorStart;
			this.log = log;

		}

		private static void download(String path, long size, Mirror[] mirrors,
				int mirrorStart, Log log) throws IOException {
			String localPath = "db/" + path;
			String localPathNotFound = localPath + ".notfound";

			if (!new File(localPath).exists()
					&& !new File(localPathNotFound).exists()) {
				for (int i = 0; i < mirrors.length; i++) {
					int mid = (i + mirrorStart) % mirrors.length;

					log.log("Downloading %s (%d KB) from %s", path,
							size / 1024, mirrors[mid].id);

					try {
						byte[] response = mirrors[mid].download(path, log);

						new File(new File(localPath).getParent()).mkdirs();
						FileOutputStream fos = new FileOutputStream(localPath);
						fos.write(response);
						fos.close();

						return;
					} catch (FileNotFoundException e) {
						log.log("File not found %s on mirror %s", path,
								mirrors[mid].id);
					}
				}

				new File(new File(localPathNotFound).getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(localPathNotFound);
				fos.close();

			} else {
				log.log("Skipping %s", path);
			}
		}

		public int progress;

		@Override
		public void run() {
			try {
				for (MavenArtifact a : queue) {
					download(a.getPath(), a.size, mirrors, mirrorStart, log);
					download(a.getPomPath(), -1, mirrors, mirrorStart, log);

					if (a.sources) {
						download(a.getSourcesPath(), -1, mirrors, mirrorStart,
								log);
					}

					progress++;
				}

				log.log("DONE");
			} catch (IOException e) {
				log.log("Exception in DumpThread: ");

				e.printStackTrace(log.out);
				throw new RuntimeException(e);
			}
		}
	}

	public static void downloadAll(MavenIndex index, Mirror[] mirrors,
			int numberOfThreads, Log log) throws FileNotFoundException,
			InterruptedException {
		log.log("Dumping map...");

		HashMap<Integer, List<MavenArtifact>> queues = new HashMap<Integer, List<MavenArtifact>>();
		for (int i = 0; i < numberOfThreads; i++) {
			queues.put(i, new ArrayList<MavenArtifact>());
		}

		int j = 0;
		for (MavenArtifact a : index) {
			queues.get(j % numberOfThreads).add(a);
			j++;
		}

		DumpThread[] ts = new DumpThread[numberOfThreads];
		for (int i = 0; i < numberOfThreads; i++) {
			String logFileName = "db/logindex-" + i + ".log";
			log.log("Opening log %s...", logFileName);

			int mirrorStart = i % mirrors.length;
			new File(new File(logFileName).getParent()).mkdirs();
			ts[i] = new DumpThread(mirrors, queues.get(i), mirrorStart,
					new Log(new PrintStream(logFileName)));
			ts[i].start();
		}

		while (true) {
			Thread.sleep(5 * 1000);

			boolean done = true;

			String progress = "";
			for (int i = 0; i < numberOfThreads; i++) {
				DumpThread t = ts[i];

				int p = t.progress * 100 / t.queue.size();
				String sp;
				if (t.isAlive()) {
					done = false;
					sp = String.format("%2d %%", p);
				} else {
					sp = p == 100 ? "DONE" : " ERR";
				}

				String join = i == 0 ? "" : " ";
				progress += join + sp;
			}

			log.log("%s", progress);

			if (done) {
				break;
			}
		}
	}
}
