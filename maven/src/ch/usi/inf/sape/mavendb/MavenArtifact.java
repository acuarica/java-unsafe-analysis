package ch.usi.inf.sape.mavendb;

import java.util.ArrayList;
import java.util.List;

public class MavenArtifact {

	public final String groupId;
	public final String artifactId;
	public final String version;
	public final long size;
	public final String ext;
	public final String groupDesc;
	public final String artifactDesc;
	public final List<Dependency> dependencies = new ArrayList<Dependency>();
	public boolean sources = false;

	public static class Dependency {
		public String groupId;
		public String artifactId;
		public String version;
		public String scope;

		public Dependency() {
		}
	}

	public MavenArtifact(String groupId, String artifactId, String version,
			long size, String ext, String groupDesc, String artifactDesc) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.size = size;
		this.ext = ext;
		this.groupDesc = groupDesc;
		this.artifactDesc = artifactDesc;
	}

	public String getPath() {
		return getPath("." + ext);
	}

	public String getPomPath() {
		return getPath(".pom");
	}

	public String getSourcesPath() {
		return getPath("-sources.jar");
	}

	public String getId() {
		return groupId + ":" + artifactId;
	}

	private String getPath(String ext) {
		return groupId.replace('.', '/') + "/" + artifactId + "/" + version
				+ "/" + artifactId + "-" + version + ext;
	}

	public MavenArtifact max(MavenArtifact other) {
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
