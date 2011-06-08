/*
 *  Copyright 2011 PLANAETS (www.planets-project.eu)/
 *  IMPACT (www.impact-project.eu)/SCAPE (www.scape-project.eu)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.opflabs.scape.tb.gw.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.log4j.Logger;

/**
 * These are generic file utilities used by the client and service.
 * @author Thomas Kraemer
 * @author onbscs
 * @version 0.1
 */
public final class FileUtil {

    public static final String JAVA_TMP = System.getProperty("java.io.tmpdir");
    private static final String TMP_DIR = "gwg-tmp-store";
    private static final int BUFF = 32768;
    private static Logger logger = Logger.getLogger(FileUtil.class);

    /**
     * Empty private constructor avoids instantiation.
     */
    private FileUtil() {
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
            File folder = new File(JAVA_TMP, FileUtil.TMP_DIR);
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
     * Get an input stream from a file
     * @param src Source file
     * @return Input stream
     */
    public static InputStream getInputStreamFromFile(File src) {
        BufferedInputStream fileIn = null;
        try {
            fileIn = new BufferedInputStream(new FileInputStream(src));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileIn;
    }

    /**
     * Get an output stream from a file
     * @param dest Destinatory file of the output stream
     * @return Output stream
     */
    public static OutputStream getOutputStreamToFile(File dest) {
        BufferedOutputStream fileOut = null;
        try {
            fileOut = new BufferedOutputStream(new FileOutputStream(dest));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileOut;
    }

    public static boolean copyFileTo(String srcPath, String destPath) {
        File src = new File(srcPath);
        File dest = new File(destPath);
        return copyFileTo(src, dest);
    }

    /**
     * Copy a file from a source to a destination
     * @param src Source file
     * @param dest Destitnation
     * @return Success status of copying the file
     */
    public static boolean copyFileTo(File src, File dest) {
        long destSize = writeInputStreamToOutputStream(getInputStreamFromFile(src),
                getOutputStreamToFile(dest));
        if (destSize == src.length()) {
            return true;
        } else {
            return false;
        }
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
     * Reads the contents of a file into a byte array.
     * @param file The file to read into a byte array
     * @return Returns the contents of the given file as a byte array
     */
    public static byte[] readFileIntoByteArray(final File file) {
        byte[] array = null;
        try {
            BufferedInputStream in = new BufferedInputStream(
                    new FileInputStream(file));
            if (file.length() > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("The file at "
                        + file.getAbsolutePath()
                        + " is too large to be represented as a byte array!");
            }
            array = new byte[(int) file.length()];
            in.read(array);
            in.close();
            return array;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param bytes The data to write
     * @param fileName The desired file name
     * @return The file containing the given data
     */
    public static File writeByteArrayToFile(final byte[] bytes,
            final String fileName) {
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        BufferedOutputStream out;
        try {
            out = new BufferedOutputStream(new FileOutputStream(fileName));
            out.write(bytes);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Writes the contents of a byte array into a temporary file.
     * @param bytes The bytes to write into a temporary file
     * @return Returns the temporary file into which the bytes have been written
     */
    public static File writeByteArrayToTempFile(final byte[] bytes) {
        File file = null;
        try {
            file = getTmpFile("planets", null);
            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(file), BUFF);
            out.write(bytes);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * @param textFile The file to read from
     * @return The file contents as a string
     */
    public static String readTxtFileIntoString(final File textFile) {
        String resultString = null;
        StringBuffer buffer = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(textFile));
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append('\n');
            }
            reader.close();
            resultString = buffer.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultString;
    }

    /**
     * @param content The content to write to destination
     * @param destination The destination to write content to
     * @return file A file at destination with the given content
     */
    public static File writeStringToFile(final String content,
            final String destination) {
        File result = new File(destination);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(result),
                    BUFF);
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param content The content to write to destination
     * @param destination The destination to write content to
     * @return file A file at destination with the given content
     */
    public static File writeStringToFile(final String content,
            final File target) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(target),
                    BUFF);
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return target;
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
     * @param inputStream The stream to write to a byte array
     * @return The byte array created from the stream
     */
    public static byte[] writeInputStreamToBinary(final InputStream inputStream) {
        ByteArrayOutputStream boStream = new ByteArrayOutputStream();
        long size = writeInputStreamToOutputStream(inputStream, boStream);
        try {
            boStream.flush();
            boStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (size > 0) {
            return boStream.toByteArray();
        }
        return null;
    }

    /**
     * Writes an input stream to the specified file.
     * @param stream The stream to write to the file
     * @param target The file to write the stream to
     */
    public static void writeInputStreamToFile(final InputStream stream,
            final File target) {
        BufferedOutputStream bos = null;
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(target);
            bos = new BufferedOutputStream(fileOut);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        long size = writeInputStreamToOutputStream(stream, bos);
        try {
            if (bos != null) {
                bos.flush();
                bos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (true) {
            logger.info("Wrote " + size + " bytes to " + target.getAbsolutePath());
        } else {
            System.out.println("Wrote " + size + " bytes to "
                    + target.getAbsolutePath());
        }
    }

    /**
     * This method writes an InputStream to a file. If a file with the same name
     * and path exists already, a random number is appended to the filename and
     * the "renamed" file is returned.
     * @param in the input stream to write to a file
     * @param parentFolder the folder the file should be created in
     * @param fileName the name of the file to be created
     * @return a File object with the given name and path
     */
    public static File writeInputStreamToFile(final InputStream in,
            final File parentFolder, final String fileName) {
        String name = fileName;
        File target = new File(parentFolder, name);
        if (target.exists()) {
            long randonNr = (long) (Math.random() * 1000000);
            if (fileName.contains(".")) {
                name = name.substring(0, name.lastIndexOf(".")) + "_"
                        + randonNr + name.substring(name.lastIndexOf("."));
            } else {
                name = name + randonNr;
            }
            target = new File(parentFolder, name);
        }
        writeInputStreamToFile(in, target);
        return target;
    }

    /**
     * @param inputStream The stream to write
     * @param fileName The file name to write the stream to
     * @param suffix The suffix to use
     * @return A temp file containing the stream contents
     */
    public static File writeInputStreamToTmpFile(final InputStream inputStream,
            final String fileName, final String suffix) {
        String suffixToUse = suffix;
        if (suffixToUse != null) {
            if (!suffixToUse.startsWith(".")) {
                suffixToUse = "." + suffixToUse;
            }
        }
        File file = getTmpFile(fileName, suffixToUse);
        writeInputStreamToFile(inputStream, file);
        return file;
    }

    /**
     * Writes an input stream to an output stream, using a sane buffer size.
     * @param in The input stream
     * @param out The output stream
     * @return The number of bytes written
     */
    public static long writeInputStreamToOutputStream(final InputStream in,
            final OutputStream out) {
        long size = 0;
        try {
            int dataBit;
            byte[] buf = new byte[BUFF];
            while ((dataBit = in.read(buf)) != -1) {
                out.write(buf, 0, dataBit);
                size += dataBit;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }
        logger.info("Wrote " + size + " bytes.");
        return size;
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

    public static void copyBinaryFile(File sourcePath,File trgtFilePath) {
        try {
            FileInputStream fin = new FileInputStream(sourcePath);
            FileOutputStream fout = new FileOutputStream(trgtFilePath);
            byte[] b = new byte[1024];
            int noOfBytes = 0;
            while ((noOfBytes = fin.read(b)) != -1) {
                fout.write(b, 0, noOfBytes);
            }
            fin.close();
            fout.close();
            logger.debug("Copied file "+sourcePath+" to "+trgtFilePath);
        } catch (FileNotFoundException ex) {
            logger.error("File not found.");
        } catch (IOException ex) {
            logger.error("Error while copying file.");
        }
    }

    /*
     * pattern: pu.getProp("project.template.dir")
     * replace: pu.getProp("project.generate.dir") + "/" + this.projectMidfix
     */
    public static void copyDirectory(File sourcePath, String pattern, String replace) throws IOException {
        if (sourcePath.isDirectory()) {
            String[] children = sourcePath.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourcePath, children[i]),pattern,replace);
            }
        } else {
            if (sourcePath.isFile()) {
                String trgtFilePathStr = sourcePath.getPath().replaceAll(pattern, replace);
                String trgtDirStr = trgtFilePathStr.replaceAll(sourcePath.getName(), "");
                FileUtil.mkdirs(new File(trgtDirStr));
                File trgtFilePath = new File(trgtFilePathStr);
                copyBinaryFile(sourcePath,trgtFilePath);

            }
        }
    }
}
