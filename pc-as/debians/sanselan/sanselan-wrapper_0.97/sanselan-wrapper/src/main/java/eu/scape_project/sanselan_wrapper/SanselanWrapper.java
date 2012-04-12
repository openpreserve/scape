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
		writableExtensionToImageFormat.put("TIFF", ImageFormat.IMAGE_FORMAT_TIFF);
		writableExtensionToImageFormat.put("BMP", ImageFormat.IMAGE_FORMAT_BMP);
		writableExtensionToImageFormat.put("PBM", ImageFormat.IMAGE_FORMAT_PBM);
		writableExtensionToImageFormat.put("PGM", ImageFormat.IMAGE_FORMAT_PGM);
		writableExtensionToImageFormat.put("PPM", ImageFormat.IMAGE_FORMAT_PPM);
		writableExtensionToImageFormat.put("PNM", ImageFormat.IMAGE_FORMAT_PNM);
		writableExtensionToImageFormat.put("TGA", ImageFormat.IMAGE_FORMAT_TGA);
		writableExtensionToImageFormat.put("JBig2", ImageFormat.IMAGE_FORMAT_JBIG2);
	}

	public static BufferedImage load(String inputFile, Hashtable<String, Object> params) throws ImageReadException, IOException {
		File inImageFile = new File(inputFile);
		return Sanselan.getBufferedImage(inImageFile, params);
	}

	public static void convert(BufferedImage image, String outputFile, Hashtable<String, Object> params) throws ImageWriteException, IOException,
			ImageReadException {
		int lastDotPosition = outputFile.lastIndexOf(".");
		String extension = outputFile.substring(lastDotPosition + 1);
		extension = extension.toUpperCase();
		ImageFormat outFormat = writableExtensionToImageFormat.get(extension);
		if (outFormat != null) {
			File outImageFile = new File(outputFile);
			Sanselan.writeImage(image, outImageFile, outFormat, params);
		} else {
			throw new ImageWriteException("Can't write to the provided output file format (\"" + extension.toLowerCase() + "\")");
		}
	}
}
