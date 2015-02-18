package ch.usi.inf.sape.unsafe.maven;

import java.io.PrintStream;

public class Log {

	private PrintStream out;

	public Log(PrintStream out) {
		this.out = out;
	}

	public void log(String format, Object... args) {
		out.format("[ " + format + " ]\n", args);
	}

	public void lognl(String format, Object... args) {
		out.format(format, args);
	}

}
