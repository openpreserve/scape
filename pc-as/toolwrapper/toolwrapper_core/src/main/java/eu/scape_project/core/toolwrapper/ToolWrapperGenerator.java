package eu.scape_project.core.toolwrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import eu.scape_project.core.toolspec_objects.from_schema.Operation;
import eu.scape_project.core.toolspec_objects.from_schema.Tool;
import eu.scape_project.core.toolspec_objects.utils.Utils;
import eu.scape_project.core.toolwrapper.bash.BashWrapperGenerator;

public class ToolWrapperGenerator {

	private ToolWrapperGenerator() {

	}

	/** Method used to print command-line syntax (usage) */
	private static void printUsage() {
		System.out
				.println("usage: (-t|--toolspec=) TOOL_SPEC_FILE (-g|--generate=) ARTIFACTS_TO_GENERATE");
		System.out
				.println("\t where ARTIFACTS_TO_GENERATE is a comma-separated list with zero or more values: soap|rest|bash");
	}

	/**
	 * Method that parses the program arguments and return an object that
	 * represents that same arguments ({@link CommandLine})
	 */
	private static CommandLine parseArguments(String[] args) {
		Options options = new Options();
		options.addOption("t", "toolspec", true, "toolspec file");
		options.addOption("g", "generate", true, "artifacts to generate");

		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e);
		}
		return commandLine;
	}

	/**
	 * Method that parses the artifacts list to generate (if any) and returns
	 * its values
	 */
	private static List<String> parseArtifactsToGenerate(CommandLine cmd) {
		List<String> res = new ArrayList<String>();
		if (cmd.hasOption("g")) {
			res = Arrays.asList(cmd.getOptionValue("g").split(","));
		}
		if (res.size() == 0) {
			res = Arrays.asList("soap", "rest", "bash");
		}
		return res;
	}

	/** Method that invokes the methods to created the necessary artifacts */
	public static void generateWrappers(Tool tool,
			List<String> artifactsToGenerate) {
		for (Operation operation : tool.getOperations().getOperation()) {
			System.out.println("operation=" + operation.getName());
			for (String artifact : artifactsToGenerate) {
				if (artifact.equals("soap")
						&& operation.getInputs().getStdin() == null) {
					System.out.println("\tgoing to generate soap web service");
				} else if (artifact.equals("rest")
						&& operation.getInputs().getStdin() == null) {
					System.out.println("\tgoing to generate rest web service");
				} else if (artifact.equals("bash")) {
					System.out.println("\tgoing to generate bash script");
					new BashWrapperGenerator(tool, operation).generateWrapper();
				}
			}
		}
	}

	public static void main(String[] args) {
		CommandLine cmd = parseArguments(args);
		if (cmd != null && cmd.hasOption("t")) {
			Tool tool = Utils.createTool(cmd.getOptionValue("t"));
			List<String> artifactsToGenerate = parseArtifactsToGenerate(cmd);
			if (tool != null) {
				generateWrappers(tool, artifactsToGenerate);
				System.exit(0);
			} else {
				System.exit(1);
			}
		} else {
			printUsage();
			System.exit(1);
		}
	}
}
