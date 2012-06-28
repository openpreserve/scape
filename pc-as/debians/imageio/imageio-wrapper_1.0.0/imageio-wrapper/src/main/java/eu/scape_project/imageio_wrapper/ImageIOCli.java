package eu.scape_project.imageio_wrapper;

import java.awt.image.BufferedImage;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 * Hello world!
 * 
 */
public class ImageIOCli {
	public static void main(String[] args) {
		try {
			Options options = new Options();
			options.addOption("i", "input-file", true, "input file");
			options.addOption("o", "output-file", true, "output file");

			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("i") && cmd.hasOption("o")) {
				BufferedImage inImage = ImageIOWrapper.load(
						cmd.getOptionValue("i"), null);
				if (inImage != null) {
					ImageIOWrapper.convert(inImage, cmd.getOptionValue("o"),
							null);
				} else {
					System.out.println("Unsuported input image file format!");
					System.exit(1);
				}
			} else {
				System.out
						.println("usage: (-i|--input-file=) INPUT_FILE (-o|--output-file=) OUTPUT_FILE");
				System.exit(1);
			}
		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		}
		System.exit(0);
	}
}
