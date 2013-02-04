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
package eu.scape_project.tool.bash_generator;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import eu.scape_project.tool.core.ToolWrapperCommandline;
import eu.scape_project.tool.core.ToolWrapperGenerator;
import eu.scape_project.tool.core.configuration.Constants;
import eu.scape_project.tool.core.utils.Utils;
import eu.scape_project.tool.data.Input;
import eu.scape_project.tool.data.Operation;
import eu.scape_project.tool.data.Output;
import eu.scape_project.tool.data.Parameter;
import eu.scape_project.tool.data.Tool;

public class BashWrapperGenerator extends ToolWrapperCommandline implements
		ToolWrapperGenerator {
	private static Logger log = Logger.getLogger(BashWrapperGenerator.class);
	private Tool tool;
	private Operation operation;
	private String wrapperName;
	private Template bashWrapperTemplate;

	public BashWrapperGenerator() {
		super();
		tool = null;
		operation = null;
		wrapperName = null;
		bashWrapperTemplate = null;
	}

	/* ****************************************************
	 * getters and setters ********************************
	 * ****************************************************
	 */
	public BashWrapperGenerator setTool(Tool tool) {
		this.tool = tool;
		return this;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public void setWrapperName(String wrapperName) {
		this.wrapperName = wrapperName;
	}

	public String getWrapperName() {
		return wrapperName;
	}

	/* ****************************************************
	 * create bash wrapper and Taverna workflow logic *****
	 * ****************************************************
	 */
	@Override
	public boolean generateWrapper(Tool tool, Operation operation,
			String outputDirectory) {
		this.tool = tool;
		this.operation = operation;
		boolean res = true;

		initVelocity();

		// verify if the needed directories exist or can be created. then
		// load the bash wrapper template
		if (verifyNeededDirectories(outputDirectory)
				&& loadBashWrapperTemplate()) {
			VelocityContext wrapperContext = new VelocityContext();

			// add bash related information to the template context
			addGeneralInformationToContext(wrapperContext);
			addUsageInformationToContext(wrapperContext);
			addCommandInformationToContext(wrapperContext);

			// generate the bash wrapper
			res = res && generateBashWrapper(wrapperContext, outputDirectory);

			// generate the Taverna workflow
			res = res && generateWorkflow(wrapperContext, outputDirectory);
		} else {
			res = false;
		}
		return res;
	}

	public boolean verifyNeededDirectories(String outputDirectory) {
		boolean res = true;
		File directory = new File(outputDirectory);
		if (!directory.exists() && !directory.mkdir()) {
			log.error("The directory \"" + directory
					+ "\" cannot be created...");
			res = false;
		} else {
			res = res
					&& verifyAndCreateNeededDirectory(new File(directory,
							Constants.BASHGENERATOR_WRAPPER_OUTDIRNAME));
			res = res
					&& verifyAndCreateNeededDirectory(new File(directory,
							Constants.BASHGENERATOR_WORKFLOW_OUTDIRNAME));
			res = res
					&& verifyAndCreateNeededDirectory(new File(directory,
							Constants.BASHGENERATOR_INSTALL_OUTDIRNAME));
		}
		return res;
	}

	private boolean verifyAndCreateNeededDirectory(File directory) {
		boolean res = true;
		boolean operationResult = (directory.exists() || directory.mkdir());
		res = res && operationResult;
		if (!operationResult) {
			log.error("The directory \"" + directory
					+ "\" either not exists or cannot be created...");
		}
		return res;
	}

	private boolean generateBashWrapper(VelocityContext context,
			String outputDirectory) {
		// merge the template context and the template itself
		StringWriter bashWrapperSW = new StringWriter();
		bashWrapperTemplate.merge(context, bashWrapperSW);
		return Utils.writeTemplateContent(outputDirectory,
				Constants.BASHGENERATOR_WRAPPER_OUTDIRNAME,
				operation.getName(), bashWrapperSW, true);
	}

	private boolean generateWorkflow(VelocityContext context,
			String outputDirectory) {
		boolean success = true;
		Template workflowTemplate = loadVelocityTemplateFromResources("workflow_template.vm");
		UUID randomUUID = UUID.randomUUID();
		context.put("uniqID", randomUUID);
		StringWriter sw = new StringWriter();
		workflowTemplate.merge(context, sw);
		Utils.writeTemplateContent(outputDirectory,
				Constants.BASHGENERATOR_WORKFLOW_OUTDIRNAME,
				operation.getName()
						+ Constants.BASHGENERATOR_WORKFLOW_EXTENSION, sw, false);
		return success;
	}

	private void addCommandInformationToContext(VelocityContext wrapperContext) {
		String command = operation.getCommand();
		VelocityContext contextForCommand = new VelocityContext();
		StringBuilder wrapperParameters = new StringBuilder();
		wrapperContext.put("listOfInputs", operation.getInputs().getInput());
		int i = 1;
		List<String> verifyRequiredArguments = new ArrayList<String>();
		for (Input input : operation.getInputs().getInput()) {
			if (contextForCommand.containsKey(input.getName())) {
				log.error("Operation \"" + operation.getName()
						+ "\" already contains an input called \""
						+ input.getName() + "\"...");
			}
			if (input.isRequired()) {
				verifyRequiredArguments.add("${input_files" + i
						+ Constants.BASHGENERATOR_ARRAY_FINAL_STR);
			}
			contextForCommand.put(input.getName(),
					wrapWithDoubleQuotes("${input_files" + i
							+ Constants.BASHGENERATOR_ARRAY_FINAL_STR));
			wrapperParameters.append("-i %%" + input.getName() + "_inner%% ");
			i++;
		}
		i = 1;
		wrapperContext
				.put("listOfParams", operation.getInputs().getParameter());
		for (Parameter parameter : operation.getInputs().getParameter()) {
			if (contextForCommand.containsKey(parameter.getName())) {
				log.error("Operation \"" + operation.getName()
						+ "\" already contains an parameter called \""
						+ parameter.getName() + "\"...");
			}
			if (parameter.isRequired()) {
				verifyRequiredArguments.add("${param_files" + i
						+ Constants.BASHGENERATOR_ARRAY_FINAL_STR);
			}
			contextForCommand.put(parameter.getName(), "${param_files" + i
					+ Constants.BASHGENERATOR_ARRAY_FINAL_STR);
			wrapperParameters.append("-p %%" + parameter.getName()
					+ "_inner%% ");
			i++;
		}

		wrapperContext.put("listOfOutputs", operation.getOutputs().getOutput());
		i = 1;
		for (Output output : operation.getOutputs().getOutput()) {
			if (contextForCommand.containsKey(output.getName())) {
				log.error("Operation \"" + operation.getName()
						+ "\" already contains an output called \""
						+ output.getName() + "\"...");
			}
			if (output.isRequired()) {
				verifyRequiredArguments.add("${output_files" + i
						+ Constants.BASHGENERATOR_ARRAY_FINAL_STR);
			}
			contextForCommand.put(output.getName(),
					wrapWithDoubleQuotes("${output_files" + i
							+ Constants.BASHGENERATOR_ARRAY_FINAL_STR));
			wrapperParameters.append("-o %%" + output.getName() + "_inner%% ");
			i++;
		}
		wrapperContext
				.put("verify_required_arguments", verifyRequiredArguments);
		wrapperContext.put("wrapperParameters", wrapperParameters.toString());

		StringWriter w = new StringWriter();
		contextForCommand.put("param", "");
		Velocity.evaluate(contextForCommand, w, "command", command);
		wrapperContext.put("command", w);
	}

	private void addUsageInformationToContext(VelocityContext wrapperContext) {
		wrapperContext.put("usageDescription", operation.getDescription());
		addInputUsageInformationToContext(wrapperContext);
		addParamUsageInformationToContext(wrapperContext);
		addOutputUsageInformationToContext(wrapperContext);
	}

	private void addInputUsageInformationToContext(
			VelocityContext wrapperContext) {
		if (operation.getInputs().getStdin() != null) {
			wrapperContext.put("usageInputParameter", "-i STDIN");
			wrapperContext.put("usageInputParameterDescription",
					"-i STDIN > Read input from the STDIN");
		} else {
			// user input parameters
			StringBuilder uip = new StringBuilder("");
			// user input parameters description to print on the bash usage
			// function
			StringBuilder uipd = new StringBuilder("");
			// user input parameters description for man page
			StringBuilder uipdman = new StringBuilder("");
			int i = 0;
			for (Input input : operation.getInputs().getInput()) {
				String value = "-i " + input.getName()
						+ (i == 0 ? "|STDIN" : "");
				uip.append((uip.length() == 0 ? "" : " "));
				if (input.isRequired()) {
					uip.append(value);
				} else {
					uip.append("[" + value + "]");
				}
				String description = value + " > " + input.getDescription()
						+ (i == 0 ? " OR Read input from the STDIN" : "");
				uipd.append((uipd.length() != 0 ? "\n\t" : "") + description);
				uipdman.append(description + "\n\n");
				i++;
			}
			wrapperContext.put("usageInputParameter", uip.toString());
			wrapperContext.put("usageInputParameterDescription",
					uipd.toString());
			wrapperContext.put("usageInputParameterDescriptionForMan",
					uipdman.toString());
		}
	}

	private void addParamUsageInformationToContext(
			VelocityContext wrapperContext) {
		// user input parameters
		StringBuilder uip = new StringBuilder("");
		// user input parameters description to print on the bash usage function
		StringBuilder uipd = new StringBuilder("");
		// user input parameters description for man page
		StringBuilder uipdman = new StringBuilder("");
		for (Parameter param : operation.getInputs().getParameter()) {
			String value = "-p " + param.getName();
			uip.append((uip.length() == 0 ? "" : " "));
			if (param.isRequired()) {
				uip.append(value);
			} else {
				uip.append("[" + value + "]");
			}
			String description = value + " > " + param.getDescription();
			uipd.append((uipd.length() != 0 ? "\n\t" : "") + description);
			uipdman.append(description + "\n\n");
		}
		wrapperContext.put("usageParamParameter", uip.toString());
		wrapperContext.put("usageParamParameterDescription", uipd.toString());
		wrapperContext.put("usageParamParameterDescriptionForMan",
				uipdman.toString());
	}

	private void addOutputUsageInformationToContext(
			VelocityContext wrapperContext) {
		if (operation.getOutputs().getStdout() != null) {
			wrapperContext.put("usageOutputParameter", "-o STDOUT");
			wrapperContext.put("usageOutputParameterDescription",
					"-o STDOUT > Write output to the STDOUT");
		} else {
			// user input parameters
			StringBuilder uip = new StringBuilder("");
			// user input parameters description to print on the bash usage
			// function
			StringBuilder uipd = new StringBuilder("");
			// user input parameters description for man page
			StringBuilder uipdman = new StringBuilder("");
			int i = 0;
			for (Output output : operation.getOutputs().getOutput()) {
				String value = "-o " + output.getName()
						+ (i == 0 ? "|STDOUT" : "");
				uip.append((uip.length() == 0 ? "" : " "));
				if (output.isRequired()) {
					uip.append(value);
				} else {
					uip.append("[" + value + "]");
				}
				String description = value + " > " + output.getDescription()
						+ (i == 0 ? " OR Write output to the STDOUT" : "");
				uipd.append((uipd.length() != 0 ? "\n\t" : "") + description);
				uipdman.append(description + "\n\n");
				i++;
			}
			wrapperContext.put("usageOutputParameter", uip.toString());
			wrapperContext.put("usageOutputParameterDescription",
					uipd.toString());
			wrapperContext.put("usageOutputParameterDescriptionForMan",
					uipdman.toString());
		}
	}

	private void initVelocity() {
		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		p.setProperty("runtime.log.logsystem.log4j.logger",
				"org.apache.velocity");
		Velocity.init(p);
	}

	private boolean loadBashWrapperTemplate() {
		bashWrapperTemplate = loadVelocityTemplateFromResources("wrapper_template.vm");
		return bashWrapperTemplate != null;
	}

	private Template loadVelocityTemplateFromResources(String templatePath) {
		Template template = null;
		try {
			template = Velocity.getTemplate(templatePath, "UTF-8");
		} catch (ResourceNotFoundException e) {
			log.error(e);
		} catch (ParseErrorException e) {
			log.error(e);
		}
		return template;
	}

	private String wrapWithDoubleQuotes(String in) {
		return "\"" + in + "\"";
	}

	private void addGeneralInformationToContext(VelocityContext context) {
		context.put("toolName", tool.getName());
		context.put("toolHomepage", tool.getHomepage());
		context.put("toolVersion", tool.getVersion());
		context.put("wrapperName", wrapperName);
	}

	public static void main(String[] args) {
		BashWrapperGenerator bwg = new BashWrapperGenerator();
		ImmutablePair<CommandLine, Tool> pair = bwg
				.processToolWrapperGenerationRequest(args);
		CommandLine cmd = null;
		Tool tool = null;
		int exitCode = 0;
		if (pair != null) {
			cmd = pair.getLeft();
			tool = pair.getRight();

			for (Operation operation : tool.getOperations().getOperation()) {

				// define wrapper name as operation name
				bwg.setWrapperName(operation.getName());

				// generate the wrapper and Taverna workflow
				boolean generationOK = bwg.generateWrapper(tool, operation,
						cmd.getOptionValue("o"));
				if (!generationOK) {
					log.error("Error generating bash wrapper for operation \""
							+ operation.getName() + "\"");
				}
			}
		} else {

			// TODO do better error handling
			// error processing cmd arguments or creating tool instance
			bwg.printUsage();
			exitCode = 1;
		}
		System.exit(exitCode);
	}
}