package eu.scape_project.pt.fs.util;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.scape_project.pt.mapred.SimpleWrapper;

public class MapSessionFiler {
	
	private static Log LOG = LogFactory.getLog(MapSessionFiler.class);
	
	protected File tempDir = null;
	
	public MapSessionFiler() {
		String t = System.getProperty("java.io.tmpdir");
		LOG.debug("Using Temp. Directory:" + t);
		tempDir = new File(t);
		if(!tempDir.exists()) {
			tempDir.mkdir();
		}
	}		

}
