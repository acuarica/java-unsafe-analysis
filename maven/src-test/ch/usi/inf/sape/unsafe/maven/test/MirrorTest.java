package ch.usi.inf.sape.unsafe.maven.test;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.usi.inf.sape.util.Mirror;

public class MirrorTest {

	@Test
	public void idTest() throws Exception {

		System.out.format("%6.2f %%\n", 3.4567);
		System.out.format("%6.2f %%\n", 123.4567);
		System.out.format("%6.2f %%\n", 1234.4567);

		Mirror m = new Mirror("http://mirrors.ibiblio.org/maven2/", 10);

		assertEquals("ibiblio", m.id);
	}

}
