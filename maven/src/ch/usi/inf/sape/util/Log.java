package ch.usi.inf.sape.util;

import java.io.PrintStream;

public class Log {

	public PrintStream out;

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
