package eu.scape_project.core.toolwrapper.bash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import eu.scape_project.core.toolspec_objects.from_schema.Input;
import eu.scape_project.core.toolspec_objects.from_schema.Operation;
import eu.scape_project.core.toolspec_objects.from_schema.Output;
import eu.scape_project.core.toolspec_objects.from_schema.Tool;

public class BashWrapperGenerator {
	private Tool tool;
	private Operation operation;
	private String outputDirectory;
	private Template template;

	public BashWrapperGenerator(Tool tool, Operation operation,
			String outputDirectory) {
		this.tool = tool;
		this.operation = operation;
		this.outputDirectory = outputDirectory;
		this.template = null;
	}

	public void generateWrapper() {
		initVelocity();
		if (loadTemplate()) {
			VelocityContext context = new VelocityContext();

			addGeneralInformationToContext(context);
			addUsageInformationToContext(context);
			addCommandInformationToContext(context);

			StringWriter w = new StringWriter();
			template.merge(context, w);
			writeBashWrapper(this.operation.getName(), w);
		}
	}

	private void addCommandInformationToContext(VelocityContext context) {
		String command = operation.getCommand();
		VelocityContext context4command = new VelocityContext();

		context.put("listOfInputs", operation.getInputs().getInput());
		int i = 1;
		for (Input input : operation.getInputs().getInput()) {
			if (context4command.containsKey(input.getName())) {
				System.err.println("Operation \"" + operation.getName()
						+ "\" already contains an input called \""
						+ input.getName() + "\"...");
			}
			context4command.put(input.getName(),
					wrapWithDoubleQuotes("${input_files" + i + "[@]}"));
			i++;
		}

		context.put("listOfOutputs", operation.getOutputs().getOutput());
		i = 1;
		for (Output output : operation.getOutputs().getOutput()) {
			if (context4command.containsKey(output.getName())) {
				System.err.println("Operation \"" + operation.getName()
						+ "\" already contains an output called \""
						+ output.getName() + "\"...");
			}
			context4command.put(output.getName(),
					wrapWithDoubleQuotes("${output_files" + i + "[@]}"));
			i++;
		}

		StringWriter w = new StringWriter();
		context4command.put("param", "");
		Velocity.evaluate(context4command, w, "command", command);
		context.put("command", w);
	}

	private void addUsageInformationToContext(VelocityContext context) {
		context.put("usageDescription", operation.getDescription());
		addInputUsageInformationToContext(context);
		addOutputUsageInformationToContext(context);
	}

	private void addInputUsageInformationToContext(VelocityContext context) {
		if (operation.getInputs().getStdin() != null) {
			context.put("usageInputParameter", "-i STDIN");
			context.put("usageInputParameterDescription",
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
			context.put("usageInputParameter", uip.toString());
			context.put("usageInputParameterDescription", uipd.toString());
		}
	}

	private void addOutputUsageInformationToContext(VelocityContext context) {
		if (operation.getOutputs().getStdout() != null) {
			context.put("usageOutputParameter", "-o STDOUT");
			context.put("usageOutputParameterDescription",
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
			context.put("usageOutputParameter", uip.toString());
			context.put("usageOutputParameterDescription", uipd.toString());
		}
	}

	private void initVelocity() {
		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
	}

	private boolean loadTemplate() {
		boolean ret = true;
		try {
			template = Velocity.getTemplate("wrapper.vm", "UTF-8");
		} catch (ResourceNotFoundException e) {
			ret = false;
			System.err.println(e);
		} catch (ParseErrorException e) {
			ret = false;
			System.err.println(e);
		}
		return ret;
	}

	private void writeBashWrapper(String name, StringWriter w) {
		// INFO next line of code was commented because naming wrapper <name +
		// ".sh"> may overwrite some migrators .sh that are used to simplify the
		// migration invocation in the toolspec
		// File wrapper = new File(outputDirectory, name + ".sh");
		File wrapper = new File(outputDirectory, name);
		FileOutputStream fos = null;
		if (wrapper != null) {
			try {
				fos = new FileOutputStream(wrapper);
				fos.write(w.toString().getBytes());
			} catch (FileNotFoundException e) {
				System.err.println(e);
			} catch (IOException e) {
				System.err.println(e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						System.err.println(e);
					}
				}
			}
		}
	}

	private String wrapWithDoubleQuotes(String in) {
		return "\"" + in + "\"";
	}

	private void addGeneralInformationToContext(VelocityContext context) {
		context.put("toolName", tool.getName());
		context.put("toolHomepage", tool.getHomepage());
	}
}
