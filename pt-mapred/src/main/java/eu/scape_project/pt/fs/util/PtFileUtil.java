package eu.scape_project.pt.fs.util;

import java.net.URI;
import java.net.URISyntaxException;

public class PtFileUtil {
	
	static String HDFS = "hdfs";
	static String FILE = "file";

	public static String getScheme(String uri) throws URISyntaxException {
		return (new URI(uri)).getScheme().toLowerCase();
	}
	
	public static boolean isHdfsUri(String uri) throws URISyntaxException {
		return getScheme(uri).toLowerCase().equals(HDFS);
	}
	
	public static boolean isFileUri(String uri) throws URISyntaxException {
		return getScheme(uri).toLowerCase().equals(FILE);
	}

}
