package ch.usi.inf.sape.unsafeanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;
import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact.Dependency;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndex;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndexBuilder;
import ch.usi.inf.sape.unsafeanalysis.index.NexusIndexParser;

public class ExtractGraph {

	private static final Logger logger = Logger.getLogger(ExtractGraph.class);

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			throw new Exception("Invalid arguments: needed index path");
		}

		String indexPath = args[0];

		NexusIndexParser nip = new NexusIndexParser(indexPath);
		MavenIndex index = MavenIndexBuilder.build(nip);

		String localPathCsv = "db/depgraph.csv";
		try (PrintStream out = new PrintStream(localPathCsv)) {

			out.println("groupId, artifactId, depGroupId, depArtifactId, depVersion, depScope");

			for (final MavenArtifact a : index) {
				String path = a.getPomPath();
				try {
					SAXParserFactory spf = SAXParserFactory.newInstance();

					File f = new File("db/" + path);
					spf.newSAXParser().parse(f, new DefaultHandler() {
						private Dependency dep;
						private String value;

						@Override
						public void startElement(String uri, String localName,
								String qName, Attributes attributes)
								throws SAXException {
							if (qName.equals("dependency")) {
								dep = new Dependency();
							}
						}

						@Override
						public void characters(char[] ch, int start, int length)
								throws SAXException {
							value = new String(ch, start, length);
						}

						@Override
						public void endElement(String uri, String localName,
								String qName) throws SAXException {
							if (qName.equals("dependency")) {
								out.format("%s, %s, %s, %s, %s, %s\n",
										a.groupId, a.artifactId, dep.groupId,
										dep.artifactId, dep.version, dep.scope);

								a.dependencies.add(dep);
								dep = null;
							} else if (dep != null && qName.equals("groupId")) {
								dep.groupId = value;
							} else if (dep != null
									&& qName.equals("artifactId")) {
								dep.artifactId = value;
							} else if (dep != null && qName.equals("version")) {
								dep.version = value;
							} else if (dep != null && qName.equals("scope")) {
								dep.scope = value;
							}
						}
					});
				} catch (FileNotFoundException e) {
					logger.info("POM not found " + path);
				} catch (Exception e) {
					logger.info("Exception in " + path, e);
				}
			}
		}
	}
}
