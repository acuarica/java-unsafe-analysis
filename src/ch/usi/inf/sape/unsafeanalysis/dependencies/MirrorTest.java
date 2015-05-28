package ch.usi.inf.sape.unsafeanalysis.dependencies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

public class MirrorTest {

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

	private static void depsTest(String outFileName, String outListFileName,
			boolean all) throws FileNotFoundException, IOException {
		Map<String, Set<String>> ds = new HashMap<String, Set<String>>();
		Map<String, Set<String>> bs = new HashMap<String, Set<String>>();

		File file = new File("db/maven-depgraph.csv");

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			reader.readLine();

			String text;
			while ((text = reader.readLine()) != null) {
				String[] fs = text.split(",");

				if (fs.length >= 6) {
					String id = fs[0].trim() + ":" + fs[1].trim();
					String did = fs[2].trim() + ":" + fs[3].trim();

					String scope = fs[5].trim();

					// System.out.println(scope);
					if (all || !scope.equals("test")) {
						Set<String> s = ds.get(did);
						if (s == null) {
							s = new HashSet<String>();
							ds.put(did, s);

							bs.put(did, new HashSet<String>());
						}

						s.add(id);
					}
				}
			}

			System.out.format("Artifacts %d\n", bs.size());

			for (String did : bs.keySet()) {
				doDeps(did, ds, bs);
			}

			try (PrintStream out = new PrintStream(outFileName)) {
				out.println("depId, depCount");
				for (Entry<String, Set<String>> e : bs.entrySet()) {
					String did = e.getKey();
					Set<String> deps = e.getValue();
					out.format("%s, %d\n", did, deps.size());
				}
			}

			try (PrintStream out = new PrintStream(outListFileName)) {
				out.println("depId, depCount");
				for (Entry<String, Set<String>> e : bs.entrySet()) {
					String did = e.getKey();
					Set<String> deps = e.getValue();

					for (String id : deps) {
						out.format("%s, %s\n", did, id);
					}
				}
			}
		}
	}

	@Test
	public void depsAllScopeTest() throws IOException {
		depsTest("db/maven-invdeps-all.csv", "db/maven-invdeps-all-list.csv",
				true);
	}

	@Test
	public void depsProductionScopeTest() throws IOException {
		depsTest("db/maven-invdeps-production.csv",
				"db/maven-invdeps-production-list.csv", false);
	}
}
