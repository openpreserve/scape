/**
 * 
 */
package eu.scape_project.pc.cc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author  <a href="mailto:carl.wilson.bl@gmail.com">Carl Wilson</a>
 *          <a href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl AT SourceForge</a>
 *          <a href="https://github.com/carlwilson-bl">carlwilson-bl AT github</a>
 * @version 0.1
 * 
 * Created Jan 16, 2012:4:19:36 PM
 */

public class JpylyzerWrapper extends DefaultHandler {
	// Be warned, this is MY default location for the Jpylyzer
	private static final String DEFAULT_JPYLYZER_PATH = "C:/bin/jpylyzer/jpylyzer.exe";
	private static final SAXParserFactory SPF = SAXParserFactory.newInstance();
	private static String VERSION = "";
	
	private File fileToJpylyze;
	private SAXParser parser;

	private String jpylyzerPath = DEFAULT_JPYLYZER_PATH;
	private String currentElementVal;
	
	private int width = 0;
	private int height = 0;
	private double horizontalGridResNumerator = 0;
	private double horizontalGridResDenominator = 0;
	private double verticalGridResNumerator = 0;
	private double verticalGridResDenominator = 0;
	private int numImageComponents = 0;
	private int bitsPerComponent = 0;
	private String compressionType = "";
	private String colourSpaceName = "";
	private double compressionRatio = 0.0;
	private boolean valid = false;

	// Private default constructor, to be called by getInstance()
	private JpylyzerWrapper(){this(DEFAULT_JPYLYZER_PATH);}

	// Private String path constructor, called by getInstance(String), and the default constructor
	private JpylyzerWrapper(String pathToJpylyzer) {
		// Check that the Jpylyzer executable exists, and get version
		try {
			this.parser = SPF.newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException("Couldn't initialise the SAX Parser", e);
		} catch (SAXException e) {
			throw new IllegalStateException("Couldn't initialise the SAX Parser", e);
		}
		// Get the version, this will throw an IllegalStateExeception
		VERSION = JpylyzerWrapper.getJpylyzerVersion(pathToJpylyzer);
		// Set the internal path variable
		this.jpylyzerPath = pathToJpylyzer;
	}
	
	/**
	 * @param path the path to a file to run the Jpylyzer is to be run.
	 * @return true if the file found at the path location is a valid JPEG 2K
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException 
	 */
	public boolean validateJp2kFile(String path) throws ParserConfigurationException, SAXException, IOException {
		// Create a file from the path
		File jp2kFile = new File(path);
		
		// Call the validate method on the file
		return this.validateJp2kFile(jp2kFile);
	}
	
	/**
	 * @param jp2kFile
	 * @return true if the JP2K is valid, false otherwise
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public boolean validateJp2kFile(File jp2kFile) throws ParserConfigurationException, SAXException, IOException {
		// Null arg check
		if (jp2kFile == null) throw new IllegalArgumentException("File argument cannot be null");
		
		// Check that the file exists and is a file
		if (!jp2kFile.exists() || !jp2kFile.isFile()) {
			throw new FileNotFoundException("Couldn't find file " + jp2kFile.getAbsolutePath());
		}
		
		// Jpylyze the file
		this.fileToJpylyze = jp2kFile;
		this.runJpylyzer();
		return this.isValid();
	}
	/**
	 * Get the horizontal (x) size of the image
	 * 
	 * @return the horizontal size of the image in pixels
	 */
	public int getHorizontalSize() {return this.width;}

	/**
	 * Get the vertical (y) size of the image
	 * 
	 * @return the vertical size of the image in pixels
	 */
	public int getVerticalSize() {return this.height;}

	/**
	 * @return the horizontal capture resolution in dots per inch
	 */
	public double getHorizontalResolution() {
		return Math.round(this.horizontalGridResNumerator * 2540 / this.horizontalGridResDenominator);
	}
	
	/**
	 * @return the vertical capture resolution in dots per inch
	 */
	public double getVerticalResolution() {
		return Math.round(this.verticalGridResNumerator * 2540 / this.verticalGridResDenominator);
	}
	
	/**
	 * Is the image a valid JP2K?
	 * 
	 * @return true if a valid JP2K, false otherwise.
	 */
	public boolean isValid() {return this.valid;}
	
	/**
	 * @return the number of image components for the JP2K
	 */
	public int getNumberOfImageComponents() {return this.numImageComponents;}
	
	/**
	 * @return the number of bits per component for the JP2K
	 */
	public int getBitsPerComponent() {return this.bitsPerComponent;}
	
	/**
	 * @return the detected compression type for the JP2K
	 */
	public String getCompressionType() {return this.compressionType;}
	
	/**
	 * @return the name of the colour space for the JP2K
	 */
	public String getColourSpaceName() {return this.colourSpaceName;}
	
	/**
	 * @return the compression ratio for the JP2K
	 */
	public double getCompressionRatio() {return this.compressionRatio;}
	
	/**
	 * @return the version string for Jpylyzer, this is a date ATM
	 */
	public String getJpylyzerVersion() {return JpylyzerWrapper.VERSION;}

	@Override
	public String toString() {
		return "width:" + this.width + "\nheight:" + this.height + "\nCR:" + this.compressionRatio + "\nisValid:" + this.isValid() +
				"\nhorizontal resolution:" + this.getHorizontalResolution() + "\nvertical resolution:" + this.getVerticalResolution() +
				"\nnumber of components:" + this.numImageComponents + "\nbits per component:" +  this.bitsPerComponent + 
				"\ncompression type:" + this.compressionType + "\ncolour space:" + this.colourSpaceName;
	}
	
