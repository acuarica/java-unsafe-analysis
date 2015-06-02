package ch.usi.inf.sape.unsafeanalysis.index;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a Nexus Record within a Nexus Index. A record is a map from keys
 * (String) to a value (also String).
 * 
 * @author Luis Mastrangelo
 *
 */
public class NexusRecord {

	/**
	 * Map to hold the key/value pairs.
	 */
	private final Map<String, String> values = new HashMap<String, String>();

	/**
	 * Given a key, retrieves the associated value in the record.
	 * 
	 * @param key
	 *            The key to look for
	 * @return The associated value to the given key.
	 */
	public String get(String key) {
		return values.get(key);
	}

	/**
	 * Puts a new pair inside the record.
	 * 
	 * @param key
	 *            The key of the new record.
	 * @param value
	 *            The value associated to the given key.
	 */
	public void put(String key, String value) {
		values.put(key, value);
	}

	@Override
	public String toString() {
		String res = "";
		for (Entry<String, String> entry : values.entrySet()) {
			res += entry.getKey() + "=" + entry.getValue() + " ";
		}

		return res;
	}
}
