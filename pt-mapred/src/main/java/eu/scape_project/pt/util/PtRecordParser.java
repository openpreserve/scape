package eu.scape_project.pt.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * A parser for specific records that are passed to the map() function.
 * A record may define:<br> 
 * * a precondition for the execution<br>
 * * the filenames to use when execution a tool specification<br>
 * * a postcondition after the execution<br>
 * <p>
 * The record format is [-exec replace_these_file_names_in_cmd -pre retrieve_these_files_from_URL - post deposit_these_files_to_URL] 
 * @author Rainer Schmidt [rschmidt13]
 */
public class PtRecordParser {
	
	private static Log LOG = LogFactory.getLog(PtRecordParser.class);

	public static String EXEC_CONDITION = "-exec";
	public static String PRE_CONDITION = "-pre";
	public static String POST_CONDITION = "-post";
	
	private String inFiles[] = null;
	private String outFiles[] = null;
	private String cmdArguments[] = null;
	
	public PtRecordParser(String record) {
    	
    	String recs[] = record.trim().split(" ");
    	List<String> lrecs = Arrays.asList(recs);
    	
    	Integer exec = new Integer(lrecs.indexOf(PtRecordParser.EXEC_CONDITION));
    	Integer pre = new Integer(lrecs.indexOf(PtRecordParser.PRE_CONDITION));
    	Integer post = new Integer(lrecs.indexOf(PtRecordParser.POST_CONDITION));
    	Integer eol = new Integer(lrecs.size());

    	//generate a list with positions of all tokens, sort it
    	Vector<Integer> vtokens = new Vector<Integer>();
    	vtokens.add(exec);
    	vtokens.add(pre);
    	vtokens.add(post);
    	vtokens.add(eol);
    	Collections.sort(vtokens);
    		        	
    	//collect the values between a particular token and the next one in the list
		if(exec > -1) 
    		cmdArguments= lrecs.subList(exec.intValue()+1, vtokens.get(vtokens.indexOf(exec)+1)).toArray(new String[0]);
    	if(pre > -1) 
    		inFiles = (String[])lrecs.subList(pre+1, vtokens.get(vtokens.indexOf(pre)+1)).toArray(new String[0]);
    	if(post > -1)
    		outFiles = (String[])lrecs.subList(post+1, vtokens.get(vtokens.indexOf(post)+1)).toArray(new String[0]);
    		
    	
    	LOG.info("exec params are: "+Arrays.toString(cmdArguments));
    	LOG.info("pre params are: "+Arrays.toString(inFiles));
    	LOG.info("post params are: "+Arrays.toString(outFiles));
	}
	
	public String[] getInFiles() {
		return inFiles;
	}

	protected void setInFiles(String[] inFiles) {
		this.inFiles = inFiles;
	}

	public String[] getOutFiles() {
		return outFiles;
	}

	protected void setOutFiles(String[] outFiles) {
		this.outFiles = outFiles;
	}
	
	public String[] getCmdArguments() {
		return cmdArguments;
	}
	
	protected void setCmdFiles(String[] cmdFiles) {
		this.cmdArguments = cmdFiles;
	}
}
