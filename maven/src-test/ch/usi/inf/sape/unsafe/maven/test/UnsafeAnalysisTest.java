package ch.usi.inf.sape.unsafe.maven.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import ch.usi.inf.sape.unsafe.maven.Log;
import ch.usi.inf.sape.unsafe.maven.UnsafeAnalysis;
import ch.usi.inf.sape.unsafe.maven.UnsafeAnalysis.UnsafeEntry;

public class UnsafeAnalysisTest {

	private static final String testJars = "tests/";

	private static Log log = new Log(System.err);

	private static void testJar(String jarFileName, boolean expected)
			throws IOException {
		log.log("Testing %s", jarFileName);

		List<UnsafeEntry> matches = UnsafeAnalysis.searchJarFile(testJars
				+ jarFileName);

		assertEquals(expected, matches.size() > 0);

		UnsafeAnalysis.printMatchesCsv(System.out, matches);
	}

	@Test
	public void testElasticSearch() throws IOException {
		testJar("elasticsearch-1.4.3.jar", true);

	}

	@Test
	public void testKryo() throws IOException {
		testJar("kryo-2.23.0.jar", true);
	}

	@Test
	public void testAsm() throws IOException {
		testJar("asm-3.1.jar", false);
	}
}
