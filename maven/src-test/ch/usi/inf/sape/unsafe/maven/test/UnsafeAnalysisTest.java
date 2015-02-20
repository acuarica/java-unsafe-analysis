package ch.usi.inf.sape.unsafe.maven.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import ch.usi.inf.sape.unsafe.maven.Log;
import ch.usi.inf.sape.unsafe.maven.UnsafeAnalysis;
import ch.usi.inf.sape.unsafe.maven.UnsafeAnalysis.UnsafeEntry;

public class UnsafeAnalysisTest {

	private static final String testJars = "tests/";

	private static Log log = new Log(System.err);

	private static List<UnsafeEntry> testJar(String jarFileName,
			boolean expected) throws IOException {
		log.log("Testing %s", jarFileName);

		List<UnsafeEntry> matches = UnsafeAnalysis.searchJarFile(testJars
				+ jarFileName, null);

		assertEquals(expected, matches.size() > 0);

		UnsafeAnalysis.printMatchesCsv(System.out, matches);

		return matches;
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
	public void testJruby() throws IOException {
		testJar("jruby-mvm.jar", true);
	}

	@Test
	public void testWithUnsafe() throws IOException {
		List<UnsafeEntry> matches = testJar("with-unsafe.jar", true);

		boolean literal = false;
		for (UnsafeEntry entry : matches) {
			if (entry.name.equals("sun/misc/Unsafe")
					&& entry.desc.equals("literal")) {
				literal = true;
			}
		}

		assertTrue(literal);
	}

	@Test
	public void testAsm() throws IOException {
		testJar("asm-3.1.jar", false);
	}

	@Test
	public void testJython() throws IOException {
		testJar("jython.jar", false);
	}
}
