package eu.scape_project.core;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.scape_project.core.model.JavaDigestValueTests;
import eu.scape_project.core.utils.DigestUtilities;

/**
 * @author <a href="mailto:carl.wilson.bl@gmail.com">Carl Wilson</a>
 *	   <a href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl@SourceForge</a>
 *	   <a href="https://github.com/carlwilson-bl">carlwilson-bl@github</a>
 *
 */
@RunWith(Suite.class)
@SuiteClasses({DigestUtilities.class, JavaDigestValueTests.class})
public class AllCoreTests {
    /** The root resource directory for the test data */
    public static String TEST_DATA_ROOT = "/eu/scape_project/test/data";

    /**
     * @param dirName
     * @return a java.util.List of java.io.File objects from the resource directory
     * @throws URISyntaxException
     */
    public static List<File> getFilesFromResourceDir(String dirName) throws URISyntaxException {
    	// OK get the resource directory as a file from the URL
        File dir = new File(AllCoreTests.class.getResource(dirName).toURI());
        return AllCoreTests.getFilesFromDir(dir);
    }

    private static List<File> getFilesFromDir(File dir) {
        // if it's not a good dir
        if ((dir == null) || (!dir.exists()) || (!dir.isDirectory())) {
        	throw new IllegalArgumentException("Argument dirname:" + dir.getAbsolutePath() + " is not an existing resource directory.");
        }
        // Get it's file children
        File[] files = dir.listFiles();
        ArrayList<File> retVal = new ArrayList<File>();
        if (files != null) {
            // If not null then iterate and add non hidden files
            for (File file : files) {
        	if ((!file.isDirectory()) || (file.isHidden())) {
        	    retVal.add(file);
        	}
            }
        }
    	return retVal;
    }
}
