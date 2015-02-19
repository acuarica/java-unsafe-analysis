package ch.usi.inf.sape.unsafe.maven.test;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.usi.inf.sape.unsafe.maven.Mirror;

public class MirrorTest {

	@Test
	public void idTest() throws Exception {
		Mirror m = new Mirror("http://mirrors.ibiblio.org/maven2/", 10);

		assertEquals("ibiblio", m.id);
	}
}
