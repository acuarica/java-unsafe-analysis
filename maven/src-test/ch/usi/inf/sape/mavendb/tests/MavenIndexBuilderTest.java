package ch.usi.inf.sape.mavendb.tests;

import org.apache.lucene.search.IndexSearcher;
import org.junit.Test;

import ch.usi.inf.sape.mavendb.MavenDBProperties;
import ch.usi.inf.sape.mavendb.MavenIndexBuilder;
import ch.usi.inf.sape.mavendb.nexus.NexusIndexParser;
import ch.usi.inf.sape.mavendb.nexus.NexusRecord;

public class MavenIndexBuilderTest {

	@Test
	public void checkTest() throws Exception {
		final String indexPath = MavenDBProperties.get().indexPath();
		final IndexSearcher searcher = new IndexSearcher(indexPath);

		MavenIndexBuilder.build(searcher);
	}

	@Test
	public void parserTest() throws Exception {

		long c = 0;
		try (NexusIndexParser nip = new NexusIndexParser(
				"nexus/nexus-maven-repository-index")) {
			for (NexusRecord nr : nip) {
				c++;
				if (c % 100000 == 0)
					System.out.println(c);

				//System.out.println(nr);
			}
		}
		System.out.println(c);
	}
}
