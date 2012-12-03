package eu.scape_project.core.toolspec_objects.utils;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.scape_project.core.toolspec_objects.from_schema.Tool;

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
