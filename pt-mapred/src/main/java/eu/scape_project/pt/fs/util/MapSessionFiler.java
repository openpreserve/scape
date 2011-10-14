package eu.scape_project.pt.fs.util;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.scape_project.pt.mapred.SimpleWrapper;

public class MapSessionFiler {
	
	private static Log LOG = LogFactory.getLog(MapSessionFiler.class);
	
	public MapSessionFiler() {
	}

	public static File getExecDir() {
		String t = System.getProperty("java.io.tmpdir");
		LOG.info("Using Temp. Directory:" + t);
		File tmpDir = new File(t);
		if(!tmpDir.exists()) {
			tmpDir.mkdir();
		}
		return tmpDir;
	}		
}
