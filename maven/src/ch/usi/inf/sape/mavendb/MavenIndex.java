package ch.usi.inf.sape.mavendb;

import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MavenIndex implements Iterable<MavenArtifact> {

	public long maxDoc;
	public List<String> rootGroupsList;
	public long totalSize = 0;
	public long lastVersionJarsSize = 0;
	public long uniqueArtifactsCount = 0;
	public Date mmindate = null;
	public Date mmaxdate = null;
	public Date imindate = null;
	public Date imaxdate = null;
	public final HashMap<String, MavenArtifact> map = new HashMap<String, MavenArtifact>();
	public final Set<String> fieldSet = new HashSet<String>();
	public final Set<String> extSet = new HashSet<String>();

	MavenIndex() {
	}

	public void print(PrintStream out) {
		out.println("groupId, artifactId, version, size, ext");
		for (MavenArtifact a : this) {
			out.format("%s, %s, %s, %s, %s\n", a.groupId, a.artifactId,
					a.version, a.size, a.ext);
		}
	}

	public MavenArtifact get(String id) {
		return map.get(id);
	}

	@Override
	public Iterator<MavenArtifact> iterator() {
		return map.values().iterator();
	}
}
