package eu.scape_project.sanselan_wrapper;

import java.awt.image.BufferedImage;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

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

public class SanselanCli {
	public static void main(String[] args) {
		try {
			Options options = new Options();
			options.addOption("i", "input-file", true, "input file");
			options.addOption("o", "output-file", true, "output file");

			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("i") && cmd.hasOption("o")) {
				BufferedImage inImage = SanselanWrapper.load(
						cmd.getOptionValue("i"), null);
				SanselanWrapper.convert(inImage, cmd.getOptionValue("o"), null);
			} else {
				System.out
						.println("usage: (-i|--input-file=) INPUT_FILE (-o|--output-file=) OUTPUT_FILE");
				System.exit(1);
			}
		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		}
		System.exit(0);
	}
}
