package ch.usi.inf.sape.unsafeanalysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ch.usi.inf.sape.unsafeanalysis.analysis.DepsManager;
import ch.usi.inf.sape.unsafeanalysis.argsparser.Arg;
import ch.usi.inf.sape.unsafeanalysis.argsparser.ArgsParser;
import ch.usi.inf.sape.unsafeanalysis.index.Inserter;
import ch.usi.inf.sape.unsafeanalysis.index.MavenArtifact;
import ch.usi.inf.sape.unsafeanalysis.index.MavenDbBuilder;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndex;
import ch.usi.inf.sape.unsafeanalysis.index.MavenIndexBuilder;
import ch.usi.inf.sape.unsafeanalysis.index.NexusIndexParser;
import ch.usi.inf.sape.unsafeanalysis.index.PomDependency;
import ch.usi.inf.sape.unsafeanalysis.log.Log;

public class BuildDb {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(shortkey = "i", longkey = "index", desc = "Specifies the path of the Nexus Index.")
		public String indexPath;

		@Arg(shortkey = "r", longkey = "repo", desc = "Specifies the path of the Maven repository.")
		public String repoPath;

		@Arg(shortkey = "d", longkey = "db", desc = "Specifies the path of the output db file.")
		public String dbPath;

	}

	private static String getResourceContent(String path) throws IOException {
		ClassLoader cl = BuildDb.class.getClassLoader();
		InputStream in = cl.getResourceAsStream(path);

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder out = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null) {
			out.append(line);
		}

		return out.toString();
	}

	private static void deps(Args ar, Connection c) throws Exception {

		log.info("Using Index: %s", ar.indexPath);

		log.info("Parsing Index...");

		NexusIndexParser nip = new NexusIndexParser(ar.indexPath);
		MavenIndex index = MavenIndexBuilder.build(nip);

		Inserter ins = new Inserter(
				c,
				"insert into dep (gid, aid, ver, dgid, daid, dver, dscope) values (?, ?, ?, ?, ?, ?, ?)");

		int i = 0;
		for (final MavenArtifact a : index) {
			i++;

			String path = a.getPomPath();

			List<PomDependency> deps;
			try {
				deps = DepsManager.extractDeps(ar.repoPath + "/" + path);
				try {

					for (PomDependency dep : deps) {
						ins.insert(a.groupId, a.artifactId, a.version,
								dep.groupId, dep.artifactId, dep.version,
								dep.scope);
					}
				} catch (SQLException e) {
					System.out.println(a);
					System.out.println(deps);
					// log.info("SQL Exception in %s (# %d): %s", path, i, e);
					throw e;
				}
			} catch (SAXException | IOException | ParserConfigurationException e) {
				log.info("Exception in %s (# %d): %s", path, i, e);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		log.info("Index: %s", ar.indexPath);
		log.info("Repo path: %s ", ar.repoPath);
		log.info("DB path: %s ", ar.dbPath);

		String sql = getResourceContent("db.sql");
		log.info("SQL: %s ", sql);

		Class.forName("org.sqlite.JDBC");

		Connection c = DriverManager.getConnection("jdbc:sqlite:" + ar.dbPath);

		Statement stmt = c.createStatement();
		stmt.executeUpdate(sql);
		stmt.close();

		c.setAutoCommit(false);
		MavenDbBuilder.build(ar.indexPath, c);
		deps(ar, c);
		c.commit();
	}
}
