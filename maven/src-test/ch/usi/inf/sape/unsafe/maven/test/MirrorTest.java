package ch.usi.inf.sape.unsafe.maven.test;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.usi.inf.sape.util.Mirror;

public class MirrorTest {

	@Test
	public void idTest() throws Exception {

		System.out.format("%2d%%\n", 20 * 100 / 1000);
		System.out.format("%2d%%\n", 55 * 100 / 1000);
		System.out.format("%2d%%\n", 99 * 100 / 1000);
		System.out.format("%2d%%\n", 999 * 100 / 1000);
		System.out.format("%2d%%\n", 1000 * 100 / 1000);

		Mirror m = new Mirror("http://mirrors.ibiblio.org/maven2/", 10);

		assertEquals("ibiblio", m.id);
	}

}
