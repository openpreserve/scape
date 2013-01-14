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
package eu.scape_project.tool.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public final class Utils {
	private static Logger log = Logger.getLogger(Utils.class);

	private Utils() {

	}

	public static File createTemporaryDirectory(String name) {
		File tempDir = null;
		try {
			tempDir = File.createTempFile(name, "");
			if (!(tempDir.delete() && tempDir.mkdir())) {
				tempDir = null;
				log.error("Error creating temp folder");
			}
		} catch (IOException e) {
			log.error("Error while creating temporary folder (\"" + name
					+ "\")");
		}
		return tempDir;
	}

	public static boolean copyResourceToTemporaryDirectory(File tempDir,
			String resourcePath, String resourceName, String newName,
			boolean createTempDir, Class<?> invokingClass) {
		boolean success = false;
		FileOutputStream fileOutputStream = null;
		try {
			if (createTempDir && !tempDir.mkdir()) {
				log.error("Error creating directory \"" + tempDir + "\"...");
			}
			File f = new File(tempDir, newName != null ? newName : resourceName);
			fileOutputStream = new FileOutputStream(f);
			IOUtils.copy(
					invokingClass.getResourceAsStream(resourcePath
							+ resourceName), fileOutputStream);
			success = true;
		} catch (IOException e) {
			log.error(e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		}
		return success;
	}

	public static boolean writeTemplateContent(String outputDirectory,
			String childDirectory, String filename, StringWriter w,
			boolean makeItExecutable) {
		boolean res = true;
		File directory;
		if (childDirectory != null) {
			directory = new File(outputDirectory, childDirectory);
		} else {
			directory = new File(outputDirectory);
		}
		File file = new File(directory, filename);
		FileOutputStream fos = null;
		if (file != null) {
			try {
				fos = new FileOutputStream(file);
				fos.write(w.toString().getBytes(Charset.defaultCharset()));
			} catch (FileNotFoundException e) {
				log.error(e);
				res = false;
			} catch (IOException e) {
				log.error(e);
				res = false;
			} finally {
				if (fos != null) {
					try {
						fos.close();
						file.setExecutable(makeItExecutable);
					} catch (IOException e) {
						log.error(e);
						res = false;
					}
				}
			}
		}
		return res;
	}

	public static boolean copyFile(File source, File destination,
			boolean setExecutable) {
		boolean success = true;
		if (source.exists()) {
			try {
				FileUtils.copyFile(source, destination);
				if (!destination.setExecutable(setExecutable)) {
					log.error("Error while setting execution permissions on \""
							+ destination + "\"...");
				}
			} catch (IOException e) {
				log.error("Error while copying file \"" + source + "\" to \""
						+ destination + "\"...");
				success = false;
			}
		} else {
			log.error("File \"" + source + "\" does not exists...");
			success = false;
		}
		return success;
	}

	public static String wrapWithDoubleQuotes(String in) {
		return "\"" + in + "\"";
	}

}