	/**
	 * Get the properties as a String array for quick writing to CSV file.
	 * @return the measured properties of the JP2k as an array of Strings
	 */
	public String[] getElementsAsStringArray() {
		return new String[] {this.fileToJpylyze.getName(), String.valueOf(this.isValid()), String.valueOf(this.width), String.valueOf(this.height),
				String.valueOf(this.getHorizontalResolution()), String.valueOf(this.getVerticalResolution()), 
				this.getCompressionType(), this.getColourSpaceName(), String.valueOf(this.getNumberOfImageComponents()),
				String.valueOf(this.getBitsPerComponent()), String.valueOf(this.compressionRatio)};
	}
	
	/**
	 * @return a new instance of the Jpylyzer wrapper using the "default" binary location
	 */
	public static JpylyzerWrapper getInstance() {return new JpylyzerWrapper();}

	/**
	 * @param pathToJpylyzer a string path to the jpylyzer executable
	 * @return a new instance of the JpylyzerWrapper running the executable at the path passed.
	 */
	public static JpylyzerWrapper getInstance(String pathToJpylyzer) {return new JpylyzerWrapper(pathToJpylyzer);}

	/**
	 * Routine to check that the Jpylyzer executable exists at a path location and obtain the version (a date stamp)
	 * @param path the string path to the Jpylyzer executable.
	 * @return the version date stamp for the executable
	 * @throws IllegalStateException if the Jpylyzer executable cannot be found.
	 */
	public static String getJpylyzerVersion(String path) throws IllegalStateException {
		String version = null;
		// Check that the file exists
		File jpylyzerExecutable = new File(path);
		
		if (!jpylyzerExecutable.exists() || !jpylyzerExecutable.isFile()) {
			throw new IllegalStateException("Path to the Jpylyzer executable: " + path + " should be a to an existing file.");
		}

		String[] command = {path, "-v"};

		try {
			Process pr = Runtime.getRuntime().exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			version = reader.readLine();
		} catch (IOException excep) {
			// OK problem getting version, throw Illegal State
			throw new IllegalStateException("Couldn't run Jpylyzer executable: " + path);
		}
		return version;
	}

	/**
	 * This is a simple test, note that Jpylyzer MUST be at DEFAULT_JPYLYZER_PATH for this
	 * main to work, it doesn't take a path to the exe as an arg.
	 * 
	 * @param args a list of paths to JP2Ks to validate
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) {
		JpylyzerWrapper jpylyzer = new JpylyzerWrapper();
		for (String jp2kpath : args) {
			try {
				jpylyzer.validateJp2kFile(new File(jp2kpath));
				System.out.println(jp2kpath);
				System.out.println(jpylyzer.toString());
			} catch (Throwable excep) {
				// Weak catch all but this is just a test main.
				excep.printStackTrace();
			}
		}
	}
	
	private void runJpylyzer() throws ParserConfigurationException, SAXException, IOException {
		// Put together the command
		String[] command = {jpylyzerPath, this.fileToJpylyze.getAbsolutePath()};

		try {
			// Run the command
			Process pr = Runtime.getRuntime().exec(command);
			// and sax parse the command stream (the jpylyzer output).
			parser.parse(pr.getInputStream(), this);
		} catch (IOException excep) {
			// OK IO error getting process output
			System.err.println("Jpylyzer problem for file: " + this.fileToJpylyze.getAbsolutePath());
			excep.printStackTrace();
			throw excep;
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		// Just reset the current element var.
		this.currentElementVal = "";
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
		// Horrible SAX parsing else if, THIS is where new element parsing code would be inserted
		if (qName.equals("height")) {
			this.height = Integer.valueOf(this.currentElementVal.trim());
		} else if (qName.equals("width")) {
			this.width = Integer.valueOf(this.currentElementVal.trim());
		} else if (qName.equals("isValidJP2")) {
			this.valid = Boolean.valueOf(this.currentElementVal.trim());
		} else if (qName.equals("compressionRatio")) {
			this.compressionRatio = Double.valueOf(this.currentElementVal.trim());
		} else if (qName.equals("nC")) {
			this.numImageComponents = Integer.valueOf(this.currentElementVal.trim());
		} else if (qName.equals("bPCDepth")) {
			this.bitsPerComponent = Integer.valueOf(this.currentElementVal.trim());
		} else if (qName.equals("c")) {
			this.compressionType = this.currentElementVal.trim();
		} else if (qName.equals("enumCS")) {
			this.colourSpaceName = this.currentElementVal.trim();
		} else if (qName.equals("vRcN")) {
			this.verticalGridResNumerator = Integer.valueOf(this.currentElementVal.trim());
		} else if (qName.equals("vRcD")) {
			this.verticalGridResDenominator = Integer.valueOf(this.currentElementVal.trim());
		} else if (qName.equals("hRcN")) {
			this.horizontalGridResNumerator = Integer.valueOf(this.currentElementVal.trim());
		} else if (qName.equals("hRcD")) {
			this.horizontalGridResDenominator = Integer.valueOf(this.currentElementVal.trim());
		}
	}
	
	// Save the current element value
	@Override
	public void characters(char[] ch, int start, int length) {
		this.currentElementVal += new String(ch, start, length); 
	}
}
