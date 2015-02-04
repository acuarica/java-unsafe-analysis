package ch.usi.inf.sape.boaclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import ch.usi.inf.sape.argparser.ArgParser;
import edu.iastate.cs.boa.BoaClient;
import edu.iastate.cs.boa.BoaException;
import edu.iastate.cs.boa.CompileStatus;
import edu.iastate.cs.boa.ExecutionStatus;
import edu.iastate.cs.boa.InputHandle;
import edu.iastate.cs.boa.JobHandle;
import edu.iastate.cs.boa.LoginException;
import edu.iastate.cs.boa.NotLoggedInException;

public class Main {

	private static String readFile(String filename) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String text = null;
			String result = "";
			while ((text = br.readLine()) != null) {
				result += text + "\n";
			}

			return result;
		}
	}

	private static void writeFile(String filename, String text)
			throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
			bw.write(text);
		}
	}

	private static String run(BoaClient client, String query, PrintStream out,
			InputHandle ds) throws NotLoggedInException, BoaException,
			InterruptedException {

		JobHandle j = client.query(query, ds);
		out.println("Job id: " + j.getId() + " with dataset \""
				+ j.getDataset() + "\"");

		out.print("Compiling ");
		while (j.getCompilerStatus() == CompileStatus.RUNNING
				|| j.getCompilerStatus() == CompileStatus.WAITING) {
			out.print(".");
			Thread.sleep(2000);
			j.refresh();
		}

		if (j.getCompilerStatus() == CompileStatus.ERROR) {
			System.out.println(j.getCompilerErrors());
			throw new IllegalArgumentException("Invalid query");
		}

		out.println();

		out.print("Executing ");
		while (j.getExecutionStatus() == ExecutionStatus.RUNNING
				|| j.getExecutionStatus() == ExecutionStatus.WAITING) {
			out.print(".");
			Thread.sleep(2000);
			j.refresh();
		}

		if (j.getExecutionStatus() == ExecutionStatus.ERROR) {
			throw new IllegalArgumentException("runtime error");
		}

		out.println();

		return j.getOutput();
	}

	public static void main(String[] args) throws BoaException, IOException,
			InterruptedException, InstantiationException,
			IllegalAccessException {

		PrintStream out = System.err;

		MainArgs as = ArgParser.parseArgs(args, MainArgs.class);

		String query = readFile(as.input);

		String username = System.console().readLine("Boa user name: ");
		String password = new String(System.console().readPassword(
				"Boa password: "));

		try (final BoaClient client = new BoaClient()) {
			try {
				client.login(username, password);
			} catch (LoginException e) {
				out.println(e.getMessage());
				System.exit(1);
			}

			InputHandle ds = client.getDataset(as.dataset);

			out.println("Sending file: \"" + as.input + "\" as user "
					+ username + "");

			String result = run(client, query, out, ds);

			out.println(result);

			writeFile(as.output, result);
		}
	}
}
