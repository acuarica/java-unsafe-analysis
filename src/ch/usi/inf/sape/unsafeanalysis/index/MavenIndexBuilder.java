package ch.usi.inf.sape.unsafeanalysis.index;

import java.util.Arrays;
import java.util.Date;

public class MavenIndexBuilder {

	@SuppressWarnings("deprecation")
	public static MavenIndex build(NexusIndexParser nip) throws Exception {
		MavenIndex index = new MavenIndex();

		Date mmindate = null;
		Date mmaxdate = null;
		Date imindate = null;
		Date imaxdate = null;

		int docIndex = 0;
		for (NexusRecord doc : nip) {
			docIndex++;

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
			String n = doc.get("n");
			String d = doc.get("d");

			assert (allGroups == null) == (allGroupsList == null) : "Invalid all groups doc: "
					+ doc;

			assert (rootGroups == null) == (rootGroupsList == null) : "Invalid root groups doc: "
					+ doc;

			assert (descriptor == null) == (idxinfo == null) : "Invalid description/idxinfo doc: "
					+ doc;

			assert ((allGroups == null) && (rootGroups == null) && (descriptor == null)) == (m != null) : "null m: "
					+ doc;

			assert (u == null) || (m != null) : "u and m: " + doc;
			assert (i == null) || (m != null) : "i and m: " + doc;
			assert (u != null) == (i != null) : "u and i: " + doc;

			assert (del == null) || (m != null) : "del and m: " + doc;
			assert (one == null) || (m != null) : "one and m: " + doc;

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
				assert m.matches("[0-9]{13}") : "Invalid m: " + doc;
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

				assert is.length == 7 : "Invalid i: " + doc;

				String dateText = is[1];
				String ext = is[6];

				index.extSet.add(ext);

				assert dateText.matches("[0-9]{13}") : "Invalid i subfield 1: "
						+ doc;

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
						+ doc;
				long size = Long.parseLong(sizeText);

				assert size >= -1 : "Size more negative: " + doc;

				assert !is[0].equals("null")
						|| (size == -1 && is[6].equals("pom")) : "size/no jar and null: "
						+ doc;

				assert is[3].matches("[0-9]") : "Invalid i subfield 3: "
						+ doc;
				assert is[4].matches("[0-9]") : "Invalid i subfield 4: "
						+ doc;
				assert is[5].matches("[0-9]") : "Invalid i subfield 5: "
						+ doc;

				// assert (us.length == 4) == size > 0 :
				// "size for kind jar/pom: "
				// + docText;xxxxx

				index.totalSize += size;

				MavenArtifact a = new MavenArtifact(groupId, artifactId,
						version, size, ext, n, d);
				String id = a.getId();

				if (us.length == 4
						&& Arrays.asList("jar", "ejb", "war", "ear").contains(
								ext)) {
					// assert size > 0 : "size zero jar" + docText;
					// assert one != null && one.matches("[0-9a-f]{40}") :
					// "sha1: "+ docText;

					if (index.map.containsKey(id)) {
						MavenArtifact b = index.map.get(id);
						index.lastVersionJarsSize -= b.size;

						a = b.max(a);
					}

					index.lastVersionJarsSize += a.size;
					index.map.put(id, a);
				}

				if (us.length == 5
						&& kind.equals("sources")
						&& Arrays.asList("jar", "ejb", "war", "ear").contains(
								ext)) {
					if (index.map.containsKey(id)) {
						index.get(id).sources = true;
					}
				}
			}

			// if (one != null && !one.toLowerCase().matches("[0-9a-f]{40}")) {
			// System.out.println(docText);
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
}
