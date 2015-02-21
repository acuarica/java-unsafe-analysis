package ch.usi.inf.sape.mavendb.tests;

import org.apache.lucene.search.IndexSearcher;
import org.junit.Test;

import ch.usi.inf.sape.mavendb.MavenDBProperties;
import ch.usi.inf.sape.mavendb.MavenIndexBuilder;

public class MavenIndexBuilderTest {

	@Test
	public void checkTest() throws Exception {
		final String indexPath = MavenDBProperties.get().indexPath();
		final IndexSearcher searcher = new IndexSearcher(indexPath);

		MavenIndexBuilder.build(searcher);
	}
}
