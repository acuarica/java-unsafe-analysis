package ch.usi.inf.sape.unsafeanalysis.index;

import java.util.Date;

/**
 * 
 * @author Luis Mastrangelo (luis.mastrangelo@usi.ch)
 *
 */
public class MavenRecord {
	public final String allGroups;
	public final String allGroupsList;
	public final String rootGroups;
	public final String rootGroupsList;
	public final String descriptor;
	public final String idxinfo;
	public final String sha;
	public final String m;
	public final String u;
	public final String i;
	public final String del;
	public final String gdesc;
	public final String adesc;
	public final Date mdate;
	public final String gid;
	public final String aid;
	public final String ver;
	public final String sat;
	public final String is0;
	public final Date idate;
	public final long size;
	public final int is3;
	public final int is4;
	public final int is5;
	public final String ext;

	public MavenRecord(NexusRecord doc) {
		allGroups = doc.get("allGroups");
		allGroupsList = doc.get("allGroupsList");
		rootGroups = doc.get("rootGroups");
		rootGroupsList = doc.get("rootGroupsList");
		descriptor = doc.get("DESCRIPTOR");
		idxinfo = doc.get("IDXINFO");
		sha = doc.get("1");
		m = doc.get("m");
		u = doc.get("u");
		i = doc.get("i");
		del = doc.get("del");
		gdesc = doc.get("n");
		adesc = doc.get("d");

		check((allGroups == null) == (allGroupsList == null),
				"Invalid all groups doc: " + doc);

		check((rootGroups == null) == (rootGroupsList == null),
				"Invalid root groups doc: " + doc);

		check((descriptor == null) == (idxinfo == null),
				"Invalid description/idxinfo doc: " + doc);

		check(((allGroups == null) && (rootGroups == null) && (descriptor == null)) == (m != null),
				"null m: " + doc);

		check((u == null) || (m != null), "u and m: " + doc);
		check((i == null) || (m != null), "i and m: " + doc);
		check((u != null) == (i != null), "u and i: " + doc);

		check((del == null) || (m != null), "del and m: " + doc);
		check((sha == null) || (m != null), "one and m: " + doc);

		if (allGroups != null) {
			check(allGroups.equals("allGroups"), "allGroups: %s", doc);
		}

		if (rootGroups != null) {
			check(rootGroups.equals("rootGroups"), "rootGroups: %s", doc);
		}

		if (descriptor != null) {
			check(descriptor.equals("NexusIndex"), "NexusIndex: %s", doc);
		}

		mdate = m != null ? checkDate(m) : null;

		if (i != null) {
			String[] us = u.split("\\|");

			check(us.length == 4 || us.length == 5,
					"Invalid value for u field: %s", doc);

			gid = us[0];
			aid = us[1];
			ver = us[2];
			sat = us[3];

			check(us.length != 4 || "NA".equals(sat), "Expected NA");

			String[] is = i.split("\\|");
			check(is.length == 7, "Invalid i: %s", doc);

			is0 = is[0];

			check(us.length == 4 || us[4].equals(is0), "us4 and is0: %s", doc);

			idate = checkDate(is[1]);
			size = checkSignedLong(is[2]);
			check(size >= -1, "Size more negative: %s", doc);

			is3 = checkDigit(is[3]);
			is4 = checkDigit(is[4]);
			is5 = checkDigit(is[5]);

			ext = is[6];

			check(!is0.equals("null") || (size == -1 && ext.equals("pom")),
					"size/no jar and null: %s", doc);
		} else {
			gid = null;
			aid = null;
			ver = null;
			sat = null;
			is0 = null;
			idate = null;
			size = 0;
			is3 = 0;
			is4 = 0;
			is5 = 0;
			ext = null;
		}
	}

	private static int checkDigit(String s) {
		check(s.matches("[0-9]"), "Invalid digit: %s", s);
		return Integer.parseInt(s);
	}

	private static long checkSignedLong(String s) {
		check(s.matches("-?[0-9]+"), "Invalid signed long: %s", s);
		return Long.parseLong(s);
	}

	private static Date checkDate(String s) {
		check(s.matches("[0-9]{13}"), "Invalid date: %s", s);
		return new Date(Long.parseLong(s));
	}

	private static void check(boolean cond, String message, Object... args) {
		if (!cond) {
			throw new AssertionError(String.format(message, args));
		}
	}
}
