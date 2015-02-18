package ch.usi.inf.sape.unsafe.maven;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Mirror {

	private String rootUrl;

	public Mirror(String rootUrl) {
		assert rootUrl.startsWith("http://") : "rootUrl does not start with 'http://': "
				+ rootUrl;
		assert rootUrl.endsWith("/") : "rootUrl does not end with '/': "
				+ rootUrl;

		this.rootUrl = rootUrl;
	}

	public byte[] download(String path) throws IOException {
		assert !path.endsWith("/") : "path ends with '/', only files can be downloaded: "
				+ path;

		URL url = new URL(rootUrl + path);

		InputStream in = new BufferedInputStream(url.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		while (-1 != (n = in.read(buf))) {
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();

		return response;
	}
}
