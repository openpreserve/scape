package eu.scape_project.core.toolwrapper.bash;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import eu.scape_project.core.toolspec_objects.from_schema.Operation;
import eu.scape_project.core.toolspec_objects.from_schema.Tool;

public class BashWrapperGenerator {
	private Tool tool;
	private Operation operation;

	public BashWrapperGenerator(Tool tool, Operation operation) {
		this.tool = tool;
		this.operation = operation;
	}

	public void generateWrapper() {
		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		p.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		Velocity.init(p);
		Template template = null;

		try {
			template = Velocity.getTemplate("wrapper.vm", "UTF-8");
			VelocityContext context = new VelocityContext();

			addGeneralInformationToContext(context);
			addUsageInformationToContext(context);

			StringWriter w = new StringWriter();
			template.merge(context, w);
			System.out.println(w + "\n\n");
		} catch (ResourceNotFoundException e) {
			System.err.println(e);
		} catch (ParseErrorException e) {
			System.err.println(e);
		}
	}

	private void addGeneralInformationToContext(VelocityContext context) {
		context.put("command", operation.getCommand());
		context.put("toolName", tool.getName());
		context.put("toolHomepage", tool.getHomepage());
	}

	private void addUsageInformationToContext(VelocityContext context) {
		context.put("usageDescription", operation.getDescription());
		if (operation.getInputs().getStdin() != null) {
			context.put("usageInputParameter", "-i STDIN");
			context.put("usageInputParameterDescription",
					"-i STDIN > Read input from the STDIN");
		} else {
			// FIXME set proper value
			context.put("usageInputParameter", "-i FILE");
			// FIXME iterate over all inputs
			context.put("usageInputParameterDescription", "-i FILE > "
					+ operation.getInputs().getInput().get(0).getDescription());
		}
		if (operation.getOutputs().getStdout() != null) {
			context.put("usageOutputParameter", "-o STDOUT");
			context.put("usageOutputParameterDescription",
					"-o STDOUT > Write output to the STDOUT");
		} else {
			// FIXME set proper value
			context.put("usageOutputParameter", "-o FILE");
			// FIXME iterate over all outputs
			context.put("usageOutputParameterDescription", "-o FILE > "
					+ operation.getOutputs().getOutput().get(0)
							.getDescription());
		}
	}
}
