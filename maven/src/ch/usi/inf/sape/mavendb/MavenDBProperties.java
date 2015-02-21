package ch.usi.inf.sape.mavendb;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MavenDBProperties {

	private final Properties prop = new Properties();

	private static MavenDBProperties instance;

	private MavenDBProperties() throws IOException {
		final String propFileName = "mavendb.properties";
		final ClassLoader cl = MavenDBProperties.class.getClassLoader();
		final InputStream inputStream = cl.getResourceAsStream(propFileName);

		prop.load(inputStream);
	}

	public static MavenDBProperties get() throws IOException {
		if (instance == null) {
			instance = new MavenDBProperties();
		}

		return instance;
	}

	public String indexPath() {
		return getProperty("indexpath");
	}

	public String repoPath() {
		return getProperty("repopath");
	}

	public String artifactsPath() {
		return getProperty("artifactspath");
	}

	public int downloaderRetries() {
		return Integer.parseInt(getProperty("downloader.retries"));
	}

	public int downloaderNumberOfThreads() {
		return Integer.parseInt(getProperty("downloader.numberofthreads"));
	}

	public String[] downloaderMirrorList() {
		List<String> list = new ArrayList<String>();
		for (String propName : prop.stringPropertyNames()) {
			if (propName.startsWith("mavendb.downloader.mirrors.")) {
				String mirrorUrl = prop.getProperty(propName);
				list.add(mirrorUrl);
			}
		}

		return list.toArray(new String[list.size()]);
	}

	private String getProperty(String propName) {
		return prop.getProperty("mavendb." + propName);
	}
}
