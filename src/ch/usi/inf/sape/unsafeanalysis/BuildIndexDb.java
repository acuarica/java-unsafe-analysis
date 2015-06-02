package ch.usi.inf.sape.unsafeanalysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndex;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndexBuilder;
import ch.usi.inf.sape.unsafeanalysis.index.NexusIndexParser;

public class BuildIndexDb {

	private static final Logger logger = Logger.getLogger(BuildIndexDb.class);

	private static String getResourceContent(String path) throws IOException {
		ClassLoader cl = BuildIndexDb.class.getClassLoader();
		InputStream in = cl.getResourceAsStream(path);

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder out = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null) {
			out.append(line);
		}

		return out.toString();
	}

	private static void extract(String indexPath, Connection c)
			throws Exception {
		NexusIndexParser nip = new NexusIndexParser(indexPath);
		MavenIndex index = MavenIndexBuilder.build(nip);

		for (MavenArtifact a : index) {
			String sql = String
					.format("insert into arts (gid, aid, path) values ('%s', '%s', '%s')",
							a.groupId, a.artifactId, a.getPath());
			Statement stmt = c.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			throw new Exception(
					"Invalid arguments: needed index path, uri list, number of artifacts to download (or - for no limit), and at least on mirror");
		}

		String indexPath = args[0];
		String dbPath = args[1];

		logger.info("Index: " + indexPath);
		logger.info("DB path: " + dbPath);

		String sql = getResourceContent("db.sql");
		System.out.println(sql);
		Class.forName("org.sqlite.JDBC");

		Connection c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

		Statement stmt = c.createStatement();
		stmt.executeUpdate(sql);
		stmt.close();

		extract(indexPath, c);
	}
}
