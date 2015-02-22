package ch.usi.inf.sape.mavendb.tests;

import java.io.IOException;

import org.junit.Test;

import ch.usi.inf.sape.mavendb.MavenDBProperties;
import ch.usi.inf.sape.util.Log;

public class MavenDBPropertiesTest {

	private final Log log = new Log(System.err);

	@Test
	public void getPropertiesTest() throws IOException {
		MavenDBProperties prop = MavenDBProperties.get();

		log.log("indexPath: %s", prop.indexPath());
		log.log("repoPath: %s", prop.repoPath());
		log.log("downloaderRetries: %d", prop.downloaderRetries());
		log.log("downloaderThreadsPerMirror: %d",
				prop.downloaderThreadsPerMirror());

		for (String mirrorUrl : prop.downloaderMirrorList()) {
			log.log("mirror: %s", mirrorUrl);
		}
	}
}
