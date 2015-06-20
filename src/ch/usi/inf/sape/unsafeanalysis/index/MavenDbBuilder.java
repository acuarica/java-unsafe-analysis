package ch.usi.inf.sape.unsafeanalysis.index;

import java.sql.Connection;
import java.util.Arrays;

import ch.usi.inf.sape.unsafeanalysis.log.Log;

public class MavenDbBuilder {

	private static final Log log = new Log(System.out);

	public static void build(String indexPath, Connection c) throws Exception {
		Inserter artins = new Inserter(
				c,
				"insert into art (mdate, sha, gid, aid, ver, sat, is0, idate, size, is3, is4, is5, ext, gdesc, adesc, path, inrepo) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)");

		Inserter allins = new Inserter(c,
				"insert into allgroups (value) values (?)");

		Inserter rootins = new Inserter(c,
				"insert into rootgroups (value) values (?)");

		try (NexusIndexParser nip = new NexusIndexParser(indexPath)) {
			for (NexusRecord doc : nip) {
				MavenRecord mr = new MavenRecord(doc);

				if (mr.allGroups != null) {
					log.info("Inserting allGroups...");

					for (String g : Arrays
							.asList(mr.allGroupsList.split("\\|"))) {
						allins.insert(g);
					}
				} else if (mr.rootGroups != null) {
					log.info("Inserting rootGroups...");

					for (String g : Arrays.asList(mr.rootGroupsList
							.split("\\|"))) {
						rootins.insert(g);
					}
				} else if (mr.descriptor != null) {
					log.info("Inserting prop:descriptor...");

					new Inserter(c,
							"insert into prop (key, value) values (?, ?)")
							.insert(mr.descriptor, mr.idxinfo);
				} else if (mr.i != null) {
					MavenArtifact a = new MavenArtifact(mr.gid, mr.aid, mr.ver,
							mr.size, mr.ext, mr.gdesc, mr.adesc, mr.sat, mr.sha);

					artins.insert(mr.mdate, mr.sha, mr.gid, mr.aid, mr.ver,
							mr.sat, mr.is0, mr.idate, mr.size, mr.is3, mr.is4,
							mr.is5, mr.ext, mr.gdesc, mr.adesc, a.getPath());

					// if (us.length == 4
					// && Arrays.asList("jar", "ejb", "war", "ear").contains(
					// ext))

					// if (index.map.containsKey(id)) {
					// MavenArtifact b = index.map.get(id);
					// index.lastVersionJarsSize -= b.size;
					//
					// a = b.max(a);
					// }
					//
					// index.lastVersionJarsSize += a.size;
					// index.map.put(id, a);
				}

				// if (one != null &&
				// !one.toLowerCase().matches("[0-9a-f]{40}")) {
				// System.out.println(docText);
				// }
			}
		}
	}
}
