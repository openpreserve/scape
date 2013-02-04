/**
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
package eu.scape_project.tool.core.configuration;

public final class Constants {
	private Constants() {

	}

	public static final String SCAPE_COPYRIGHT_STATEMENT = "\nThis software is copyrighted by the SCAPE Project Consortium.\nThe SCAPE project is co-funded by the European Union under\nFP7 ICT-2009.4.1 (Grant Agreement number 270137).";

	public static final String BASHGENERATOR_WRAPPER_OUTDIRNAME = "bash";
	public static final String BASHGENERATOR_WORKFLOW_OUTDIRNAME = "workflow";
	public static final String BASHGENERATOR_WORKFLOW_EXTENSION = ".t2flow";
	public static final String BASHGENERATOR_ARRAY_FINAL_STR = "[@]}";

	public static final String DEBIANBASHGENERATOR_DEBS_OUTDIRNAME = "debian";
	public static final String BASHGENERATOR_INSTALL_OUTDIRNAME = "install";
}
