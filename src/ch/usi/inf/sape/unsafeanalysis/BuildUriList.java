package ch.usi.inf.sape.unsafeanalysis;

import java.io.File;
import java.io.PrintStream;

import org.apache.log4j.Logger;

import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndex;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndexBuilder;
import ch.usi.inf.sape.unsafeanalysis.index.NexusIndexParser;

public class BuildUriList {

	private static final Logger logger = Logger.getLogger(BuildUriList.class);

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			throw new Exception(
					"Invalid arguments: needed index path, uri list, and at least on mirror");
		}

		String indexPath = args[0];
		String uriListPath = args[1];

		logger.info("Index: " + indexPath);
		logger.info("URI list: " + uriListPath);

		NexusIndexParser nip = new NexusIndexParser(indexPath);
		MavenIndex index = MavenIndexBuilder.build(nip);

		new File(new File(uriListPath).getParent()).mkdirs();
		try (PrintStream out = new PrintStream(uriListPath)) {
			for (MavenArtifact a : index) {
				for (int i = 2; i < args.length; i++) {
					String mirror = args[i];

					out.format("%s/%s\t", mirror, a.getPath());
				}

				out.println();
				out.format("\tout=%s\n", a.getPath());
			}
		}
	}
}
