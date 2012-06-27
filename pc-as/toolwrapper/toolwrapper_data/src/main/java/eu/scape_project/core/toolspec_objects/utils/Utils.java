package eu.scape_project.core.toolspec_objects.utils;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.scape_project.core.toolspec_objects.from_schema.Tool;

public final class Utils {

	private Utils() {
	}

	/** Method that creates a {@link Tool} instance from a toolspec filename */
	public static Tool createTool(String toolFileName) {
		Tool tool = null;
		try {
			JAXBContext context = JAXBContext.newInstance(Tool.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			tool = (Tool) unmarshaller.unmarshal(new File(toolFileName));
		} catch (JAXBException e) {
			System.err.println(e);
		}
		return tool;
	}
}
