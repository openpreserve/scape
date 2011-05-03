/**
 * 
 */
package eu.scape_project.pc.qa.bitwiser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Brute force bitwise analysis of transformations and analysers.
 * 
 * Compressed images appear to be rather good for spotting damage. 
 * Need to align with Volker's work. 
 * e.g. Small but silent damage is worse that major damage that is easy to spot.
 * Byte-wise versus bit-wise analysis modes? 
 * Byte-flips easily produce nonsense values (e.g. negative integers).
 * Bit-flips are more thorough and produces more accurate results.
 * Compare TIFFs, compressed, commented, etc. 
 * Collect massive data-map showing exit codes etc for each data point?
 * Of course, this is also testing the tools ability to cope with difficult input.
 * 
 * Metrics, format robustness and tool coverage.
 *  - Sensitivity: Fraction of bit-flips that raise warnings. 
 *  - Robustness: Fraction of bit-flips raise warnings are are repaired accurately/wrongly.
 *  - Coverage: Fraction of bit-flips that change the resulting file (including no-output).
 *  - Transmission: Bitwise difference in output file compared to the input file. ???
 *  - Fuzzing: Fraction of bit-flips that cause the process to flip out/hang/asplode.
 * 
 * 
 * @author anj
 *
 */
public class BitwiseAnalyser {
    
    enum PROCESS { 
        CLEAN,
        WARNING,
        ERROR
    }
    enum OUTPUT {
        SAME,
        DIFFERENT,
        NONE
    }

    public static void main(String [] args) throws IOException {
        File sourceFile = new File("/home/anj/Desktop/jp2-tests/32x32-lzw.tif");
        File tempFile = new File("/home/anj/Desktop/jp2-tests/32x32.tmp.tif");
        File outputFile = new File("/home/anj/Desktop/jp2-tests/32x32.tmp.jp2");
        copy(sourceFile,tempFile);
        
        // Entropy Calc:
        Entropy ent = new Entropy();
        System.out.println("Starting entropy calc...");
        ent.calculate(tempFile, false, false, false, false);
        System.exit(0);
        
        // Start munging...
        System.out.println("Start bit-flipping...");
        RandomAccessFile rf = new RandomAccessFile(tempFile, "rws");
        String truth = runCommand(tempFile, outputFile);
        System.out.println("Truth is "+truth);
        String result = "";
        int clears = 0;
        int errors = 0;
        int warnings = 0;
        for( long pos = 0; pos < tempFile.length(); pos++ ) {
            flipByteAt(rf,pos);
            result = runCommand(tempFile, outputFile);
            if( ! result.equals(truth) )
                System.out.println("Flipped byte "+pos+" : "+result);
            if( result.equals(truth) ) {
                clears++;
            }
            flipByteAt(rf,pos);
        }
        System.out.println("Clears: "+clears+"/"+tempFile.length());
    }
    
    static void flipByteAt(RandomAccessFile rf, long pos ) throws IOException {
        rf.seek(pos);
        byte b = rf.readByte();
        b = (byte) (b ^ 0xff);
        rf.seek(pos);
        rf.write(b);
    }
    
    static String runCommand( File tempFile, File outputFile ) throws IOException {
//        String[] commands = new String[]{"file", tempFile.getAbsolutePath() };
        outputFile.delete();
        String[] commands = new String[]{"convert", tempFile.getAbsolutePath(), outputFile.getAbsolutePath() };
                
        ProcessBuilder pb = new ProcessBuilder(commands);
        // Do this?
        pb.redirectErrorStream(true);
        Process child = pb.start();
        try {
            child.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        String stdout = convertStreamToString(child.getInputStream()).trim();
        String stderr = convertStreamToString(child.getErrorStream()).trim();
        int exitCode = child.exitValue();
        boolean exists = outputFile.exists();
        
        if( exists ) {
            if(exitCode != 0 || stdout.length() > 1 || stderr.length() > 1) return "WARNING:"+exitCode+":"+stdout+"::"+stderr;
            return "CLEAR";
        } else {
            return "ERROR:"+exitCode+":"+stdout+"::"+stderr;
        }
    }
    
    static void copy(File src, File dst) throws IOException {
     InputStream in = new FileInputStream(src);
     OutputStream out = new FileOutputStream(dst);

     // Transfer bytes from in to out
     byte[] buf = new byte[1024];
     int len;
     while ((len = in.read(buf)) >= 0) {
         if( len > 0 ) out.write(buf, 0, len);
     }
     in.close();
     out.flush();
     out.close();
    }
    
    public static String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {        
            return "";
        }
    }
}
