/**
 * 
 */
package eu.scape_project.xa.ws;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;

import com.sun.xml.ws.developer.StreamingAttachment;
import com.sun.xml.ws.developer.StreamingDataHandler;

import eu.planets_project.services.identify.IdentifyResult;
import eu.planets_project.services.identify.IdentifyResult.Method;
import eu.scape_project.pit.invoke.CommandNotFoundException;
import eu.scape_project.pit.invoke.In;
import eu.scape_project.pit.invoke.PitInvoker;
import eu.scape_project.pit.invoke.Processor;
import eu.scape_project.pit.invoke.ToolSpecNotFoundException;
import eu.scape_project.pit.tools.Inputs;

/**
 * http://fue.onb.ac.at/axis2-l/services/IMPACTOpenjpegConversionService?wsdl
 * 
 * @author anj
 *
 */
//MTOM
//StreamingAttachment(parseEagerly=true, memoryThreshold=4000000L)
@WebService
//SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.ENCODED, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public class ToolService {

	private Processor ib;

	public ToolService(String toolspec, String action) {
		try {
			ib  = Processor.createProcessor(toolspec, action);
		} catch (ToolSpecNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommandNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 
	@WebMethod(operationName="identify")
	@WebResult(name="identifyResult")
	@RequestWrapper(localName="arguments")
	//@RequestWrapper(partName="arguments")
	//@ResponseWrapper(partName="results")
	public IdentifyResult identify( 
			@WebParam(name="inputURI") URI input, 
			@WebParam(name="parameters") Inputs parameters ) {
		try {
			ib.execute( new In( input ), null );
		} catch (IOException e) {
            throw new WebServiceException(e);
		}
		List<URI> formats = new Vector<URI>();
		formats.add(URI.create("image/jpeg"));
		return new IdentifyResult( formats, Method.MAGIC, null );
	}
	
	public void fileUpload(String name, @XmlMimeType("application/octet-stream") DataHandler data) {
        try {
             StreamingDataHandler dh = (StreamingDataHandler)data;
             System.out.println("Got DataHander: "+dh);
             File file = File.createTempFile(name, "");
             dh.moveTo(file);
             dh.close();
        } catch(Exception e) {
             throw new WebServiceException(e);
        }
    }
}
