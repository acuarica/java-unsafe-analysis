package ch.usi.inf.sape.boaclient;

import ch.usi.inf.sape.argparser.Arg;

public class MainArgs {

	@Arg(shortkey = "u", longkey = "user")
	public String username;

	@Arg(shortkey = "p", longkey = "pass")
	public String password;

	@Arg(shortkey = "f", longkey = "file")
	public String filename;

	@Arg(shortkey = "d", longkey = "dataset")
	public String dataset;

}
