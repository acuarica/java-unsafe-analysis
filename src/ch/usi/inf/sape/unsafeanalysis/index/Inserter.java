package ch.usi.inf.sape.unsafeanalysis.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class Inserter {

	private PreparedStatement stmt;

	public Inserter(Connection conn, String sql) throws SQLException {
		stmt = conn.prepareStatement(sql);
	}

	public void insert(Object... values) {
		try {

			for (int i = 0; i < values.length; i++) {
				stmt.setObject(i + 1, values[i]);
			}

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
