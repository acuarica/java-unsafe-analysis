package ch.usi.inf.sape.unsafeanalysis;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

import org.apache.log4j.Logger;

import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndex;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndexBuilder;
import ch.usi.inf.sape.unsafeanalysis.index.NexusIndexParser;

public class BuildUriList {

	private static final Logger logger = Logger.getLogger(BuildUriList.class);

	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			throw new Exception(
					"Invalid arguments: needed index path, uri list, number of artifacts to download (or - for no limit), and at least on mirror");
		}

		String indexPath = args[0];
		String uriListPath = args[1];
		String arts = args[2];
		Integer noArtsToDownload = "-".equals(arts) ? null : Integer
				.parseInt(arts);

		String[] mirrors = Arrays.copyOfRange(args, 3, args.length);

		logger.info("Index: " + indexPath);
		logger.info("URI list: " + uriListPath);
		logger.info("# artifact to download: " + noArtsToDownload);

		logger.info("Using " + mirrors.length + " mirrors:");
		for (String mirror : mirrors) {
			logger.info("  * " + mirror);
		}

		NexusIndexParser nip = new NexusIndexParser(indexPath);
		MavenIndex index = MavenIndexBuilder.build(nip);

		new File(new File(uriListPath).getParent()).mkdirs();

		try (PrintStream out = new PrintStream(uriListPath)) {
			int noArtifacts = 0;

			for (MavenArtifact a : index) {
				for (String mirror : mirrors) {
					out.format("%s/%s\t", mirror, a.getPath());
				}

				out.println();
				out.format("\tout=%s\n", a.getPath());

				noArtifacts++;

				if (noArtsToDownload != null
						&& noArtsToDownload.intValue() == noArtifacts) {
					break;
				}
			}
		}
	}
}
