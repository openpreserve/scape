package eu.scape_project.pt.util.fs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class PtFileUtil {
	
	static String HDFS = "hdfs";
	static String FILE = "file";
	
	public static String getScheme(String uri) throws URISyntaxException {
		String scheme = (new URI(uri)).getScheme();
		
		if(scheme != null)
			return scheme.toLowerCase();
		
		return null;
	}
	
	public static boolean isHdfsUri(String uri) throws URISyntaxException {
		String scheme = (new URI(uri)).getScheme();
		
		if(scheme != null)
			return scheme.toLowerCase().equals(HDFS);
		
		return false;
	}
	
	public static boolean isFileUri(String uri) throws URISyntaxException {
		String scheme = (new URI(uri)).getScheme();
		
		if(scheme != null)
			return scheme.toLowerCase().equals(FILE);
		
		return false;
	}
	
	public static File getExecDir() {
		String t = System.getProperty("java.io.tmpdir");
		File tmpDir = new File(t);
		if(!tmpDir.exists()) {
			tmpDir.mkdir();
		}
		return tmpDir;
	}		
}
