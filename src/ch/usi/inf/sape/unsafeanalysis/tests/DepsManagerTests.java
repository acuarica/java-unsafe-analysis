package ch.usi.inf.sape.unsafeanalysis.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ch.usi.inf.sape.unsafeanalysis.analysis.DepsManager;
import ch.usi.inf.sape.unsafeanalysis.index.PomDependency;

public class DepsManagerTests {

	private static void testPom(String pomPath, int expected)
			throws IOException, SAXException, ParserConfigurationException {
		List<PomDependency> deps = DepsManager.extractDeps("tests/poms/"
				+ pomPath);

		assertEquals(expected, deps.size());
	}

	@Test
	public void testHttpClient() throws IOException, SAXException,
			ParserConfigurationException {
		testPom("HTTPClient-0.3-3.pom", 0);
	}

	@Test
	public void testCalcite0() throws IOException, SAXException,
			ParserConfigurationException {
		testPom("calcite-0.9.1-incubating.pom", 31);
	}

	@Test
	public void testCalcite1() throws IOException, SAXException,
			ParserConfigurationException {
		testPom("calcite-1.0.0-incubating.pom", 32);
	}

	@Test
	public void testCatalina() throws IOException, SAXException,
			ParserConfigurationException {
		testPom("catalina-5.5.23.pom", 1);
	}

	@Test
	public void testApiServices() throws IOException, SAXException,
			ParserConfigurationException {
		testPom("google-api-services-admin-directory_v1-rev10-1.16.0-rc.pom", 1);
	}

	@Test
	public void testMavenProjects() throws IOException, SAXException,
			ParserConfigurationException {
		testPom("maven-project-3.0-alpha-1.pom", 10);
	}

	@Test
	public void testPinus() throws IOException, SAXException,
			ParserConfigurationException {
		testPom("pinus-orm-1.1.0.pom", 16);
	}

	@Test
	public void testSprintCore() throws IOException, SAXException,
			ParserConfigurationException {
		testPom("spring-core-4.1.4.RELEASE.pom", 5);
	}

	@Test(expected = SAXParseException.class)
	public void testWfs11() throws IOException, SAXException,
			ParserConfigurationException {
		testPom("wfs11-1.7.0.pom", 7);
	}
}
