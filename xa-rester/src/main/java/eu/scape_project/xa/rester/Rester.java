/**
 * 
 */
package eu.scape_project.xa.rester;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/")
public class Rester {
	
	@Context UriInfo uriInfo;

	
	@GET
	@Path("/tool")
    @Produces("text/plain")
	public String getTools() {
		return "list of tools";
	}
	
	@GET
	@Path("/tool/{id}")
    @Produces("text/plain")
    public String getActions(
    		@PathParam("id") String id
    		) {
		return "list actions for tool "+id;
	}

	// http://wikis.sun.com/display/Jersey/Overview+of+JAX-RS+1.0+Features
	
	// Act: If query parameters do not match tool action, then returns description.
	@GET
	@Path("/tool/{id}/{act}")
    @Produces("text/plain")
    public String doAction(
    		@PathParam("id") String id,
    		@PathParam("act") String act
    		) {
		StringBuilder buf = new StringBuilder();
		buf.append("id:"+id+", act:"+act+"\n");
		for (String param: uriInfo.getQueryParameters().keySet()) {  
		  buf.append(param);
		  buf.append(" = "+uriInfo.getQueryParameters().get(param));
		  buf.append("\n");  
		}  
		return buf.toString();
	}
	
	@GET
	@Path("/test")
	@Produces("text/plain")
    public String doTest() {
		try {
		StringBuilder buf = new StringBuilder();
		for (String src: uriInfo.getQueryParameters().get("src")) {
		  buf.append(src);
		  buf.append(" = "+getFileInfo(URI.create(src)));
		  buf.append("\n");  
		}  
		return buf.toString();
		} catch( Exception e ) {
			throw new WebApplicationException(e);
		}
	}
	
	private String getFileInfo(URI src) throws MalformedURLException, IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("file", "-Ib", "-");
		pb.redirectErrorStream(true);
		System.out.println("Command: "+pb.command());
		Process p = pb.start();
		//copy input stream to output stream
		IOUtils.copyLarge(src.toURL().openStream() ,  p.getOutputStream() );
		p.getOutputStream().close();
		System.out.println("Data in...");
		Thread.currentThread();
		//System.out.println("Wait: "+p.waitFor());
		//Thread.sleep(2000);
		StringWriter sw = new StringWriter();
		//copy process STDOUT input stream into the string.
		InputStream is = p.getInputStream();
		IOUtils.copy(is, sw );
		//byte[] tmp = new byte[is.available()];
		//p.getInputStream().read(tmp);
		//System.out.println("Out: "+tmp.length);
		//System.out.println("Out: "+new String(tmp));
		//sw.append( new String(tmp));
		return sw.toString();
	}

	static MimetypesFileTypeMap map = null;
	static {
		map = new MimetypesFileTypeMap();
		map.addMimeTypes("image/png png");
	}

	@GET
	@Path("/test2")
    public Response doTest2() {
		try {
			String src = uriInfo.getQueryParameters().get("src").get(0);
			String fmt = uriInfo.getQueryParameters().get("fmt").get(0);
		InputStream is = this.getJPEG(URI.create(src), fmt);
		String mt = map.getContentType("temp."+fmt);
		return Response.ok(is,mt).build();
		} catch( Exception e ) {
			throw new WebApplicationException(e);
		}
	}

	
	
	private InputStream getJPEG(URI src, String fmt) throws MalformedURLException, IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("convert", "-", fmt+":-");
		pb.redirectErrorStream(true);
		System.out.println("Command: "+pb.command());
		Process p = pb.start();
		//copy input stream to output stream
		IOUtils.copyLarge(src.toURL().openStream() ,  p.getOutputStream() );
		p.getOutputStream().close();
		return p.getInputStream();
	}
	
	

//	@Path("/tool/{id : ([^/]+)?}/{op : ([^/]+)?}")

/*
    @POST
    @Consumes("application/xml")
    public void setMessage(MessageDTO messageDTO) {
        System.out.println("Storing message: " + messageDTO.content);
    }
    */
}