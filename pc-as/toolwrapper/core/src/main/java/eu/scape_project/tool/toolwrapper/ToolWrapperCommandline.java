package eu.scape_project.tool.toolwrapper;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.tuple.ImmutablePair;

import eu.scape_project.tool.data.Tool;
import eu.scape_project.tool.data.utils.Utils;

/*
 ################################################################################
 #                  Copyright 2012 The SCAPE Project Consortium
 #
 #   This software is copyrighted by the SCAPE Project Consortium. 
 #   The SCAPE project is co-funded by the European Union under
 #   FP7 ICT-2009.4.1 (Grant Agreement number 270137).
 #
 #   Licensed under the Apache License, Version 2.0 (the "License");
 #   you may not use this file except in compliance with the License.
 #   You may obtain a copy of the License at
 #
 #                   http://www.apache.org/licenses/LICENSE-2.0              
 #
 #   Unless required by applicable law or agreed to in writing, software
 #   distributed under the License is distributed on an "AS IS" BASIS,
 #   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   
 #   See the License for the specific language governing permissions and
 #   limitations under the License.
 ################################################################################
 */

public abstract class ToolWrapperCommandline {
	private static final String SCAPE_COPYRIGHT_STATEMENT = "\nThis software is copyrighted by the SCAPE Project Consortium.\nThe SCAPE project is co-funded by the European Union under\nFP7 ICT-2009.4.1 (Grant Agreement number 270137).";
	private Options options;

	public ToolWrapperCommandline() {
		options = new Options();
		options.addOption("t", "toolspec", true, "toolspec file location");
		options.addOption("o", "outDir", true,
				"directory where to put the generated artifacts");
		options.addOption("e", "email", true,
				"maintainer e-mail for Debian package generation");
		options.addOption("d", "debian", false,
				"generate Debian package for each artifact");
		options.addOption("sh", "script", true,
				"location of the script referred in the toolspec"
						+ " that invokes the tool (to simplify the invocation)");
	}

	/** Method used to print command-line syntax (usage) */
	public void printUsage(boolean exitProgram, int exitCode) {
		HelpFormatter helpFormatter = new HelpFormatter();
		PrintWriter systemErrPrintWriter = new PrintWriter(System.err, true);
		helpFormatter.printHelp(systemErrPrintWriter,
				HelpFormatter.DEFAULT_WIDTH, "\"" + getClass().getSimpleName()
						+ ".jar\"", null, options,
				HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD,
				SCAPE_COPYRIGHT_STATEMENT, true);
		if (exitProgram) {
			System.exit(exitCode);
		}
	}

	/**
	 * Method that parses the program arguments and return an object that
	 * represents that same arguments ({@link CommandLine})
	 */
	private CommandLine parseArguments(String[] args) {
		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (org.apache.commons.cli.ParseException e) {
			System.err.println(e);
		}
		return commandLine;
	}

	public ImmutablePair<CommandLine, Tool> processToolWrapperGenerationRequest(
			String[] args) {
		ImmutablePair<CommandLine, Tool> pair = null;
		Tool tool = null;
		CommandLine cmd = parseArguments(args);
		if (cmd != null && cmd.hasOption("t") && cmd.hasOption("o")) {
			if (!(cmd.hasOption("d") && !cmd.hasOption("e"))) {
				tool = Utils.createTool(cmd.getOptionValue("t"));
				if (tool != null) {
					pair = ImmutablePair.of(cmd, tool);
				}
			}
		}
		return pair;
	}
}
