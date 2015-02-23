package ch.usi.inf.sape.unsafe.maven.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import ch.usi.inf.sape.util.Mirror;

public class MirrorTest {

	@Test
	public void idTest() throws Exception {

		System.out.format("%2d%%\n", 20 * 100 / 1000);
		System.out.format("%2d%%\n", 55 * 100 / 1000);
		System.out.format("%2d%%\n", 99 * 100 / 1000);
		System.out.format("%2d%%\n", 999 * 100 / 1000);
		System.out.format("%2d%%\n", 1000 * 100 / 1000);

		Mirror m = new Mirror("http://mirrors.ibiblio.org/maven2/", 10);

		assertEquals("ibiblio", m.id);
	}

	private static Set<String> doDeps(String did, Map<String, Set<String>> ds,
			Map<String, Set<String>> bs) {

		Set<String> s = bs.get(did);

		if (s != null && s.size() == 0) {
			s.add(did);

			for (String id : ds.get(did)) {
				Set<String> t = doDeps(id, ds, bs);
				if (t != null) {
					s.addAll(t);
				}
			}
		}

		return s;
	}

	@Test
	public void depsTest() throws IOException {
		Map<String, Set<String>> ds = new HashMap<String, Set<String>>();
		Map<String, Set<String>> bs = new HashMap<String, Set<String>>();

		File file = new File("db/maven-depgraph.csv");

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			reader.readLine();

			String text;
			while ((text = reader.readLine()) != null) {
				String[] fs = text.split(",");

				if (fs.length >= 4) {
					String id = fs[0].trim() + ":" + fs[1].trim();
					String did = fs[2].trim() + ":" + fs[3].trim();

					Set<String> s = ds.get(did);
					if (s == null) {
						s = new HashSet<String>();
						ds.put(did, s);

						bs.put(did, new HashSet<String>());
					}

					s.add(id);
				}
			}

			System.out.format("Artifacts %d\n", bs.size());

			for (String did : bs.keySet()) {
				doDeps(did, ds, bs);
			}

			try (PrintStream out = new PrintStream("db/maven-invdeps.csv")) {
				out.println("depId, depCount");
				for (Entry<String, Set<String>> e : bs.entrySet()) {
					out.format("%s, %d\n", e.getKey(), e.getValue().size());
				}
			}
		}
	}
}
