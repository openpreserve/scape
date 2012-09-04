package eu.scape_project.tool.bash_generator;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import eu.scape_project.tool.data.Input;
import eu.scape_project.tool.data.OperatingSystemDependency;
import eu.scape_project.tool.data.Operation;
import eu.scape_project.tool.data.Output;
import eu.scape_project.tool.data.Parameter;
import eu.scape_project.tool.data.Tool;
import eu.scape_project.tool.toolwrapper.ToolWrapperCommandline;
import eu.scape_project.tool.toolwrapper.ToolWrapperGenerator;

public class BashWrapperGenerator extends ToolWrapperCommandline implements
		ToolWrapperGenerator {
	private Tool tool;
	private Operation operation;
	private String wrapperName;
	private String maintainerEmail;
	private Template bashWrapperTemplate;
	private File debianDir, tempDir, tempDir2;
	private Map<String, Template> debianTemplates;

	private static final String RFC_822_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss Z";
	private static final String DEBIAN_TEMPLATE_DIRECTORY_NAME = "/bash_debian_template/";

	public BashWrapperGenerator() {
		tool = null;
		operation = null;
		wrapperName = null;
		maintainerEmail = null;
		bashWrapperTemplate = null;
		debianDir = null;
		debianTemplates = null;
	}

	/* ****************************************************
	 * getters and setters ********************************
	 * ****************************************************
	 */
	public BashWrapperGenerator setTool(Tool tool) {
		this.tool = tool;
		return this;
	}

	public BashWrapperGenerator setOperation(Operation operation) {
		this.operation = operation;
		return this;
	}

	public BashWrapperGenerator setWrapperName(String wrapperName) {
		this.wrapperName = wrapperName;
		return this;
	}

	public BashWrapperGenerator setMaintainerEmail(String maintainerEmail) {
		this.maintainerEmail = maintainerEmail;
		return this;
	}

	public String getWrapperName() {
		return wrapperName;
	}

	/* ****************************************************
	 * create wrapper and Debian package logic ************
	 * ****************************************************
	 */
	@Override
	public boolean generateWrapper(Tool tool, Operation operation,
			String outputDirectory, boolean generateDebianPackage,
			String maintainerEmail) {
		this.tool = tool;
		this.operation = operation;
		this.maintainerEmail = maintainerEmail;

		boolean res = true;
		initVelocity();
		if (loadBashWrapperTemplate()) {
			VelocityContext wrapperContext = new VelocityContext();

			addGeneralInformationToContext(wrapperContext);
			addUsageInformationToContext(wrapperContext);
			addCommandInformationToContext(wrapperContext);

			StringWriter bashWrapperSW = new StringWriter();
			bashWrapperTemplate.merge(wrapperContext, bashWrapperSW);
			res = res
					&& writeTemplateContent(outputDirectory,
							operation.getName(), bashWrapperSW, true);
			if (generateDebianPackage) {
				res = res && generateDebianPackage(wrapperContext);
				res = res
						&& writeTemplateContent(tempDir2.getAbsolutePath(),
								operation.getName(), bashWrapperSW, true);
			}
		} else {
			res = false;
		}
		return res;
	}

	private boolean generateDebianPackage(VelocityContext context) {
		return generateDebianPackageDirectoryAndInfo(context)
				&& generateWorkflowFileAndInfo(context) && generatePackage();
	}

	// FIXME not working as it should (debuild fails when executed through java
	// but in the bash it works well
	private boolean generatePackage() {
		boolean success = true;
		System.out
				.println("Now, execute the command \"/usr/bin/debuild -us -uc -b\" in the directory \""
						+ tempDir2.getAbsolutePath() + "\"");
		// System.out.println("generatePackage starting now...");
		// Runtime rt = Runtime.getRuntime();
		// try {
		// Process exec = rt.exec("/usr/bin/debuild -us -uc -b", null,
		// tempDir2);
		// // Process exec = rt
		// // .exec("/usr/bin/dpkg-buildpackage", null, tempDir2);
		// InputStream inputStream = exec.getInputStream();
		// BufferedReader br = new BufferedReader(new InputStreamReader(
		// inputStream));
		// String line;
		// while ((line = br.readLine()) != null) {
		// System.out.println(line);
		// }
		// success = (exec.waitFor() == 0);
		// } catch (InterruptedException e) {
		// success = false;
		// } catch (IOException e) {
		// success = false;
		// }
		return success;
	}

	private boolean generateDebianPackageDirectoryAndInfo(
			VelocityContext context) {
		return createDebianPackageDirectorySkeleton() && loadDebianTemplates()
				&& addInformationToDebianTemplates(context);
	}

	private boolean addInformationToDebianTemplates(VelocityContext context) {
		SimpleDateFormat sdf = new SimpleDateFormat(RFC_822_DATE_PATTERN,
				Locale.ENGLISH);
		context.put("dateOfGeneration", sdf.format(new Date()));

		context.put("maintainerEmail", maintainerEmail);
		context.put("operationName", operation.getName());

		List<OperatingSystemDependency> dependencyList = tool.getInstallation()
				.getDependency();
		String dependencies = "";
		for (OperatingSystemDependency osd : dependencyList) {
			if ("Debian".equalsIgnoreCase(osd.getOperatingSystemName()
					.toString())) {
				dependencies = osd.getValue();
				break;
			}
		}
		context.put("toolDependencies", dependencies);

		context.put("toolDescription", operation.getDescription());
		context.put("toolHomepage", tool.getHomepage());
		context.put("toolVersion", tool.getVersion());

		context.put("folder", tempDir2.getAbsolutePath());

		for (Entry<String, Template> debianTemplateEntry : debianTemplates
				.entrySet()) {
			StringWriter sw = new StringWriter();
			debianTemplateEntry.getValue().merge(context, sw);
			writeTemplateContent(
					debianDir.getAbsolutePath(),
					debianTemplateEntry.getKey().replaceFirst("MAN",
							operation.getName()), sw, false);
		}

		return true;
	}

	private boolean loadDebianTemplates() {
		boolean success = true;
		if (debianTemplates == null) {
			debianTemplates = new HashMap<String, Template>();
			debianTemplates
					.put("source/format",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "source/format"));
			debianTemplates
					.put("changelog",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "changelog"));
			debianTemplates
					.put("compat",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "compat"));
			debianTemplates
					.put("control",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "control"));
			debianTemplates
					.put("copyright",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "copyright"));
			debianTemplates
					.put("dirs",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "dirs"));
			debianTemplates
					.put("docs",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "docs"));
			debianTemplates
					.put("install",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "install"));
			debianTemplates
					.put("MAN.manpages",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "MAN.manpages"));
			debianTemplates
					.put("MAN.pod",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "MAN.pod"));
			debianTemplates
					.put("README",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "README"));
			debianTemplates
					.put("rules",
							loadVelocityTemplateFromResources(DEBIAN_TEMPLATE_DIRECTORY_NAME
									+ "rules"));
		}
		return success;
	}

	private boolean createDebianPackageDirectorySkeleton() {
		boolean success = true;
		tempDir = createTemporaryDirectory(operation.getName());
		tempDir2 = new File(tempDir, operation.getName());
		success = success && tempDir2.mkdir();
		debianDir = new File(tempDir2, "debian");
		success = success && debianDir.mkdir();
		if (debianDir != null) {
			File sourceDir = new File(debianDir, "source");
			success = success && sourceDir.mkdir();
		}
		return success;
	}

	// private boolean copyResourceToTemporaryDirectory(File tempDir,
	// String resourcePath, String resourceName, String newName,
	// boolean createTempDir) {
	// boolean success = false;
	// try {
	// if (createTempDir) {
	// tempDir.mkdir();
	// }
	// File f = new File(tempDir, newName != null ? newName : resourceName);
	// IOUtils.copy(
	// getClass().getResourceAsStream(resourcePath + resourceName),
	// new FileOutputStream(f));
	// success = true;
	// } catch (IOException e) {
	// System.err.println(e);
	// }
	// return success;
	// }

	private File createTemporaryDirectory(String name) {
		File tempDir = null;
		try {
			tempDir = File.createTempFile(name, "");
			if (!(tempDir.delete() && tempDir.mkdir())) {
				tempDir = null;
				System.err.println("Error creating temp folder");
			}
		} catch (IOException e) {
			System.err.println("Error while creating temporary folder (\""
					+ name + "\")");
		}
		return tempDir;
	}

	// TODO improve workflow generation (as it is, it only accepts one input and
	// has one output
	private boolean generateWorkflowFileAndInfo(VelocityContext context) {
		boolean success = true;
		Template workflowTemplate = loadVelocityTemplateFromResources("bash_workflow_template.t2flow");
		UUID randomUUID = UUID.randomUUID();
		context.put("uniqID", randomUUID);
		context.put("listOfInputs", operation.getInputs().getInput());
		context.put("listOfOutputs", operation.getOutputs().getOutput());
		context.put("listOfParams", operation.getInputs().getParameter());
		StringWriter sw = new StringWriter();
		workflowTemplate.merge(context, sw);
		writeTemplateContent(tempDir2.getAbsolutePath(), operation.getName()
				+ "_bash.t2flow", sw, false);
		return success;
	}

	private void addCommandInformationToContext(VelocityContext wrapperContext) {
		String command = operation.getCommand();
		VelocityContext contextForCommand = new VelocityContext();

		wrapperContext.put("listOfInputs", operation.getInputs().getInput());
		int i = 1;
		List<String> verify_required_arguments = new ArrayList<String>();
		for (Input input : operation.getInputs().getInput()) {
			if (contextForCommand.containsKey(input.getName())) {
				System.err.println("Operation \"" + operation.getName()
						+ "\" already contains an input called \""
						+ input.getName() + "\"...");
			}
			if (input.isRequired()) {
				verify_required_arguments.add("${input_files" + i + "[@]}");
			}
			contextForCommand.put(input.getName(),
					wrapWithDoubleQuotes("${input_files" + i + "[@]}"));
			i++;
		}
		i = 1;
		wrapperContext
				.put("listOfParams", operation.getInputs().getParameter());
		for (Parameter parameter : operation.getInputs().getParameter()) {
			if (contextForCommand.containsKey(parameter.getName())) {
				System.err.println("Operation \"" + operation.getName()
						+ "\" already contains an parameter called \""
						+ parameter.getName() + "\"...");
			}
			if (parameter.isRequired()) {
				verify_required_arguments.add("${param_files" + i + "[@]}");
			}
			// INFO the next line was commented so bash won't process the stuff
			// inside quotes (single/double)
			// context4command.put(parameter.getName(),
			// wrapWithDoubleQuotes("${param_files" + i + "[@]}"));
			contextForCommand.put(parameter.getName(), "${param_files" + i
					+ "[@]}");
			i++;
		}

		wrapperContext.put("listOfOutputs", operation.getOutputs().getOutput());
		i = 1;
		for (Output output : operation.getOutputs().getOutput()) {
			if (contextForCommand.containsKey(output.getName())) {
				System.err.println("Operation \"" + operation.getName()
						+ "\" already contains an output called \""
						+ output.getName() + "\"...");
			}
			if (output.isRequired()) {
				verify_required_arguments.add("${output_files" + i + "[@]}");
			}
			contextForCommand.put(output.getName(),
					wrapWithDoubleQuotes("${output_files" + i + "[@]}"));
			i++;
		}
		wrapperContext.put("verify_required_arguments",
				verify_required_arguments);

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
			StringBuilder uip = new StringBuilder("");
			StringBuilder uipd = new StringBuilder("");
			for (Input input : operation.getInputs().getInput()) {
				String value = "-i " + input.getName();
				uip.append((uip.length() == 0 ? "" : " ") + value);
				uipd.append((uipd.length() != 0 ? "\n\t" : "") + value + " > "
						+ input.getDescription());
			}
			wrapperContext.put("usageInputParameter", uip.toString());
			wrapperContext.put("usageInputParameterDescription",
					uipd.toString());
		}
	}

	private void addParamUsageInformationToContext(
			VelocityContext wrapperContext) {
		StringBuilder uip = new StringBuilder("");
		StringBuilder uipd = new StringBuilder("");
		for (Parameter param : operation.getInputs().getParameter()) {
			String value = "-p " + param.getName();
			uip.append((uip.length() == 0 ? "" : " ") + value);
			uipd.append((uipd.length() != 0 ? "\n\t" : "") + value + " > "
					+ param.getDescription());
		}
		wrapperContext.put("usageParamParameter", uip.toString());
		wrapperContext.put("usageParamParameterDescription", uipd.toString());
	}

	private void addOutputUsageInformationToContext(
			VelocityContext wrapperContext) {
		if (operation.getOutputs().getStdout() != null) {
			wrapperContext.put("usageOutputParameter", "-o STDOUT");
			wrapperContext.put("usageOutputParameterDescription",
					"-o STDOUT > Write output to the STDOUT");
		} else {
			StringBuilder uip = new StringBuilder("");
			StringBuilder uipd = new StringBuilder("");
			for (Output output : operation.getOutputs().getOutput()) {
				String value = "-o " + output.getName();
				uip.append((uip.length() == 0 ? "" : " ") + value);
				uipd.append((uipd.length() != 0 ? "\n\t" : "") + value + " > "
						+ output.getDescription());
			}
			wrapperContext.put("usageOutputParameter", uip.toString());
			wrapperContext.put("usageOutputParameterDescription",
					uipd.toString());
		}
	}

	private void initVelocity() {
		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
	}

	private boolean loadBashWrapperTemplate() {
		bashWrapperTemplate = loadVelocityTemplateFromResources("wrapper.vm");
		return bashWrapperTemplate != null;
	}

	private Template loadVelocityTemplateFromResources(String templatePath) {
		Template template = null;
		try {
			template = Velocity.getTemplate(templatePath, "UTF-8");
		} catch (ResourceNotFoundException e) {
			System.err.println(e);
		} catch (ParseErrorException e) {
			System.err.println(e);
		}
		return template;
	}

	private boolean writeTemplateContent(String outputDirectory,
			String filename, StringWriter w, boolean makeItExecutable) {
		// INFO next line of code was commented because naming wrapper <name +
		// ".sh"> may overwrite some migrators .sh that are used to simplify the
		// migration invocation in the toolspec
		// File wrapper = new File(outputDirectory, name + ".sh");
		boolean res = true;
		File directory = new File(outputDirectory);
		if (!directory.exists() && !directory.mkdir()) {
			System.err.println("Error creating directory \"" + directory
					+ "\"...");
			res = false;
		} else {
			File file = new File(directory, filename);
			FileOutputStream fos = null;
			if (file != null) {
				try {
					fos = new FileOutputStream(file);
					fos.write(w.toString().getBytes());
				} catch (FileNotFoundException e) {
					System.err.println(e);
					res = false;
				} catch (IOException e) {
					System.err.println(e);
					res = false;
				} finally {
					if (fos != null) {
						try {
							fos.close();
							file.setExecutable(makeItExecutable);
						} catch (IOException e) {
							System.err.println(e);
							res = false;
						}
					}
				}
			}
		}
		return res;
	}

	private String wrapWithDoubleQuotes(String in) {
		return "\"" + in + "\"";
	}

	private void addGeneralInformationToContext(VelocityContext context) {
		context.put("toolName", tool.getName());
		context.put("toolHomepage", tool.getHomepage());
	}

	public static void main(String[] args) {
		BashWrapperGenerator bwg = new BashWrapperGenerator();
		ImmutablePair<CommandLine, Tool> pair = bwg
				.processToolWrapperGenerationRequest(args);
		CommandLine cmd = null;
		Tool tool = null;
		if (pair != null) {
			cmd = pair.getLeft();
			tool = pair.getRight();

			for (Operation operation : tool.getOperations().getOperation()) {

				// generate the wrapper and if chosen the Debian package
				bwg.generateWrapper(tool, operation, cmd.getOptionValue("o"),
						cmd.hasOption("d"), cmd.getOptionValue("e"));
			}
		} else {

			// error processing cmd arguments or creating tool instance
			bwg.printUsage(true, 1);
		}

	}
}
