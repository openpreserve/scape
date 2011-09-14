package eu.scape_project.pt.util;

import java.net.MalformedURLException;
import java.net.URL;

/*
 * A parser for specific records that are passed to the map() function
 * the format is [in_file_URL out_file_URL] 
 * @author Rainer Schmidt [rschmidt13]
 */
public class FNRecordParser {
	
	private String inFile = null;
	private String outFile = null;
	
	public FNRecordParser(String record) {
		inFile = record.trim().substring(0, record.indexOf(' '));
		outFile = record.trim().substring(record.indexOf(' '), record.length());
	}
	
	public String getProtocol(String url) throws MalformedURLException {
		return (new URL(url)).getProtocol();
	}
		
	public boolean isHDFS(String url) throws MalformedURLException {
		return this.getProtocol(url).toLowerCase().equals("hdfs");
	}
	
	public boolean isFILE(String url) throws MalformedURLException {
		return this.getProtocol(url).toLowerCase().equals("file");
	}

	public String getInFile() {
		return inFile;
	}

	public void setInFile(String inFile) {
		this.inFile = inFile;
	}

	public String getOutFile() {
		return outFile;
	}

	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}

}
