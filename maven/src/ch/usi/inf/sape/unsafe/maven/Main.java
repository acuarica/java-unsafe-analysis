package ch.usi.inf.sape.unsafe.maven;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;

public class Main {

	private static void log(Object message) {
		System.err.println(message);
	}

	private static boolean isSha(String text) {
		return text.matches("[0-9a-f]{40}");
	}

	private static int compare(String[] ls, String[] rs, int i) {
		try {
			return Integer.parseInt(ls[i]) - Integer.parseInt(rs[i]);
		} catch (NumberFormatException e) {
			return ls[i].compareTo(rs[i]);
		}
	}

	private static String maxVersion(String lhs, String rhs) throws Exception {
		String[] ls = lhs.split("\\.");
		String[] rs = rhs.split("\\.");

		for (int i = 0; i < Math.min(ls.length, rs.length); i++) {
			int res = compare(ls, rs, i);
			if (res > 0) {
				return lhs;
			} else if (res < 0) {
				return rhs;
			}
		}

		// if (ls.length == rs.length && !lhs.equals(rhs)) {
		// throw new Exception(lhs + " ? " + rhs);
		// }

		return ls.length > rs.length ? lhs : rhs;
	}

	private static void dumpMap(HashMap<String, String> map) {
		for (Entry<String, String> entry : map.entrySet()) {
			System.out.println(entry.getKey() + "/" + entry.getValue());
		}
	}

	@SuppressWarnings("deprecation")
	private static void checkIndex(IndexSearcher searcher)
			throws CorruptIndexException, IOException {
		Date mmindate = null;
		Date mmaxdate = null;
		Date imindate = null;
		Date imaxdate = null;

		for (int docIndex = 0; docIndex < searcher.maxDoc(); docIndex++) {
			Document doc = searcher.doc(docIndex);

			@SuppressWarnings("unchecked")
			List<Field> fields = (List<Field>) doc.getFields();

			String docText = "";
			for (Field f : fields) {
				String name = f.name();
				String value = doc.get(name);
				docText += name + "=" + value + " ";
			}

			String allGroups = doc.get("allGroups");
			if (allGroups != null) {
				assert allGroups.equals("allGroups");
			}

			String allGroupsList = doc.get("allGroupsList");

			assert (allGroups == null) == (allGroupsList == null) : "Invalid all groups doc: "
					+ docText;

			String rootGroups = doc.get("rootGroups");
			if (rootGroups != null) {
				assert rootGroups.equals("rootGroups");
			}

			String rootGroupsList = doc.get("rootGroupsList");

			assert (rootGroups == null) == (rootGroupsList == null) : "Invalid root groups doc: "
					+ docText;

			String descriptor = doc.get("DESCRIPTOR");
			if (descriptor != null) {
				assert descriptor.equals("NexusIndex");
			}

			String idxinfo = doc.get("IDXINFO");

			assert (descriptor == null) == (idxinfo == null) : "Invalid description/idxinfo doc: "
					+ docText;

			String m = doc.get("m");
			assert ((allGroups == null) && (rootGroups == null) && (descriptor == null)) == (m != null) : "null m: "
					+ docText;
			if (m != null) {
				assert m.matches("[0-9]{13}") : "Invalid m: " + docText;
				Date date = new Date(Long.parseLong(m));

				if (mmindate == null) {
					mmindate = date;
					mmaxdate = date;
				} else {
					mmindate = date.compareTo(mmindate) < 0 ? date : mmindate;
					mmaxdate = date.compareTo(mmaxdate) > 0 ? date : mmaxdate;
				}
			}

			String u = doc.get("u");
			if (u != null) {
				String[] us = u.split("\\|");

				assert us.length == 4 || us.length == 5 : "Invalid value for u field";
				assert us.length != 4 || "NA".equals(us[3]) : "Expected NA";

				if (us.length == 4 && "NA".equals(us[3])) {
					String key = us[0].replace('.', '/') + "/" + us[1];
					// if (!map.containsKey(key)) {
					// map.put(key, us[2]);
					// } else {
					// String v = maxVersion(map.get(key), us[2]);
					// map.put(key, v);
					// }
					// System.out.println(us[0].replace('.', '/') + "/" + us[1]
					// + us[2]);
				}
			}

			String i = doc.get("i");

			if (i != null) {
				String[] is = i.split("\\|");
				assert is.length == 7 : "Invalid i: " + docText;
				assert is[1].matches("[0-9]{13}") : "Invalid i subfield 1: "
						+ docText;
				Date date = new Date(Long.parseLong(is[1]));
				if (imindate == null) {
					imindate = date;
					imaxdate = date;
				} else {
					imindate = date.compareTo(imindate) < 0 ? date : imindate;
					imaxdate = date.compareTo(imaxdate) > 0 ? date : imaxdate;
				}

				assert is[2].matches("-?[0-9]+") : "Invalid i subfield 2: "
						+ docText;
				assert is[3].matches("[0-9]") : "Invalid i subfield 3: "
						+ docText;
				assert is[4].matches("[0-9]") : "Invalid i subfield 4: "
						+ docText;
				assert is[5].matches("[0-9]") : "Invalid i subfield 5: "
						+ docText;
			}

			// String one = doc.get("1");
			// assert one != null: "null: " +docText;
			// if (one != null) {
			// assert isSha(one) : "Not a SHA1 value in field 1: doc: "
			// + docText;
			// }
		}

		assert mmindate.getYear() + 1900 == 2011 : "Unexpected mmindate: "
				+ mmindate;
		assert mmaxdate.getYear() + 1900 == 2015 : "Unexpected mmaxdate: "
				+ mmaxdate;

		assert imindate.getYear() + 1900 == 2002 : "Unexpected imindate: "
				+ imindate;
		assert imaxdate.getYear() + 1900 == 2015 : "Unexpected imaxdate: "
				+ imaxdate;
	}

