package ch.usi.inf.sape.unsafeanalysis;

import java.io.PrintStream;

import ch.usi.inf.sape.unsafeanalysis.argsparser.Arg;
import ch.usi.inf.sape.unsafeanalysis.argsparser.ArgsParser;
import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndex;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndexBuilder;
import ch.usi.inf.sape.unsafeanalysis.index.NexusIndexParser;
import ch.usi.inf.sape.unsafeanalysis.log.Log;

public class BuildUriList {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(shortkey = "i", longkey = "index", desc = "Specifies the path of the Nexus Index.")
		public String indexPath;

		@Arg(shortkey = "u", longkey = "urilist", desc = "Specifies the output uri list.")
		public String uriListPath;

		@Arg(shortkey = "n", longkey = "artscount", desc = "Specifies the number of artifacts to download.")
		public Integer noArtsToDownload;

		@Arg(shortkey = "m", longkey = "mirrors", desc = "Comma separated list of mirrors.")
		public String[] mirrors;

	}

	private static void emitDownloadFile(String path, String[] mirrors,
			PrintStream out) {
		for (String mirror : mirrors) {
			out.format("%s/%s\t", mirror, path);
		}

		out.println();
		out.format("\tout=%s\n", path);
	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		log.info("Using Index: %s", ar.indexPath);
		log.info("URI list: %s", ar.uriListPath);
		log.info("# artifact to download: %s", ar.noArtsToDownload);

		log.info("Using %d mirrors:", ar.mirrors.length);
		for (String mirror : ar.mirrors) {
			log.info("  * %s", mirror);
		}

		log.info("Parsing Index ...");

		NexusIndexParser nip = new NexusIndexParser(ar.indexPath);
		MavenIndex index = MavenIndexBuilder.build(nip);

		try (PrintStream out = new PrintStream(ar.uriListPath)) {
			int noArtifacts = 0;

			for (MavenArtifact a : index) {
				emitDownloadFile(a.getPomPath(), ar.mirrors, out);

				noArtifacts++;

				if (ar.noArtsToDownload == null
						|| noArtifacts < ar.noArtsToDownload.intValue()) {
					emitDownloadFile(a.getPath(), ar.mirrors, out);
				}
			}
		}
	}
}
