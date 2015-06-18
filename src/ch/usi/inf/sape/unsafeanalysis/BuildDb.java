package ch.usi.inf.sape.unsafeanalysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import ch.usi.inf.sape.unsafeanalysis.argsparser.Arg;
import ch.usi.inf.sape.unsafeanalysis.argsparser.ArgsParser;
import ch.usi.inf.sape.unsafeanalysis.index.MavenDbBuilder;
import ch.usi.inf.sape.unsafeanalysis.log.Log;

public class BuildDb {

	private static final Log log = new Log(System.out);

	public static class Args {

		@Arg(shortkey = "i", longkey = "index", desc = "Specifies the path of the Nexus Index.")
		public String indexPath;

		@Arg(shortkey = "d", longkey = "db", desc = "Specifies the path of the db file.")
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

	public static void main(String[] args) throws Exception {
		Args ar = ArgsParser.parse(args, Args.class);

		log.info("Index: %s", ar.indexPath);
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
		c.commit();
	}
}
