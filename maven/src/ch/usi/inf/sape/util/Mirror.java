package ch.usi.inf.sape.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mirror {

	private final String rootUrl;
	private final int noRetries;
	public final String id;

	public Mirror(String rootUrl, int noRetries) throws Exception {
		assert rootUrl.startsWith("http://") : "rootUrl does not start with 'http://': "
				+ rootUrl;
		assert rootUrl.endsWith("/") : "rootUrl does not end with '/': "
				+ rootUrl;

		this.rootUrl = rootUrl;

		Matcher m = Pattern.compile("http://.+\\.(.+)\\.(.+)/(.+)").matcher(
				rootUrl);
		if (m.matches()) {
			this.id = m.group(1);
		} else {
			throw new Exception("Invalid mirror url: " + rootUrl);
		}

		this.noRetries = noRetries;
	}

	public byte[] download(String path, Log log) throws IOException {
		assert !path.endsWith("/") : "path ends with '/', only files can be downloaded: "
				+ path;

		IOException ex = null;
		for (int r : new Retry(noRetries)) {
			URL url = new URL(rootUrl + path);
			URLConnection conn = url.openConnection();

			try {

				InputStream in = new BufferedInputStream(conn.getInputStream());
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
			} catch (IOException e) {
				int rc = ((HttpURLConnection) conn).getResponseCode();
				if (rc == 403) {
					log.log("Retry #%d for %s on response code %s in mirror %s",
							r, path, rc, id);
					ex = e;
				} else {
					throw e;
				}
			}
		}

		throw ex;
	}

	private static class Retry implements Iterable<Integer> {

		private int times;

		public Retry(int times) {
			this.times = times;
		}

		@Override
		public Iterator<Integer> iterator() {
			return new RetryIterator(times);
		}
	}

	private static class RetryIterator implements Iterator<Integer> {

		private int times;
		private int retry;

		public RetryIterator(int times) {
			this.times = times;
		}

		@Override
		public boolean hasNext() {

			return retry < times;
		}

		@Override
		public Integer next() {
			retry++;

			try {
				Thread.sleep(retry * 4 * 1000);
			} catch (InterruptedException e) {
			}

			return retry;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"remove on retry not supported");
		}
	}
}
