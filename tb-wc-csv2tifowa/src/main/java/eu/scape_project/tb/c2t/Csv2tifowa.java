/*
 *  Copyright 2011 The SCAPE Project Consortium.
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

package eu.scape_project.tb.c2t;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * This tool can be used to pick the relevant output data produced by DROID (a CSV format)
 * and write an output file similar to the output produced by the ONB FuE tool “TIFOWA”.
 * 
 * Convertig first, enables FuE to use the same tool (“MergeTifowaReports”) to create a combined
 * summary output for the DROID and TIKA (“TIFOWA”) approach.
 * 
 */
public class Csv2tifowa 
{

    static HashMap<String, Integer> myCollection = new HashMap<String, Integer>();
    static int countAllGoodItems = 0;
    
    public static void main( String[] args )
    {
        System.out.println( "Passed Arguments:" );
        
        for (String s: args) {
            System.out.println(s);
        }
        
        //Read CSV file and count file TYPEs
        String myCSV = args[0];
        long processingTime = 0;
        try {
            processingTime = createTXTfromCSV(myCSV);
        } catch (Exception ex) {
            Logger.getLogger(Csv2tifowa.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        displayMyTypes(myCollection, countAllGoodItems, processingTime);
        
    }

    private static long createTXTfromCSV(String myCSV) throws Exception {
        System.out.println("************************************");
        System.out.println("Readin CSV file and counting file TYPEs...");
        System.out.println("myCSV: " + myCSV);
        
        //start timer
        long startClock = System.currentTimeMillis();
        
        BufferedReader myFile = new BufferedReader(new FileReader(myCSV));
        
        String dataRow = myFile.readLine();
        
        while (dataRow != null){
           
            String[] dataArray = dataRow.split(",");
            String itemType = dataArray[8].toLowerCase().replace("\"",""); //read the TYPE field
            
            if(itemType.equals("file")) // only process if type is "file" (discard "folder" or the header of the table)
            {
                // Increase overall item counter
                countAllGoodItems++;
                
                String myMIME = dataArray[15].replace("\"",""); //read the MIME_TYPE field
                if(myMIME.equals("")) myMIME = "NO_RESULT"; // MIME_TYPE field was empty (MIME_TYPE not detected)
                   
                // Check for an existing key for the current type. Create it if it is not existing.
                try{int myGetCounter = myCollection.get(myMIME);}
                catch (Exception ex)
                {//System.out.println("1st >" + myType + "< file type. Create NEW key for counter. ");
                myCollection.put(myMIME, 0);}
            
                 // Read the counter for the current type and increase the type counter 
                 myCollection.put(myMIME, myCollection.get(myMIME) + 1);
               
            }
            dataRow = myFile.readLine(); // Read next line
        }
         // Close the file once all data has been read.
        myFile.close();
        
        long elapsedTimeMillis = System.currentTimeMillis()-startClock;
        System.out.println("...done.");
        
        return elapsedTimeMillis;
 
    }
    
    
    private static void displayMyTypes(HashMap<String, Integer> myCollection,int countAllGoodItems, long elapsedTimeSec)
    {
            
        System.out.println("************************************");
        System.out.println("Total file processing time (sec): " + elapsedTimeSec/1000F );
        System.out.println("************************************");
        System.out.println("Total number of files analyzed  : " + countAllGoodItems);
        System.out.println("************************************");
        System.out.println("*** You can import the data below into a CSV. Use # as the separator. ***");
        System.out.println();
        System.out.println("TYPE#COUNT#PERCENTAGE");
        Iterator it = myCollection.keySet().iterator();
        
        while(it.hasNext()){
            String typeKey = it.next().toString();
            float typeValue = myCollection.get(typeKey);
            float myPerc = typeValue/countAllGoodItems*100;
            System.out.println(typeKey + "#" + (int) typeValue + "#" + myPerc);
        }
            
            
    
    }
    
    
    
    
    
    
}
