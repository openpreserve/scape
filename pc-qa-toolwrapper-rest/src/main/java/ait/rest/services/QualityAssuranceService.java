package ait.rest.services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("/qualityassurance")
@Component
@Scope("request")
public class QualityAssuranceService {

	private Logger log = Logger.getLogger(getClass().getName());
	
	/**
	 * This is an output file name. The standard output is redirected to the output file.
	 */
	private final static String OUTPUT_FILE = "output.xml";
	
	/**
	 * This character splits the input parameter array.
	 */
	private final static String ARRAY_SPLITTER = ",";
	
    /**
     * The default method for quality assurance services.
     * @return Usage information
     */
    @GET
	@Produces(MediaType.TEXT_PLAIN)
    public String getMessage() {
        return "==========================================================\n" + 
        	"USAGE: Scape quality assurance provides following services \n" +
        	"==========================================================\n\n\t" +
        	"1a. Extract features. For example: " +
        	"http://localhost:8080/scape-pc-qa-toolwrapper-rest/rest/qualityassurance/extractfeatures/scape-logo.png\n\r\t" +
        	"1b. Extract features with numbins parameter. For example: " +
        	"http://localhost:8080/scape-pc-qa-toolwrapper-rest/rest/qualityassurance/extractfeatures/scape-logo.png/7\n\r\t" +
        	"2. Compare features. For expample: " +
        	"http://localhost:8080/scape-pc-qa-toolwrapper-rest/rest/qualityassurance/compare/h1.xml/h2.xml\n\r\t" +
        	"3. Quality assurance provides also common solution service that supports any QA command line tool" +
        	" using path '/tool' and parameters separated by ','.\n\t" +
        	"For example for QA extract features tool: " +
        	"http://localhost:8080/scape-pc-qa-toolwrapper-rest/rest/qualityassurance/tool/" +
        	"extractfeatures.exe,--numbins%205,scape-logo.png\n\t" +
        	"or for QA comapre tool: " +
        	"http://localhost:8080/scape-pc-qa-toolwrapper-rest/rest/qualityassurance/tool/compare.exe,h1.xml,h2.xml\n\n\n" +
        	"-------------------\n" +
        	"Taverna integration\n" +
        	"-------------------\n\n" +

        	"1. Create RESTfull service using drag & drop feature from " +
        	"'Available services / Service templates / REST service' in service Browser.\n" +
        	"2. Define service URL - for example default service URL: " +
        	"http://localhost:8080/scape-pc-qa-toolwrapper-rest/rest/qualityassurance/\n" + 
        	"3. Specify service name at the end of the URL from (2). " +
        	"Service names are defined in @Path annotation in QualityAssuranceService.java\n" +
        	"4. Provide service request with parameter according to the service syntax defined in @Path annotation. " +
        	"For example service 'compare' has 2 parameters h1.xml and h2.xml: @Path(\"/compare/{path1}/{path2}\")\n" +
        	"http://localhost:8080/scape-pc-qa-toolwrapper-rest/rest/qualityassurance/compare/h1.xml/h2.xml\n" +
        	"5. Define HTTP method (GET for example)\n" +
        	"6. Define 'Accept' header (text/plane for example)\n" +
        	"7. Check that tomcat server is running\n" +
        	"8. Run workflow\n";
    }

    /** 
     * @param filePath the name of the file to open. 
     */ 
     private String readFileAsString(String filePath)
     	throws java.io.IOException{
         StringBuffer fileData = new StringBuffer(1000);
         BufferedReader reader = new BufferedReader(
                 new FileReader(filePath));
         char[] buf = new char[1024];
         int numRead=0;
         while((numRead=reader.read(buf)) != -1){
             String readData = String.valueOf(buf, 0, numRead);
             fileData.append(readData);
             buf = new char[1024];
         }
         reader.close();
         return fileData.toString();
     }
     
  	/**
 	 * This method utilizes QA command line tool extractfeatures.exe that extracts colour histograms and 
 	 * profiles from XML file.
 	 * @param path The path to the XML file that should be analyzed
 	 * @return XML output string
  	 */
  	@GET
	@Path("/extractfeatures/{path}")
	@Produces({ MediaType.TEXT_PLAIN })
	public String extractFeatures(@PathParam("path") String path) {
		String res = "";
		res = path;
		try {
            log.info("file name: " + path);
            String commands = "cmd.exe /C extractfeatures.exe " + path + " > " + OUTPUT_FILE;
            log.info("command: " + commands);
           	Process p = Runtime.getRuntime().exec(commands);
           	p.waitFor(); 
		    res = readFileAsString(OUTPUT_FILE);
		} catch (Exception e) {
			res = "Error: " + e.getMessage();
			log.info(res);
		}			
		return res;
	}

