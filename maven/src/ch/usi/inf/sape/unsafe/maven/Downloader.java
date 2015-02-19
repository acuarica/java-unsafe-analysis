package ch.usi.inf.sape.unsafe.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Downloader {

	private static class DumpThread extends Thread {
		private final Mirror[] mirrors;
		private final List<Artifact> queue;
		private final int mirrorStart;
		private final Log log;

		public DumpThread(Mirror[] mirrors, List<Artifact> queue,
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
							size / 1024, mid);

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

		@Override
		public void run() {
			try {
				for (Artifact a : queue) {
					download(a.getPath(), a.size, mirrors, mirrorStart, log);
					download(a.getPath("pom"), -1, mirrors, mirrorStart, log);
				}

				log.log("[DONE]");
			} catch (IOException e) {
				log.log("Exception in DumpThread: ");

				e.printStackTrace(log.out);
				throw new RuntimeException(e);
			}
		}
	}

	public static void downloadAll(MavenIndex index, Mirror[] mirrors,
			int numberOfThreads, Log log) throws FileNotFoundException {
		log.log("Dumping map...");

		HashMap<Integer, List<Artifact>> queues = new HashMap<Integer, List<Artifact>>();
		for (int i = 0; i < numberOfThreads; i++) {
			queues.put(i, new ArrayList<Artifact>());
		}

		int j = 0;
		for (Artifact a : index) {
			queues.get(j % numberOfThreads).add(a);
			j++;
		}

		for (int i = 0; i < numberOfThreads; i++) {
			String logFileName = "db/logindex-" + i + ".log";
			log.log("Opening log %s...", logFileName);

			new File(new File(logFileName).getParent()).mkdirs();
			Thread t = new DumpThread(mirrors, queues.get(i), i
					% mirrors.length, new Log(new PrintStream(logFileName)));
			t.start();
		}
	}
}
