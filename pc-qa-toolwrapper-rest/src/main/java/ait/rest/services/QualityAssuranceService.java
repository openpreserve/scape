package ait.rest.services;

import java.io.BufferedReader;
import java.io.FileReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/qualityassurance")
@Component
@Scope("request")
public class QualityAssuranceService {

    @GET
	@Produces(MediaType.TEXT_PLAIN)
    public String getMessage() {
        return "USAGE: Scape quality assurance services provide following services. Extract features. Compare features.";
    }

    /** 
     * @param filePath the name of the file to open. 
     */ 
     private String readFileAsString(String filePath)
     	throws java.io.IOException{
         StringBuffer fileData = new StringBuffer(1000);
         BufferedReader reader = new BufferedReader(
                 new FileReader(filePath));
         char[] buf = new char[1024];
         int numRead=0;
         while((numRead=reader.read(buf)) != -1){
             String readData = String.valueOf(buf, 0, numRead);
             fileData.append(readData);
             buf = new char[1024];
         }
         reader.close();
         return fileData.toString();
     }
     
	@GET
	@Path("/extractfeatures/{path}")
	@Produces({ MediaType.TEXT_PLAIN })
	public String extractFeatures(@PathParam("path") String path) {
		String res = "";
		res = path;
		try {
            System.out.println("file name: " + path);
            String commands = "cmd.exe /C extractfeatures.exe " + path + " > log.txt";
            System.out.println("command: " + commands);
           	Process p = Runtime.getRuntime().exec(commands);
           	p.waitFor(); 
		    Process child = Runtime.getRuntime().exec(commands);
		    System.out.println(child.toString());
		    res = readFileAsString("log.txt");
		} catch (Exception e) {
			res = "Error: " + e.getMessage();
			System.out.println(res);
		}			
		return res;
	}
}