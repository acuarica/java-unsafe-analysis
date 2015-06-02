package ch.usi.inf.sape.unsafeanalysis.index;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

/**
 * Iterator interface to parse a Nexus Index.
 * 
 * @author Luis Mastragelo
 *
 */
public class NexusIndexParser implements Iterable<NexusRecord>, Closeable {

	private RandomAccessFile raf;
	private FileChannel fc;
	private MappedByteBuffer mbb;

	/**
	 * Creates a new parser with the specified path. The indexPath must be a
	 * valid Nexus Index.
	 * 
	 * @param indexPath
	 *            Path to the Nexus Index to parse.
	 * @throws IOException
	 */
	public NexusIndexParser(String indexPath) throws IOException {
		raf = new RandomAccessFile(indexPath, "r");
		fc = raf.getChannel();
		mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

		mbb.get();
		mbb.getLong();
	}

	@Override
	public Iterator<NexusRecord> iterator() {
		return new Iterator<NexusRecord>() {

			@Override
			public boolean hasNext() {
				return mbb.hasRemaining();
			}

			@Override
			public NexusRecord next() {
				NexusRecord nr = new NexusRecord();
				int fieldCount = mbb.getInt();

				for (int i = 0; i < fieldCount; i++) {
					mbb.get();

					int keyLen = mbb.getShort();
					String key = readString(keyLen);

					int valueLen = mbb.getInt();
					String value = readString(valueLen);

					nr.put(key, value);
				}

				return nr;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Remove not supported");
			}

			private String readString(int len) {
				byte[] buffer = new byte[len];
				mbb.get(buffer);
				return new String(buffer);
			}
		};
	}

	@Override
	public void close() throws IOException {
		fc.close();
		raf.close();
	}
}
