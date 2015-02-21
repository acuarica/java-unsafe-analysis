package ch.usi.inf.sape.unsafe.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipException;

import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.search.IndexSearcher;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.usi.inf.sape.mavendb.MavenArtifact;
import ch.usi.inf.sape.mavendb.MavenArtifact.Dependency;
import ch.usi.inf.sape.mavendb.MavenDBProperties;
import ch.usi.inf.sape.mavendb.MavenIndex;
import ch.usi.inf.sape.mavendb.MavenIndexBuilder;
import ch.usi.inf.sape.unsafe.maven.UnsafeAnalysis.UnsafeEntry;
import ch.usi.inf.sape.util.Log;
import ch.usi.inf.sape.util.Mirror;

public class Main {

	private static final Log log = new Log(System.err);

	private static void showSummary(MavenIndex index) {
		log.log("uniqueArtifactsCount: %,d", index.uniqueArtifactsCount);
		log.log("totalSize: %,d MB", index.totalSize / (1024 * 1024));
		log.log("lastVersionJarsSize: %,d MB", index.lastVersionJarsSize
				/ (1024 * 1024));
		log.log("mmindate: %s", index.mmindate);
		log.log("mmindate: %s", index.mmaxdate);
		log.log("mmindate: %s", index.imindate);
		log.log("mmindate: %s", index.imaxdate);
		log.log("Root groups list (%d): %s", index.rootGroupsList.size(),
				index.rootGroupsList);
		log.log("Field set (%d): %s", index.fieldSet.size(), index.fieldSet);
		log.log("Extension set (%d): %s", index.extSet.size(), index.extSet);
	}

	private static MavenIndex build(Class<?> mainClass) throws Exception {
		log.log("Running class %s", mainClass);

		String indexPath = MavenDBProperties.get().indexPath();
		IndexSearcher searcher = new IndexSearcher(indexPath);

		log.log("Maven Index contains %,d documents", searcher.maxDoc());

		MavenIndex index = MavenIndexBuilder.build(searcher);
		showSummary(index);

		return index;
	}

	public static class Build {
		public static void main(String args[]) throws Exception {
			MavenIndex index = build(Build.class);

			log.log("Serializing index... ");

			String fileName = MavenDBProperties.get().artifactsPath();
			new File(new File(fileName).getParent()).mkdirs();
			try (PrintStream out = new PrintStream(fileName)) {
				index.print(out);
			}

			log.log("DONE");
		}
	}

	public static class Download {
		public static void main(String[] args) throws Exception {
			MavenIndex index = build(Download.class);

			int r = MavenDBProperties.get().downloaderRetries();
			int n = MavenDBProperties.get().downloaderNumberOfThreads();

			List<Mirror> ms = new ArrayList<Mirror>();
			for (String url : MavenDBProperties.get().downloaderMirrorList()) {
				Mirror m = new Mirror(url, r);
				ms.add(m);
			}

			Mirror[] mirrors = ms.toArray(new Mirror[ms.size()]);

			Downloader.downloadAll(index, mirrors, n, log);
		}
	}

	public static class Analyse {
		public static void main(String[] args) throws Exception {
			final MavenIndex index = build(Analyse.class);

			List<UnsafeEntry> allMatches = new LinkedList<UnsafeEntry>();

			int i = 0;
			for (MavenArtifact a : index) {
				i++;

				String path = a.getPath();

				try {
					List<UnsafeEntry> matches = UnsafeAnalysis.searchJarFile(
							"db/" + path, a);

					allMatches.addAll(matches);
				} catch (NoSuchFileException e) {
					log.log("File not found %s (%dth)", path, i);
				} catch (ZipException e) {
					log.log("Zip exception for %s (%dth): %s", path, i,
							e.getMessage());
				} catch (Exception e) {
					log.log("Exception for %s (%dth): %s", path, i,
							e.getMessage());
				}
			}

			String localPathCsv = "db/unsafe-maven.csv";

			try (PrintStream out = new PrintStream(localPathCsv)) {
				UnsafeAnalysis.printMatchesCsv(out, allMatches);
			}
		}
	}

	public static class Graph {
		public static void main(String[] args) throws Exception {
			final MavenIndex index = build(Graph.class);

			String localPathCsv = "db/depgraph.csv";
			try (PrintStream out = new PrintStream(localPathCsv)) {

				out.println("groupId, artifactId, depGroupId, depArtifactId, depVersion");

				for (final MavenArtifact a : index) {
					String path = a.getPomPath();
					try {
						SAXParserFactory spf = SAXParserFactory.newInstance();

						File f = new File("db/" + path);
						spf.newSAXParser().parse(f, new DefaultHandler() {
							private Dependency dep;
							private String value;

							@Override
							public void startElement(String uri,
									String localName, String qName,
									Attributes attributes) throws SAXException {
								if (qName.equals("dependency")) {
									dep = new Dependency();
								}
							}

							@Override
							public void characters(char[] ch, int start,
									int length) throws SAXException {
								value = new String(ch, start, length);
							}

							@Override
							public void endElement(String uri,
									String localName, String qName)
									throws SAXException {
								if (qName.equals("dependency")) {
									out.format("%s, %s, %s, %s, %s\n",
											a.groupId, a.artifactId,
											dep.groupId, dep.artifactId,
											dep.version);

									a.dependencies.add(dep);
									dep = null;
								} else if (dep != null
										&& qName.equals("groupId")) {
									dep.groupId = value;
								} else if (dep != null
										&& qName.equals("artifactId")) {
									dep.artifactId = value;
								} else if (dep != null
										&& qName.equals("version")) {
									dep.version = value;
								}
							}
						});
					} catch (FileNotFoundException e) {
						log.log("POM not found %s", path);
					} catch (Exception e) {
						log.log("Exception (%s) in %s: %s", e.getClass(), path,
								e.getMessage());
					}
				}
			}
		}
	}
}
