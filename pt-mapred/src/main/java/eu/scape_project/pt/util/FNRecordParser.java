package eu.scape_project.pt.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.scape_project.pt.mapred.SimpleWrapper;

/*
 * A parser for specific records that are passed to the map() function
 * the format is [in_file_URL out_file_URL] 
 * @author Rainer Schmidt [rschmidt13]
 */
public class FNRecordParser {
	
	private static Log LOG = LogFactory.getLog(FNRecordParser.class);

	public static String IN_OPT = "-i";
	public static String OUT_OPT = "-o";
	
	private String inFiles[] = null;
	private String outFiles[] = null;

	//inFile = record.trim().substring(0, record.indexOf(' '));
	//outFile = record.trim().substring(record.indexOf(' '), record.length());
	
	public FNRecordParser(String record) {
		int i = -1;
		record = record.trim();
		//any output files?
		if( (i = record.indexOf(OUT_OPT)) > -1 ) {
			outFiles = record.substring(i+OUT_OPT.length(), record.length()).trim().split(Pattern.quote(" "));
			record = record.substring(0, i).trim();
		} else {
			LOG.info("no input file found in map() record");
		}
		//any output files?
		if( (i = record.indexOf(IN_OPT)) > -1) {
			inFiles = record.substring(i+IN_OPT.length(), record.length()).trim().split(Pattern.quote(" "));
		} else {
			LOG.info("no output file found in map() record");
		}
	}
	
	public String getScheme(String uri) throws URISyntaxException {
		return (new URI(uri)).getScheme();
	}
		
	public boolean isHDFS(String uri) throws URISyntaxException {
		return this.getScheme(uri).toLowerCase().equals("hdfs");
	}
	
	public boolean isFILE(String uri) throws URISyntaxException {
		return this.getScheme(uri).toLowerCase().equals("file");
	}

	public String[] getInFiles() {
		return inFiles;
	}

	public void setInFiles(String[] inFiles) {
		this.inFiles = inFiles;
	}

	public String[] getOutFiles() {
		return outFiles;
	}

	public void setOutFiles(String[] outFiles) {
		this.outFiles = outFiles;
	}

}
