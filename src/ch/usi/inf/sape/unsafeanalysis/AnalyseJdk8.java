package ch.usi.inf.sape.unsafeanalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.log4j.Logger;

import ch.usi.inf.sape.unsafeanalysis.analysis.UnsafeAnalysis;
import ch.usi.inf.sape.unsafeanalysis.analysis.UnsafeAnalysis.UnsafeEntry;
import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;

public class AnalyseJdk8 {

	private static final Logger logger = Logger.getLogger(AnalyseMaven.class);

	public static void main(String[] args) throws Exception {

		ArrayList<String> list = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(
				"jdk8/list.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
		}

		List<UnsafeEntry> allMatches = new LinkedList<UnsafeEntry>();

		int i = 0;
		for (String file : list) {
			String aid = file.substring(0, file.lastIndexOf("."));
			MavenArtifact a = new MavenArtifact("jdk8", aid, file + "@1.8", 1,
					"jar", "", "");

			i++;

			String path = a.getPath();

			logger.info("Analyzing artifact " + a.getId() + " (#" + i + ")");

			try {
				List<UnsafeEntry> matches = UnsafeAnalysis.searchJarFile(
						"jdk8/" + file, a);

				allMatches.addAll(matches);
			} catch (NoSuchFileException e) {
				logger.info("File not found " + path + " (" + i + "th)", e);
			} catch (ZipException e) {
				logger.info("Zip exception for " + path + " (" + i + "th)", e);
			} catch (Exception e) {
				logger.info("Exception for " + path + " (" + i + "th): %s", e);
			}
		}

		String localPathCsv = "db/unsafe-maven-jdk8.csv";

		try (PrintStream out = new PrintStream(localPathCsv)) {
			UnsafeAnalysis.printMatchesCsv(out, allMatches);
		}
	}
}