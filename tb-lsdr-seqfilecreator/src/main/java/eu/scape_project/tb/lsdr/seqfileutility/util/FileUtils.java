/*
 *Copyright 2012 The SCAPE Project Consortium.
 * 
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 *under the License.
 */
package eu.scape_project.tb.lsdr.seqfileutility.util;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File utilities.
 * 
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public class FileUtils {
    
    // Logger instance
    private static Logger logger = LoggerFactory.getLogger(FileUtils.class.getName());

    public static final String JAVA_TMP = System.getProperty("java.io.tmpdir");
    private static final String TMP_DIR = "tmp-store";
    
    /**
     *Reads a file storing intermediate data into an array.
     *@param file the file to be read
     *@return a file data
     */
    public static byte[] readFileToByteArray(String file) {
        InputStream in = null;
        byte[] buf = null; // output buffer
        int bufLen = 20 * 1024 * 1024;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            buf = new byte[bufLen];
            byte[] tmp = null;
            int len = 0;
            //List data = new ArrayList(24);
            while ((len = in.read(buf, 0, bufLen)) != -1) {
                tmp = new byte[len];
                System.arraycopy(buf, 0, tmp, 0, len); 
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception _) {
                }
            }
        } catch (IOException e) {
            logger.error("IOException occurred", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception _) {
                }
            }
        }
        return buf;
    }
    
    /**
     * Get system's JAVA temporary directory.
     * @return System's JAVA temporary directory
     */
    public static File getSystemTempFolder() {
        return new File(JAVA_TMP);
    }

    /**
     * Get temporary file
     * @param name The name to use when generating the temp file
     * @param suffix The suffix for the temp file to be created
     * @return Returns a temp file created in the System-Temp folder
     */
    public static File getTmpFile(final String name, final String suffix) {
        String suffixToUse = suffix == null ? ".tmp" : suffix;
        if (!suffixToUse.startsWith(".")) {
            suffixToUse = "." + suffix;
        }
        String nameToUse = name == null ? "tmp" : name;
        File input = null;
        try {
            File folder = new File(JAVA_TMP, FileUtils.TMP_DIR);
            if (!folder.exists()) {
                boolean mkdirs = folder.mkdirs();
                checkCreation(folder, mkdirs);
            }
            input = File.createTempFile(nameToUse, suffixToUse, folder);
            input.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    /**
     * Get temporary file
     * @param data - the data to write to that file
     * @param name - the file name of the file to be created
     * @param suffix - the suffx of that file (e.g. ".tmp", ".bin", ...)
     * @return - a new File with the given content (--> data), name and
     *         extension.
     */
    public static File getTmpFile(final byte[] data, final String name,
            final String suffix) {
        String suffixCopy = suffix;
        if (!suffixCopy.startsWith(".")) {
            suffixCopy = "." + suffix;
        }
        File input = getTmpFile(name, suffixCopy);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(input);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(fos);
        }
        return input;
    }

    /**
     * @param folder The file we tried to create
     * @param mkdirs The result of creating the file
     */
    private static void checkCreation(final File folder, final boolean mkdirs) {
        logger.info(String.format("Created folder '%s': %s", folder, mkdirs));
        if (!folder.exists()) {
            throw new IllegalArgumentException(String.format(
                    "Could not create '%s'", folder));
        }
    }

    /**
     * This method deletes all the content in a folder, without the need of
     * passing it a PlanetsLogger instance!
     * @param workFolder the folder you wish to delete. All contained folders
     *        will be deleted recursively
     * @return true, if all folders were deleted and false, if not.
     */
    public static boolean deleteTempFiles(final File workFolder) {
        if (workFolder.isDirectory()) {
            File[] entries = workFolder.listFiles();
            for (int i = 0; i < entries.length; i++) {
                File current = entries[i];
                boolean deleteTempFiles = deleteTempFiles(current);
                if (!deleteTempFiles) {
                    return false;
                } else {
                    logger.info("Deleted: " + current);
                }
            }
            return workFolder.delete() ? true : false;
        }
        return workFolder.delete() ? true : false;
    }

    /**
     * @param out The closeable (Writer, Stream, etc.) to close
     */
    public static void close(final Closeable out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param file The file to call mkdir on and check for the result
     * @return The result of calling mkdirs on the given file
     * @throws IllegalArgumentException if the creation was not successful and
     *         the file does not already exist
     */
    public static boolean mkdir(final File file) {
        boolean mkdir = file.mkdir();
        handle(mkdir, file);
        return mkdir;
    }

    /**
     * @param file The file to call mkdirs on and check for the result
     * @return The result of calling mkdirs on the given file
     * @throws IllegalArgumentException if the creation was not successful and
     *         the file does not already exist
     */
    public static boolean mkdirs(final File file) {
        boolean mkdirs = file.mkdirs();
        handle(mkdirs, file);
        return mkdirs;
    }

    private static void handle(boolean mkdir, File file) {
        if (!mkdir && !file.exists()) {
            throw new IllegalArgumentException("Could not create " + file);
        }
    }

    public static String makePath(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null) {
                sb.append(part);
                if (!(part.charAt(part.length() - 1) == '/')) {
                    sb.append("/");
                }
            }
        }
        return sb.toString();
    }
    

}
