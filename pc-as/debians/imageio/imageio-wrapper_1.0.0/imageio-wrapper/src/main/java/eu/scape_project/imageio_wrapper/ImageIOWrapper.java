package eu.scape_project.imageio_wrapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;

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
