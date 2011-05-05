/**
 * 
 */
package eu.scape_project.xa.ws;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Vector;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.soap.MTOM;

import com.sun.xml.ws.developer.StreamingAttachment;

import eu.planets_project.services.identify.IdentifyResult;
import eu.planets_project.services.identify.IdentifyResult.Method;
import eu.scape_project.pit.invoke.CommandNotFoundException;
import eu.scape_project.pit.invoke.PitInvoker;
import eu.scape_project.pit.invoke.ToolSpecNotFoundException;
import eu.scape_project.pit.tools.Parameters;

/**
 * http://fue.onb.ac.at/axis2-l/services/IMPACTOpenjpegConversionService?wsdl
 * 
 * @author anj
 *
 */
@MTOM
@StreamingAttachment(parseEagerly=true, memoryThreshold=4000000L)
@WebService
//SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.ENCODED, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public class ToolService {

	private PitInvoker ib;

	public ToolService() {
		try {
			ib  = new PitInvoker("file");
		} catch (ToolSpecNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 
	@WebMethod(operationName="identify")
	@WebResult(name="identifyResult")
	//@RequestWrapper(partName="arguments")
	//@ResponseWrapper(partName="results")
	public IdentifyResult identify( 
			@WebParam(name="toolId") String toolId, 
			@WebParam(name="inputURI") URI input, 
			@WebParam(name="parameters") Parameters parameters ) {
		try {
			ib.identify(toolId, new File( input ) );
		} catch (CommandNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<URI> formats = new Vector<URI>();
		formats.add(URI.create("image/jpeg"));
		return new IdentifyResult( formats, Method.MAGIC, null );
	}
	
}
