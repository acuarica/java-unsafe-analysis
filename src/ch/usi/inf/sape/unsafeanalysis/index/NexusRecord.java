package ch.usi.inf.sape.unsafeanalysis.index;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NexusRecord {

	private Map<String, String> values = new HashMap<String, String>();

	public String get(String key) {
		return values.get(key);
	}

	@Override
	public String toString() {
		String res = "";
		for (Entry<String, String> entry : values.entrySet()) {
			res += entry.getKey() + "=" + entry.getValue() + " ";
		}

		return res;
	}

	void put(String key, String value) {
		values.put(key, value);
	}
}
