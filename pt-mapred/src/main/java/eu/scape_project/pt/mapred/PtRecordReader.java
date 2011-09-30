package eu.scape_project.pt.mapred;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;

public class PtRecordReader extends LineRecordReader {

	public static char COMMENT_CHARACTER = '#';
	
	@Override
	public LongWritable getCurrentKey() {
		checkForComments();
		return super.getCurrentKey();
	}

	@Override
	public Text getCurrentValue() {
		checkForComments();
		return super.getCurrentValue();
	}

	@Override
	public float getProgress() {
		checkForComments();
		return super.getProgress();
	}

	protected void checkForComments() {
		if(getCurrentValue().toString().startsWith(new Character(COMMENT_CHARACTER).toString()))
			try {
				super.nextKeyValue();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
	}
}
