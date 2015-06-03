package ch.usi.inf.sape.unsafeanalysis.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.usi.inf.sape.unsafeanalysis.index.PomDependency;

public class DepsManager {

	public static List<PomDependency> extractDeps(String pomPath)
			throws SAXException, IOException, ParserConfigurationException {
		SAXParserFactory spf = SAXParserFactory.newInstance();

		final List<PomDependency> deps = new ArrayList<PomDependency>();

		File f = new File(pomPath);
		spf.newSAXParser().parse(f, new DefaultHandler() {
			private PomDependency dep;
			private String value;

			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				if (qName.equals("dependency")) {
					dep = new PomDependency();
				}
			}

			@Override
			public void characters(char[] ch, int start, int length)
					throws SAXException {
				value = new String(ch, start, length);
			}

			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				if (qName.equals("dependency")) {
					deps.add(dep);
					dep = null;
				} else if (dep != null && qName.equals("groupId")) {
					dep.groupId = value;
				} else if (dep != null && qName.equals("artifactId")) {
					dep.artifactId = value;
				} else if (dep != null && qName.equals("version")) {
					dep.version = value;
				} else if (dep != null && qName.equals("scope")) {
					dep.scope = value;
				}
			}
		});

		return deps;
	}
}