 	/**
 	 * This method utilizes QA command line tool extractfeatures.exe that extracts colour histograms and 
 	 * profiles from XML file.
 	 * @param path The path to the XML file that should be analyzed
 	 * @param numbins The numbins parameter of the tool
 	 * @return XML output string
 	 */
 	@GET
	@Path("/extractfeatures/{path}/{numbins}")
	@Produces({ MediaType.TEXT_PLAIN })
	public String extractFeaturesExt(@PathParam("path") String path, @PathParam("numbins") String numbins) {
		String res = "";
		res = path;
		try {
            log.info("file name: " + path + ", numbins: " + numbins);
            String commands = "cmd.exe /C extractfeatures.exe --numbins " + numbins + " " + path + " > " + OUTPUT_FILE;
            log.info("command: " + commands);
           	Process p = Runtime.getRuntime().exec(commands);
           	p.waitFor(); 
		    res = readFileAsString(OUTPUT_FILE);
		} catch (Exception e) {
			res = "Error: " + e.getMessage();
			log.info(res);
		}			
		return res;
	}

 	/**
 	 * This method utilizes QA command line tool compare.exe and compares two XML files.
 	 * @param path1 The path to the first XML file
 	 * @param path2 The path to the second XML file
 	 * @return XML output string of the tool
 	 */
 	@GET
	@Path("/compare/{path1}/{path2}")
	@Produces({ MediaType.TEXT_XML })
	public String compare(@PathParam("path1") String path1, @PathParam("path2") String path2) {
		String res = "";
		try {
            log.info("first file name: " + path1 + ", second file name: " + path2);
            String commands = "cmd.exe /C compare.exe " + path1 + " " + path2 + " > " + OUTPUT_FILE;
            log.info("command: " + commands);
           	Process p = Runtime.getRuntime().exec(commands);
           	p.waitFor(); 
		    res = readFileAsString(OUTPUT_FILE);
		} catch (Exception e) {
			res = "Error: " + e.getMessage();
			log.info(res);
		}			
		return res;
	}
 	
 	/**
 	 * This method stores resulting data to an output file.
 	 * @param res The output data in String format
 	 */
 	public void createOutputFile(String res)
 	  {
 	  try {
 		  InputStream inputStream = new ByteArrayInputStream(res.getBytes());
 		  File outputFile = new File(OUTPUT_FILE);
 		  OutputStream out=new FileOutputStream(outputFile);
 		  byte buf[]=new byte[1024];
 		  int len;
 		  while((len=inputStream.read(buf))>0)
 			  out.write(buf,0,len);
 		  out.close();
 		  inputStream.close();
 		  log.info("\nOutput file " + OUTPUT_FILE + " is created.");
 	  }
 	  catch (IOException e){
 		  log.info("\nOutput file creation error. " + e);
 	  }
 	}
 	 
 	/**
 	 * This method supports command line tools execution in a RESTful service.
 	 * The command line tool name and parameters passed in a String separated by ','
 	 * character. For example 'extractfeatures.exe,--numbins 5,test.png'.
 	 * The output of this method is an XML string. The output is stored in OUTPUT_FILE
 	 * @param args The command line tool parameters in String format
 	 * @return XML string
 	 */
 	@GET
	@Path("/tool/{args}")
	@Produces({ MediaType.TEXT_PLAIN })
	public String tool(@PathParam("args") String args) {
		String res = "";
		log.info("request parameters: " + args); 
		String[] parameterSet = args.split(Pattern.quote(ARRAY_SPLITTER));
		try {
            Process process = new ProcessBuilder(parameterSet).start();
            InputStream is = process.getInputStream();
            
            if (is != null) {
            	Writer writer = new StringWriter();             
            	char[] buffer = new char[1024];
            	try {
            		Reader reader = new InputStreamReader(is);
            		int n;
            		while ((n = reader.read(buffer)) != -1) {
            			writer.write(buffer, 0, n);
            		}
            	} finally {
            		is.close();
            	}
            	
            	res = writer.toString();
        		if (res != null && res.length() > 0) {
        			createOutputFile(res);
        		}
            } 
		} catch (Exception e) {
			res = "Error: " + e.getMessage();
			log.info(res);
		}			
		return res;
	}

}