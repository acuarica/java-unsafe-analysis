package ch.usi.inf.sape.unsafeanalysis;

import java.io.PrintStream;

import ch.usi.inf.sape.unsafeanalysis.argsparser.Arg;
import ch.usi.inf.sape.unsafeanalysis.argsparser.ArgsParser;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndex;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndexBuilder;
import ch.usi.inf.sape.unsafeanalysis.index.NexusIndexParser;
import ch.usi.inf.sape.unsafeanalysis.log.Log;

public class Stats {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(shortkey = "i", longkey = "index", desc = "Specifies the path of the Nexus Index.")
		public String indexPath;

		@Arg(shortkey = "r", longkey = "repo", desc = "Specifies the path of the Maven repository.")
		public String repoPath;

		@Arg(shortkey = "o", longkey = "output", desc = "Specifies the path of the output file.")
		public String outputPath;

	}

	private static final int G = 1024 * 1024 * 1024;

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		log.info("Using Index: %s", ar.indexPath);
		log.info("Output: %s", ar.outputPath);

		log.info("Parsing Index...");

		NexusIndexParser nip = new NexusIndexParser(ar.indexPath);
		MavenIndex index = MavenIndexBuilder.build(nip);

		try (PrintStream out = new PrintStream(ar.outputPath)) {
			out.format("variable, value\n");
			out.format("maxDoc, %d\n", index.maxDoc);
			out.format("totalArtifactsCount, %d\n", index.totalArtifactsCount);
			out.format("uniqueArtifactsCount, %d\n", index.uniqueArtifactsCount);
			out.format("uniqueJarsArtifactsCount, %d\n",
					index.uniqueJarsArtifactsCount);
			out.format("lastVersionJarsSize, %d GB\n",
					index.lastVersionJarsSize / G);
			out.format("totalSize, %f TB\n", index.totalSize / (G * 1024f));
		}
	}
}
