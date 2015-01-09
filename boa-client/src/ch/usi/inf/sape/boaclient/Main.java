package ch.usi.inf.sape.boaclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import edu.iastate.cs.boa.BoaClient;
import edu.iastate.cs.boa.BoaException;
import edu.iastate.cs.boa.CompileStatus;
import edu.iastate.cs.boa.ExecutionStatus;
import edu.iastate.cs.boa.JobHandle;
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

	private static JobHandle run(BoaClient client, String query, PrintStream out)
			throws NotLoggedInException, BoaException, InterruptedException {

		JobHandle j = client.query(query);
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

		return j;
	}

	public static void main(String[] args) throws BoaException, IOException,
			InterruptedException {

		PrintStream out = System.out;

		if (args.length == 0) {
			throw new IllegalArgumentException("Missing query file");
		}

		String file = args[0];
		String user = "luismastrangelo";
		String pass = "elotrodia";

		String query = readFile(file);

		try (final BoaClient client = new BoaClient()) {
			client.login(user, pass);

			out.println("Sending file: \"" + file + "\" as user " + user + "");

			JobHandle j = run(client, query, out);

			out.println(j.getOutput());

			writeFile(file + "-" + j.getId() + ".out", j.getOutput());
		}
	}
}
