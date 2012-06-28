package eu.scape_project.tb.lsdr.seqfileutility.hadoop;

import eu.scape_project.tb.lsdr.seqfileutility.util.FileUtils;
import java.io.File;
import java.io.IOException;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * The map class of the sequence file creation.
 *
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public class SmallFilesSequenceFileMapper
        extends Mapper<Object, Text, Text, BytesWritable> {

    /**
     * Map implementation assigns the absolute path to the file as key and the
     * file content as value of the sequence file.
     *
     * @param key Key
     * @param value Value
     * @param context Context
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void map(Object key, Text value, Mapper.Context context)
            throws IOException, InterruptedException {
        Text outkey = new Text();
        BytesWritable outvalue = new BytesWritable();
        String keyPath = value.toString();
        File keyFile = new File(keyPath);
        if (keyFile.exists() && keyFile.canRead()) {
            outkey.set(keyPath);
            byte[] buffer;
            long length = 0;
            length = keyFile.length();
            buffer = FileUtils.readFileToByteArray(keyFile.getAbsolutePath());
            if (buffer != null) {
                outvalue.set(buffer, 0, (int) length);
                context.write(outkey, outvalue);
            } else {
                throw new IOException("Unable to read file in buffer!");
            }
        }
    }
}