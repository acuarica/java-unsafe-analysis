package ch.usi.inf.sape.unsafeanalysis;

import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.log4j.Logger;

import ch.usi.inf.sape.unsafeanalysis.UnsafeAnalysis.UnsafeEntry;
import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndex;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndexBuilder;
import ch.usi.inf.sape.unsafeanalysis.index.NexusIndexParser;

public class AnalyseMaven {

	private static final Logger logger = Logger.getLogger(AnalyseMaven.class);

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			throw new Exception("Invalid arguments: needed index path");
		}

		String indexPath = args[0];

		NexusIndexParser nip = new NexusIndexParser(indexPath);
		MavenIndex index = MavenIndexBuilder.build(nip);

		List<UnsafeEntry> allMatches = new LinkedList<UnsafeEntry>();

		int i = 0;
		for (MavenArtifact a : index) {
			i++;

			String path = a.getPath();

			try {
				List<UnsafeEntry> matches = UnsafeAnalysis.searchJarFile("db/"
						+ path, a);

				allMatches.addAll(matches);
			} catch (NoSuchFileException e) {
				logger.info("File not found " + path + " (" + i + "th)", e);
			} catch (ZipException e) {
				logger.info("Zip exception for " + path + " (" + i + "th)", e);
			} catch (Exception e) {
				logger.info("Exception for " + path + " (" + i + "th): %s", e);
			}
		}

		String localPathCsv = "db/unsafe-maven.csv";

		try (PrintStream out = new PrintStream(localPathCsv)) {
			UnsafeAnalysis.printMatchesCsv(out, allMatches);
		}
	}

}
