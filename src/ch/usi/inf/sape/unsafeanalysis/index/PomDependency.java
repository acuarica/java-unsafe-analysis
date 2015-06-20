package ch.usi.inf.sape.unsafeanalysis.index;

public class PomDependency {
	public String groupId;
	public String artifactId;
	public String version;
	public String scope;

	@Override
	public String toString() {
		return String.format("g: %s, a: %s v: %s s: %s", groupId, artifactId,
				version, scope);
	}
}
