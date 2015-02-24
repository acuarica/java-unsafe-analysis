package ch.usi.inf.sape.mavendb.tests;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;
import org.junit.Test;

import ch.usi.inf.sape.mavendb.MavenDBProperties;

public class IndexSearcherTest {

	private static void show(IndexSearcher searcher, String term,
			PrintStream out) throws CorruptIndexException, IOException {
		out.format("docs: %d\n", searcher.maxDoc());
		
		for (int i = 0; i < searcher.maxDoc(); i++) {
			Document doc = searcher.doc(i);

			@SuppressWarnings("unchecked")
			List<Field> fs = (List<Field>) doc.getFields();

			String docText = "";
			boolean found = false;
			for (Field f : fs) {
				String name = f.name();
				String value = doc.get(name);
				docText += name + "=" + value + " ";

				if (value.toLowerCase().contains(term)
						&& !name.contains("allGroupsList")) {
					found = true;
				}
			}

			if (found) {
				out.println(docText);
			}

			if (i >= 5000) {
				break;
			}
		}
	}

	@Test
	public void showGoogleTest() throws CorruptIndexException, IOException {
		String indexPath = MavenDBProperties.get().indexPath();
		IndexSearcher searcher = new IndexSearcher(indexPath);

		show(searcher, "google", System.out);
	}

	@Test
	public void showApacheTest() throws CorruptIndexException, IOException {
		String indexPath = MavenDBProperties.get().indexPath();
		IndexSearcher searcher = new IndexSearcher(indexPath);

		show(searcher, "apache", System.out);
	}
}
