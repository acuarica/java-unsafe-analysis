package ch.usi.inf.sape.unsafeanalysis;

import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.zip.ZipException;

import ch.usi.inf.sape.unsafeanalysis.analysis.UnsafeAnalysis;
import ch.usi.inf.sape.unsafeanalysis.analysis.UnsafeAnalysis.UnsafeEntry;
import ch.usi.inf.sape.unsafeanalysis.argsparser.Arg;
import ch.usi.inf.sape.unsafeanalysis.argsparser.ArgsParser;
import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndex;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndexBuilder;
import ch.usi.inf.sape.unsafeanalysis.index.NexusIndexParser;
import ch.usi.inf.sape.unsafeanalysis.log.Log;

public class Analyse {

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

		log.info("Parsing Index: %s", ar.indexPath);

		NexusIndexParser nip = new NexusIndexParser(ar.indexPath);
		MavenIndex index = MavenIndexBuilder.build(nip);

		try (PrintStream out = new PrintStream(ar.outputPath)) {
			out.println("className, methodName, methodDesc, owner, name, desc, groupId, artifactId, version, size, ext");

			int i = 0;
			for (MavenArtifact a : index) {
				i++;
				String path = ar.repoPath + "/" + a.getPath();
				analyseArtifact(path, a, i, out);
			}

			for (String file : new String[] { "rt.jar" }) {
				String aid = file.substring(0, file.lastIndexOf("."));
				MavenArtifact a = new MavenArtifact("jdk8", aid, file + "@1.8",
						1, "jar", "", "");

				i++;
				String path = "jdk8/" + file;
				analyseArtifact(path, a, i, out);
			}

		}
	}

	private static void analyseArtifact(String path, MavenArtifact a, int i,
			PrintStream out) {
		log.info("Analyzing artifact %s (# %d)", a.getId(), i);

		try {
			List<UnsafeEntry> matches = UnsafeAnalysis.searchJarFile(path, a);

			for (UnsafeEntry entry : matches) {
				printMatchCsv(out, entry);
			}

		} catch (NoSuchFileException e) {
			log.info("File not found %s: (# %d): %s", path, i, e.getMessage());
		} catch (ZipException e) {
			log.info("Zip exception for %s (# %d): %s", path, i, e.getMessage());
		} catch (Exception e) {
			log.info("Exception for %s (# %d): %s", path, i, e);
		}
	}

	private static void printMatchCsv(PrintStream out, UnsafeEntry entry) {
		out.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s\n",
				entry.className, entry.methodName, entry.methodDesc,
				entry.owner, entry.name, entry.desc,
				entry.artifact == null ? "" : entry.artifact.groupId,
				entry.artifact == null ? "" : entry.artifact.artifactId,
				entry.artifact == null ? "" : entry.artifact.version,
				entry.artifact == null ? "" : entry.artifact.size,
				entry.artifact == null ? "" : entry.artifact.ext);

	}
}