	private static void printIndex(IndexSearcher searcher, String term,
			PrintStream out) throws CorruptIndexException, IOException {
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

				if (value.toLowerCase().contains(term)) {
					found = true;
				}
			}

			if (found) {
				out.println(docText);
			}
		}
	}

	private static HashMap<String, String> buildMap(IndexSearcher searcher)
			throws Exception {
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < searcher.maxDoc(); i++) {
			Document doc = searcher.doc(i);

			// @SuppressWarnings("unchecked")
			// List<Field> fs = (List<Field>) doc.getFields();

			// String docText = "";
			// boolean found = false;
			// for (Field f : fs) {
			// String name = f.name();
			// String value = doc.get(name);
			String u = doc.get("u");

			if (u != null) {
				// if (name.equals("u")) {
				String[] us = u.split("\\|");

				if (us.length != 4 && us.length != 5) {
					throw new Exception(us[4] + " --- " + u);
				}

				if (us.length == 4 && !"NA".equals(us[3])) {
					throw new Exception(u + " --- " + us[3]);
				}

				if (us.length == 4 && "NA".equals(us[3])) {
					String key = us[0].replace('.', '/') + "/" + us[1];
					if (!map.containsKey(key)) {
						map.put(key, us[2]);
					} else {
						String v = maxVersion(map.get(key), us[2]);
						map.put(key, v);
					}
					// System.out.println(us[0].replace('.', '/') + "/" + us[1]
					// + us[2]);
				}
			}
		}

		return map;
	}

	public static void main(String[] args) throws Exception {
		IndexSearcher searcher = new IndexSearcher("index/");

		log("Analysing database with " + searcher.maxDoc() + " documents...");

		checkIndex(searcher);
		// printIndex(searcher, "", System.out);
		// HashMap<String, String> map = buildMap(searcher);

		log("Dumping map...");

		// dumpMap(map);
	}
}
