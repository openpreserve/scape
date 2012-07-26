package eu.scape_project.imageio_wrapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;

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

public class ImageIOWrapper {
	public static List<String> writableExtensions = new ArrayList<String>();
	static {
		writableExtensions.add("jpeg");
		writableExtensions.add("png");
		writableExtensions.add("bmp");
		writableExtensions.add("wbmp");
		writableExtensions.add("gif");
	}

	public static BufferedImage load(String inputFile,
			Hashtable<String, Object> params) throws IOException {
		File inImageFile = new File(inputFile);
		return ImageIO.read(inImageFile);
	}

	public static void convert(BufferedImage image, String outputFile,
			Hashtable<String, Object> params) throws IOException {
		int lastDotPosition = outputFile.lastIndexOf(".");
		String extension = outputFile.substring(lastDotPosition + 1);
		extension = extension.toLowerCase();
		if (writableExtensions.contains(extension)) {
			File outImageFile = new File(outputFile);
			ImageIO.write(image, extension, outImageFile);
		} else {
			throw new IOException(
					"Can't write to the provided output file format (\""
							+ extension.toLowerCase() + "\")");
		}
	}
}
