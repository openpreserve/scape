package eu.scape_project.pt.fs.util;

import java.io.File;
import java.io.IOException;

public interface Filer {
	
	public File createTempFileFromReference(String ref) throws IOException;

	
	public void depositTempDirectoryOrFile(String hdfsRef) throws IOException;
	
	public void depositTempDirectory(String hdfsRef) throws IOException;
	
	public void depositTempFile(String ref) throws IOException;

}
