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
package eu.scape_project.tool.data.utils;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import eu.scape_project.tool.data.Tool;

public final class Utils {
	private static Logger log = Logger.getLogger(Utils.class);
	private static final String TOOLSPEC_FILENAME_IN_RESOURCES = "/tool-1.0_draft.xsd";

	private Utils() {
	}

	/**
	 * Method that creates a {@link Tool} instance from a toolspec filename,
	 * validating it against toolspec XML Schema
	 */
	public static Tool createTool(String toolFilename) {
		Tool tool = null;
		File schemaFile = null;
		try {
			JAXBContext context = JAXBContext.newInstance(Tool.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();

			// copy XML Schema from resources to a temporary location
			schemaFile = File.createTempFile("schema", null);
			FileUtils.copyInputStreamToFile(Utils.class
					.getResourceAsStream(TOOLSPEC_FILENAME_IN_RESOURCES),
					schemaFile);
			Schema schema = SchemaFactory.newInstance(
					XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaFile);

			// validate provided toolspec against XML Schema
			unmarshaller.setSchema(schema);

			// unmarshal it
			tool = (Tool) unmarshaller.unmarshal(new File(toolFilename));
		} catch (JAXBException e) {
			log.error(
					"The toolspec provided doesn't validate against its schema!",
					e);
		} catch (SAXException e) {
			log.error("The XML Schema is not valid!", e);
		} catch (IOException e) {
			log.error(
					"An error occured while copying the XML Schema from the resources to a temporary location!",
					e);
		} finally {
			if (schemaFile != null) {
				schemaFile.deleteOnExit();
			}
		}
		return tool;
	}
}
