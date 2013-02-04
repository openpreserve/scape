package eu.scape_project.pt.proc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;

import eu.scape_project.pt.fs.util.Filer;
import eu.scape_project.pt.fs.util.HDFSFiler;
import eu.scape_project.pt.fs.util.PtFileUtil;
import java.util.Map.Entry;
import org.apache.hadoop.conf.Configuration;

public class FileProcessor implements PreProcessor, PostProcessor {

    private static Log LOG = LogFactory.getLog(FileProcessor.class);
    protected HashMap<String, String> inRefs = null;
    protected HashMap<String, String> outRefs = null;
    protected FileSystem hdfs = null;
    private File[] inputFiles = null;
    // A map that maps from all input strings to their corresponding local temp file
    private HashMap<String, String> retrievedFiles = new HashMap<String, String>();
    private HDFSFiler hdfs_filer = null;

    public FileProcessor() {
        createHDFS();
    }

    /**
     *
     * @param inFiles
     * @deprecated
     */
    public FileProcessor(String[] inFiles) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    public FileProcessor(String[] inRefs, String[] outRefs, FileSystem fs) {
        throw new UnsupportedOperationException();
    }

    public FileProcessor(HashMap<String, String> inRefs, HashMap<String, String> outRefs) {
        this.inRefs = inRefs;
        this.outRefs = outRefs;
        createHDFS();
    }

    private void createHDFS() {
        try {
            this.hdfs = FileSystem.get(new Configuration());
        } catch (IOException ex) {
            LOG.error(ex);
        }
    }

    public void setInRefs(HashMap<String, String> inRefs) {
        this.inRefs = inRefs;
    }

    public HashMap<String, String> getLocalInRefs() {
        HashMap<String, String> localInRefs = new HashMap<String, String>();
        for (Entry<String, String> entry : inRefs.entrySet()) {
            localInRefs.put(entry.getKey(), getTempInputLocation(entry.getValue()));
        }
        return localInRefs;
    }

    public void setOutRefs(HashMap<String, String> outRefs) {
        this.outRefs = outRefs;
    }

    /**
     * Returns local file references of output files.
     *
     * @return
     */
    public HashMap<String, String> getLocalOutRefs() {
        HashMap<String, String> localOutRefs = new HashMap<String, String>();
        for (Entry<String, String> entry : outRefs.entrySet()) {
            localOutRefs.put(entry.getKey(), getTempInputLocation(entry.getValue()));
        }
        return localOutRefs;
    }

    public void setHadoopFS(FileSystem hdfs) {
        this.hdfs = hdfs;
    }

    @Override
    /**
     * Copies remote input references to local filesystem.
     * 
     * TODO createInputStreams if files are to be used as streams
     * maybe use In and Out classes
     */
    public void resolvePrecondition() throws IOException, URISyntaxException {
        // Any inRefs?
        if (inRefs == null) {
            LOG.info("no preprocessing required");
            return;
        }

        ArrayList<File> files = new ArrayList<File>();
        for (Entry<String, String> entry : inRefs.entrySet()) {
            String file = entry.getValue();
            if (retrievedFiles != null && retrievedFiles.containsKey(file)) {
                continue;
            }

            LOG.info("trying to retrieve file: " + file);
            Filer filer = getFiler(file);
            if (filer == null) {
                continue;
            }

            File inFile = filer.copyFile(file, getTempInputLocation(file));
            files.add(inFile);

            retrievedFiles.put(file, inFile.getAbsolutePath());

            LOG.info("retrieving file: " + inFile.getName());
        }
        this.inputFiles = files.toArray(new File[0]);

    }

    /**
     * Returns the full path to the location of temporary input file.
     *
     * @param fileRef location of original source (eg. an HDFS-location)
     * @return
     */
    static public String getTempInputLocation(String fileRef) {
        return getTempLocation(fileRef);
    }

    /**
     * Returns the full path to the location of temporary output file.
     *
     * @param fileRef location of original destination (eg. an HDFS-location)
     * @return
     */
    static public String getTempOutputLocation(String fileRef) {
        return getTempLocation(fileRef);
    }

    /**
     * Returns the full path to the location of temporary file.
     *
     * @param fileRef location of original (eg. an HDFS-location)
     * @return
     */
    static public String getTempLocation(String fileRef) {
        String extension = "";
        int pos = fileRef.lastIndexOf(".");
        if (pos >= 0) {
            extension = fileRef.substring(pos);
        }
        return PtFileUtil.getExecDir().toString() + File.separator + "tmp" + fileRef.hashCode() + extension;
    }

    public File[] getInputFiles() {
        return inputFiles;
    }

    public void resolvePostcondition() throws IOException, URISyntaxException {
        // Any outRefs?
        if (outRefs == null) {
            LOG.info("no postprocessing required");
            return;
        }

        for (Entry<String, String> entry : outRefs.entrySet()) {
            String file = entry.getValue();
            LOG.info("trying to deposit file: " + file);
            Filer filer = getFiler(file);
            if (filer == null) {
                continue;
            }
            String outFile = getTempOutputLocation(file);
            filer.depositDirectoryOrFile(outFile, file);
            outRefs.put(entry.getKey(), outFile);
        }
    }

    public Filer getFiler(String file) throws URISyntaxException {
        if (PtFileUtil.isHdfsUri(file)) {
            if (hdfs == null) {
                LOG.error("Cannot create HDFSFiler. Hadoop FileSystem not set!");
                return null;
            }
            if (hdfs_filer == null) {
                return hdfs_filer = new HDFSFiler(hdfs);
            } else {
                return hdfs_filer;
            }
        } else if (PtFileUtil.isFileUri(file)) {
            LOG.error("Cannot create FileFiler. Not implemented!");
            return null;
        } else {
            LOG.warn("Unknown Schema in file: " + file);
            return null;
        }
    }

    public Map<String, String> getRetrievedFiles() {
        return retrievedFiles;
    }
}
