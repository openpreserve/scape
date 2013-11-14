/* NOTICE
 * This file has been taken from the source code of 
 * FITS (File Information Tool Set) version 0.6
 * the original software license is copied below
 */

/* 
 * Copyright 2009 Harvard University Library
 * 
 * This file is part of FITS (File Information Tool Set).
 * 
 * FITS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FITS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FITS.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.scape_project.pt.tools.fits;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.consolidation.ToolOutputConsolidator;
import edu.harvard.hul.ois.fits.exceptions.FitsConfigurationException;
import edu.harvard.hul.ois.fits.exceptions.FitsException;

import edu.harvard.hul.ois.fits.mapping.FitsXmlMapper;
import edu.harvard.hul.ois.fits.tools.Tool;
import edu.harvard.hul.ois.fits.tools.ToolBelt;
import edu.harvard.hul.ois.fits.tools.ToolOutput;
import edu.harvard.hul.ois.ots.schemas.XmlContent.XmlContent;

public class Fits {
	public static String FITS_HOME;
	public static String FITS_XML;
	public static String FITS_TOOLS;
	public static XMLConfiguration config;
	public static FitsXmlMapper mapper;
	public static boolean validateToolOutput;
	public static String externalOutputSchema;
	public static String internalOutputSchema;
	public static String fitsXmlNamespace;
	
	public static String VERSION = "0.6.0";
	
	private ToolOutputConsolidator consolidator;
	private ToolBelt toolbelt;
	
	private static boolean traverseDirs;
	
	private static Log LOG = LogFactory.getLog(Fits.class);
	
	public Fits() throws FitsException {
		//this(null);
		LOG.info("1");
	}
	
	public Fits(String fits_home) throws FitsConfigurationException {
		LOG.info("2");
		//Set BB_HOME dir with environment variable
		FITS_HOME = System.getenv("FITS_HOME");
		if(FITS_HOME == null) {
			//if env variable not set check for fits_home passed into constructor
			if(fits_home != null) {
				FITS_HOME = fits_home;
			}
			else {
				//if fits_home is still not set use the current directory
				FITS_HOME = "";
			}
		}
		
		//If fits home is not an empty string and doesn't send with a file separator character, add one
		if(FITS_HOME.length() > 0 && !FITS_HOME.endsWith(File.separator)) {
				FITS_HOME = FITS_HOME+File.separator;
		}
		
		FITS_XML = FITS_HOME+"xml"+File.separator;
		FITS_TOOLS = FITS_HOME+"tools"+File.separator;
		
		try {
			config = new XMLConfiguration(FITS_XML+"fits.xml");
		} catch (ConfigurationException e) {
			throw new FitsConfigurationException("Error reading "+FITS_XML+"fits.xml",e);
		}
		try {
			mapper = new FitsXmlMapper();
		} catch (Exception  e) {
			throw new FitsConfigurationException("Error creating FITS XML Mapper",e);
		} 
		validateToolOutput = config.getBoolean("output.validate-tool-output");
		externalOutputSchema   = config.getString("output.external-output-schema");
		internalOutputSchema   = config.getString("output.internal-output-schema");
		fitsXmlNamespace   = config.getString("output.fits-xml-namespace");
		
		String consolidatorClass = config.getString("output.dataConsolidator[@class]");
		try {
			Class<?> c = Class.forName(consolidatorClass);
			consolidator = (ToolOutputConsolidator)c.newInstance();
		}
		catch(Exception e) {
			throw new FitsConfigurationException("Error initializing "+consolidatorClass,e);
		}
		
		toolbelt = new ToolBelt(FITS_XML+"fits.xml");
		LOG.info("3");

	}
	
	//public static void main(String[] args) throws FitsException, IOException, ParseException, XMLStreamException {
	public void doit(String[] args) throws FitsException, IOException, ParseException, XMLStreamException {
		//Fits fits = new Fits();
		LOG.info("4");

		Options options = new Options();
		options.addOption("i",true, "input file or directory");
		options.addOption("r",false,"process directories recursively when -i is a directory ");
		options.addOption("o",true, "output file");
		options.addOption("x",false,"convert FITS output to a standard metadata schema");
		options.addOption("h",false,"print this message");
		options.addOption("v",false,"print version information");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(options, args);
		
		if(cmd.hasOption("h")) {
			//fits.printHelp(options);
			printHelp(options);
			System.exit(0);
		}
		if(cmd.hasOption("v")) {
			System.out.println(Fits.VERSION);
			System.exit(0);
		}
		
		if(cmd.hasOption("r")) {
			traverseDirs = true;
		}
		else {
			traverseDirs = false;
		}
		
		if(cmd.hasOption("i")) {
			String input = cmd.getOptionValue("i");	
			File inputFile = new File(input);
			
			if(inputFile.isDirectory()) {
				String outputDir = cmd.getOptionValue("o");
				if(outputDir == null || !(new File(outputDir).isDirectory())) {
					throw new FitsException("When FITS is run in directory processing mode the output location must be a diretory");
				}
				//fits.doDirectory(inputFile,new File(outputDir),cmd.hasOption("x"));
				LOG.info("5");

				doDirectory(inputFile,new File(outputDir),cmd.hasOption("x"));
			}
			else {
				LOG.info("6");

				//FitsOutput result = fits.doSingleFile(inputFile);
				FitsOutput result = doSingleFile(inputFile);
				//fits.outputResults(result,cmd.getOptionValue("o"),cmd.hasOption("x"),false);
				outputResults(result,cmd.getOptionValue("o"),cmd.hasOption("x"),false);
			}
		}
		else {
			System.err.println("Invalid CLI options");
			//fits.printHelp(options);
			printHelp(options);
			//System.exit(-1);
		}
	    

		LOG.info("7");

		//System.exit(0);
	}
	
	/**
	 * Recursively processes all files in the directory.
	 * @param intputFile
	 * @param useStandardSchemas
	 * @throws IOException 
	 * @throws XMLStreamException 
	 * @throws FitsException 
	 */
	private void doDirectory(File inputDir, File outputDir, boolean useStandardSchemas) throws FitsException, XMLStreamException, IOException {
		for(File f : inputDir.listFiles()) {
			if(f.isDirectory() && traverseDirs) {
				doDirectory(f, outputDir, useStandardSchemas);
			}
			else if(f.isFile()) {
				FitsOutput result = doSingleFile(f);
				String outputFile = outputDir.getPath() + File.separator + f.getName() + ".fits.xml";
				outputResults(result,outputFile,useStandardSchemas,true);
			}
		}
	}
	
	
	/**
	 * processes a single file and outputs to the provided output location. Outputs to 
	 * standard out if outputLocation is null
	 * @param inputFile
	 * @param outputLocation
	 * @param useStandardSchemas - use standard schemas if available for output type
	 * @throws FitsException
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private FitsOutput doSingleFile(File inputFile) throws FitsException, XMLStreamException, IOException {
		
		FitsOutput result = this.examine(inputFile);	
		if(result.getCaughtExceptions().size() > 0) {
			for(Exception e: result.getCaughtExceptions()) {
				System.err.println("Warning: " + e.getMessage());
			}
		}
		return result;
	}
	
	private void outputResults(FitsOutput result, String outputLocation, boolean standardSchema, boolean dirMode) throws XMLStreamException, IOException, FitsException {
		
	    Document doc = result.getFitsXml();

	    //figure out the output location
	    OutputStream out = null;
		if(outputLocation != null) {
			out = new FileOutputStream(outputLocation);
		}
		else if(!dirMode) {		
			out = System.out;
		}
		else {
			throw new FitsException("The output location must be provided when running FITS in directory mode");
		}
		
		//if -x is set, then convert to standard metadata schema and output to -o
		if(standardSchema) {
			outputStandardSchemaXml(result,out);
		}
		//else output FITS XML to -o
		else {
			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
			serializer.output(doc, out);
		}
		out.close();
	}
	
	public static void outputStandardSchemaXml(FitsOutput fitsOutput, OutputStream out) throws XMLStreamException, IOException {
		XmlContent xml = fitsOutput.getStandardXmlContent();
		
		//create an xml output factory
	    XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
	    Transformer transformer = null;
	    
	    //initialize transformer for pretty print xslt
	    TransformerFactory tFactory = TransformerFactory.newInstance ();
	    String prettyPrintXslt = FITS_XML+"prettyprint.xslt";
	    try {
			Templates template = tFactory.newTemplates(new StreamSource(prettyPrintXslt));			
			transformer = template.newTransformer();
	    }
	    catch(Exception e) {
	    	transformer = null;
	    }
	    
		if(xml != null && transformer != null) {

			xml.setRoot(true);	
			ByteArrayOutputStream xmlOutStream = new ByteArrayOutputStream();
			OutputStream xsltOutStream = new ByteArrayOutputStream();
			
			try {
				//send standard xml to the output stream
				XMLStreamWriter sw = xmlOutputFactory.createXMLStreamWriter(xmlOutStream);
				xml.output(sw);
				
				//convert output stream to byte array and read back in as inputstream
				Source source = new StreamSource(new ByteArrayInputStream(xmlOutStream.toByteArray()));
				Result rstream = new StreamResult(xsltOutStream);
				
				//apply the xslt
				transformer.transform(source,rstream);
				
				//send to the providedOutpuStream
				out.write(xsltOutStream.toString().getBytes("UTF-8"));
				out.flush();
				
			} catch (Exception e) {
				System.err.println("error converting output to a standard schema format");
			}
			finally {
				xmlOutStream.close();
				xsltOutStream.close();
			}			
			
		}
		else {
			System.err.println("Error: output cannot be converted to a standard schema format for this file");
		}
	}
	
	private void printHelp(Options opts) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("fits", opts );
	}
	
	/* ORIGINAL EXAMINE METHOD WITHOUT THREADS
	 
	public FitsOutput examineOriginal(File input) throws FitsException {	
		if(!input.exists()) {
			throw new FitsConfigurationException(input+" does not exist or is not readable");
		}
				
		List<ToolOutput> toolResults = new ArrayList<ToolOutput>();
		
		//run file through each tool, catching exceptions thrown by tools
		List<Exception> caughtExceptions = new ArrayList<Exception>();
		String path = input.getPath().toLowerCase();
		String ext = path.substring(path.lastIndexOf(".")+1);
		for(Tool t : toolbelt.getTools()) {			
			if(t.isEnabled()) {			
				if(!t.hasExcludedExtension(ext)) {
					try {
						ToolOutput tOutput = t.extractInfo(input);
						toolResults.add(tOutput);
					}
					catch(Exception e) {
						caughtExceptions.add(e);
					}
				}
			}
		}

		
		// consolidate the results into a single DOM
		FitsOutput result = consolidator.processResults(toolResults);
		result.setCaughtExceptions(caughtExceptions);
		
		for(Tool t: toolbelt.getTools()) {
			t.resetOutput();
		}
		
		return result;	
	}
	*/
	
	public FitsOutput examine(File input) throws FitsException {	
		if(!input.exists()) {
			throw new FitsConfigurationException(input+" does not exist or is not readable");
		}
				
		List<ToolOutput> toolResults = new ArrayList<ToolOutput>();
		
		//run file through each tool, catching exceptions thrown by tools
		List<Exception> caughtExceptions = new ArrayList<Exception>();
		String path = input.getPath().toLowerCase();
		String ext = path.substring(path.lastIndexOf(".")+1);
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		for(Tool t : toolbelt.getTools()) {			
			if(t.isEnabled()) {
				if(!t.hasExcludedExtension(ext)) {
					//spin up new threads
					t.setInputFile(input);
					Thread thread = new Thread(t);
					threads.add(thread);
					thread.start();
				}
			}
		}
		
		//wait for them all to finish
		for(Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//get all output from the tools
		for(Tool t: toolbelt.getTools()) {
			toolResults.add(t.getOutput());
		}
		
		// consolidate the results into a single DOM
		FitsOutput result = consolidator.processResults(toolResults);
		result.setCaughtExceptions(caughtExceptions);
		
		for(Tool t: toolbelt.getTools()) {
			t.resetOutput();
		}
		
		return result;	
	}
	
	public ToolBelt getToolbelt() {
		return toolbelt;
	}

}