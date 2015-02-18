package ch.usi.inf.sape.unsafe.maven;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;

public class MavenIndex {

	public List<String> rootGroupsList;
	public long totalSize = 0;
	public long lastVersionJarsSize = 0;
	public long uniqueArtifactsCount = 0;
	public Date mmindate = null;
	public Date mmaxdate = null;
	public Date imindate = null;
	public Date imaxdate = null;
	public final HashMap<String, Artifact> map = new HashMap<String, Artifact>();
	public final HashSet<String> extSet = new HashSet<String>();

	public static class Artifact {

		public String groupId;
		public String artifactId;
		public final String version;
		public final long size;
		public final String ext;

		public Artifact(String groupId, String artifactId, String version,
				long size, String ext) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
			this.size = size;
			this.ext = ext;
		}

		public String getPath() {
			return getPath(ext);
		}

		public String getPath(String ext) {
			return groupId.replace('.', '/') + "/" + artifactId + "/" + version
					+ "/" + artifactId + "-" + version + "." + ext;
		}

		public Artifact max(Artifact other) {
			String[] ls = this.version.split("\\.");
			String[] rs = other.version.split("\\.");

			for (int i = 0; i < Math.min(ls.length, rs.length); i++) {
				int res = compare(ls, rs, i);
				if (res > 0) {
					return this;
				} else if (res < 0) {
					return other;
				}
			}

			return ls.length > rs.length ? this : other;
		}

		private static int compare(String[] ls, String[] rs, int i) {
			try {
				return Integer.parseInt(ls[i]) - Integer.parseInt(rs[i]);
			} catch (NumberFormatException e) {
				return ls[i].compareTo(rs[i]);
			}
		}

	}

	private MavenIndex() {
	}

	@SuppressWarnings("deprecation")
	public static MavenIndex build(IndexSearcher searcher) throws Exception {
		MavenIndex index = new MavenIndex();

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
			String allGroupsList = doc.get("allGroupsList");
			String rootGroups = doc.get("rootGroups");
			String rootGroupsList = doc.get("rootGroupsList");
			String descriptor = doc.get("DESCRIPTOR");
			String idxinfo = doc.get("IDXINFO");
			String one = doc.get("1");
			String m = doc.get("m");
			String u = doc.get("u");
			String i = doc.get("i");
			String del = doc.get("del");

			assert (allGroups == null) == (allGroupsList == null) : "Invalid all groups doc: "
					+ docText;

			assert (rootGroups == null) == (rootGroupsList == null) : "Invalid root groups doc: "
					+ docText;

			assert (descriptor == null) == (idxinfo == null) : "Invalid description/idxinfo doc: "
					+ docText;

			assert ((allGroups == null) && (rootGroups == null) && (descriptor == null)) == (m != null) : "null m: "
					+ docText;

			assert (u == null) || (m != null) : "u and m: " + docText;
			assert (i == null) || (m != null) : "i and m: " + docText;
			assert (u != null) == (i != null) : "u and i: " + docText;

			assert (del == null) || (m != null) : "del and m: " + docText;
			assert (one == null) || (m != null) : "one and m: " + docText;

			if (allGroups != null) {
				assert allGroups.equals("allGroups");
			}

			if (rootGroups != null) {
				assert rootGroups.equals("rootGroups");
				assert index.rootGroupsList == null : "rootGroupsList already set";

				index.rootGroupsList = Arrays.asList(rootGroupsList
						.split("\\|"));
			}

			if (descriptor != null) {
				assert descriptor.equals("NexusIndex");
			}

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

			if (i != null) {
				assert u != null : "u and i just in case";

				String[] us = u.split("\\|");
				String[] is = i.split("\\|");

				assert us.length == 4 || us.length == 5 : "Invalid value for u field";

				String groupId = us[0];
				String artifactId = us[1];
				String version = us[2];
				String kind = us[3];
				assert us.length != 4 || "NA".equals(kind) : "Expected NA";

				assert is.length == 7 : "Invalid i: " + docText;

				String dateText = is[1];
				String ext = is[6];

				index.extSet.add(ext);

				assert dateText.matches("[0-9]{13}") : "Invalid i subfield 1: "
						+ docText;

				Date date = new Date(Long.parseLong(dateText));

				if (imindate == null) {
					imindate = date;
					imaxdate = date;
				} else {
					imindate = date.compareTo(imindate) < 0 ? date : imindate;
					imaxdate = date.compareTo(imaxdate) > 0 ? date : imaxdate;
				}

				String sizeText = is[2];
				assert sizeText.matches("-?[0-9]+") : "Invalid i.size: "
						+ docText;
				long size = Long.parseLong(sizeText);

				assert !is[0].equals("null")
						|| (size == -1 && is[6].equals("pom")) : "size/no jar and null: "
						+ docText;

				assert is[3].matches("[0-9]") : "Invalid i subfield 3: "
						+ docText;
				assert is[4].matches("[0-9]") : "Invalid i subfield 4: "
						+ docText;
				assert is[5].matches("[0-9]") : "Invalid i subfield 5: "
						+ docText;

				// assert (us.length == 4) == size > 0 :
				// "size for kind jar/pom: "
				// + docText;xxxxx

				index.totalSize += size;

				if (us.length == 4
						&& Arrays.asList("jar", "ejb", "war", "ear").contains(
								ext)) {
					String key = groupId + ":" + artifactId;

					Artifact a = new Artifact(groupId, artifactId, version,
							size, ext);
					if (index.map.containsKey(key)) {
						Artifact b = index.map.get(key);
						index.lastVersionJarsSize -= b.size;

						a = b.max(a);
					}

					index.lastVersionJarsSize += a.size;
					index.map.put(key, a);
				}
			}

			//
			// assert one != null: "null: " +docText;
			// if (one != null) {
			// text.matches("[0-9a-f]{40}");
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

		index.uniqueArtifactsCount = index.map.size();
		index.mmindate = mmindate;
		index.mmaxdate = mmaxdate;
		index.imindate = imindate;
		index.imaxdate = imaxdate;

		return index;
	}

	public static void print(IndexSearcher searcher, String term,
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
}
