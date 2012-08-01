package eu.scape_project.sanselan_wrapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;

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

public class SanselanWrapper extends Sanselan {
	public static Map<String, ImageFormat> writableExtensionToImageFormat = new HashMap<String, ImageFormat>();
	static {
		/* the following (comments) are not writable file formats */
		// writableExtensionToImageFormat.put("PSD",
		// ImageFormat.IMAGE_FORMAT_PSD);
		// writableExtensionToImageFormat.put("ICO",
		// ImageFormat.IMAGE_FORMAT_ICO);
		// writableExtensionToImageFormat.put("JPEG",
		// ImageFormat.IMAGE_FORMAT_JPEG);
		writableExtensionToImageFormat.put("PNG", ImageFormat.IMAGE_FORMAT_PNG);
		writableExtensionToImageFormat.put("GIF", ImageFormat.IMAGE_FORMAT_GIF);
		writableExtensionToImageFormat.put("TIFF",
				ImageFormat.IMAGE_FORMAT_TIFF);
		writableExtensionToImageFormat.put("BMP", ImageFormat.IMAGE_FORMAT_BMP);
		writableExtensionToImageFormat.put("PBM", ImageFormat.IMAGE_FORMAT_PBM);
		writableExtensionToImageFormat.put("PGM", ImageFormat.IMAGE_FORMAT_PGM);
		writableExtensionToImageFormat.put("PPM", ImageFormat.IMAGE_FORMAT_PPM);
		writableExtensionToImageFormat.put("PNM", ImageFormat.IMAGE_FORMAT_PNM);
		writableExtensionToImageFormat.put("TGA", ImageFormat.IMAGE_FORMAT_TGA);
		writableExtensionToImageFormat.put("JBig2",
				ImageFormat.IMAGE_FORMAT_JBIG2);
	}

	public static BufferedImage load(String inputFile,
			Hashtable<String, Object> params) throws ImageReadException,
			IOException {
		File inImageFile = new File(inputFile);
		return Sanselan.getBufferedImage(inImageFile, params);
	}

	public static void convert(BufferedImage image, String outputFile,
			Hashtable<String, Object> params) throws ImageWriteException,
			IOException, ImageReadException {
		int lastDotPosition = outputFile.lastIndexOf(".");
		String extension = outputFile.substring(lastDotPosition + 1);
		extension = extension.toUpperCase();
		ImageFormat outFormat = writableExtensionToImageFormat.get(extension);
		if (outFormat != null) {
			File outImageFile = new File(outputFile);
			Sanselan.writeImage(image, outImageFile, outFormat, params);
		} else {
			throw new ImageWriteException(
					"Can't write to the provided output file format (\""
							+ extension.toLowerCase() + "\")");
		}
	}
}
