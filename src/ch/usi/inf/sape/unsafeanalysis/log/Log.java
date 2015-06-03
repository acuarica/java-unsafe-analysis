package ch.usi.inf.sape.unsafeanalysis.log;

import java.io.PrintStream;

/**
 * Wrapper around a PrintStream to serve as a log. 
 * @author Luis Mastrangelo
 *
 */
public class Log {

	private final PrintStream out;

	public Log(PrintStream out) {
		this.out = out;
	}

	public void info(String message, Object... args) {
		out.format(message + "\n", args);
	}
}
