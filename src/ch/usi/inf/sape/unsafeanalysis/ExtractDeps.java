package ch.usi.inf.sape.unsafeanalysis;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import ch.usi.inf.sape.unsafeanalysis.analysis.DepsManager;
import ch.usi.inf.sape.unsafeanalysis.argsparser.Arg;
import ch.usi.inf.sape.unsafeanalysis.argsparser.ArgsParser;
import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndex;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndexBuilder;
import ch.usi.inf.sape.unsafeanalysis.index.NexusIndexParser;
import ch.usi.inf.sape.unsafeanalysis.index.PomDependency;
import ch.usi.inf.sape.unsafeanalysis.log.Log;

public class ExtractDeps {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(shortkey = "i", longkey = "index", desc = "Specifies the path of the Nexus Index.")
		public String indexPath;

		@Arg(shortkey = "r", longkey = "repo", desc = "Specifies the path of the Maven repository.")
		public String repoPath;

		@Arg(shortkey = "o", longkey = "output", desc = "Specifies the path of the output file.")
		public String outputPath;
	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		log.info("Using Index: %s", ar.indexPath);
		log.info("Output: %s", ar.outputPath);

		log.info("Parsing Index...");

		NexusIndexParser nip = new NexusIndexParser(ar.indexPath);
		MavenIndex index = MavenIndexBuilder.build(nip);

		try (PrintStream out = new PrintStream(ar.outputPath)) {
			out.println("groupId, artifactId, depGroupId, depArtifactId, depVersion, depScope");

			int i = 0;
			for (final MavenArtifact a : index) {
				i++;

				String path = a.getPomPath();
				try {
					List<PomDependency> deps = DepsManager
							.extractDeps(ar.repoPath + "/" + path);

					for (PomDependency dep : deps) {
						out.format("%s, %s, %s, %s, %s, %s\n", a.groupId,
								a.artifactId, dep.groupId, dep.artifactId,
								dep.version, dep.scope);
					}
				} catch (FileNotFoundException e) {
					log.info("POM not found %s (# %d)", path, i);
				} catch (Exception e) {
					log.info("Exception in %s (# %d): %s", path, i, e);
				}
			}
		}
	}
}
