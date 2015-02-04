package ch.usi.inf.sape.boaclient;

import ch.usi.inf.sape.argparser.Arg;

public class MainArgs {

	@Arg(shortkey = "d", longkey = "dataset")
	public String dataset;

	@Arg(shortkey = "i", longkey = "input")
	public String input;

	@Arg(shortkey = "o", longkey = "output")
	public String output;
}
