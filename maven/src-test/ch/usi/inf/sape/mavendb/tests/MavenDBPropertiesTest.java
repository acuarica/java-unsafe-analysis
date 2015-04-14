package ch.usi.inf.sape.mavendb.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

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

	@Test
	public void main() {
//		HashSet<String> sets = new HashSet<String>();
//		sets.add("");
//		System.out.println(sets.size());

		HashSet<Integer> set = new HashSet<Integer>();
		set.add(2);
		set.add(4);
		set.add(4);
		set.add(4);
		System.out.println(set.size());

		// ArrayList<String> s = new ArrayList<String>();
		//
		// Iterator<String> i = s.iterator();
		// s.addAll(Arrays.asList("A", "B", "C", "D"));
		//
		// // if (i.hasNext()) {
		// String item = i.next();
		// // }
		//
		// System.out.println(s);
	}
}
