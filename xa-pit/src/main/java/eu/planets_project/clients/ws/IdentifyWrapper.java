/**
 * 
 */
package eu.planets_project.clients.ws;

import java.net.URL;
import java.util.List;

import javax.xml.ws.Service;

import java.util.logging.Logger;

import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.identify.Identify;
import eu.planets_project.services.identify.IdentifyResult;

/**
 * This is a wrapper class that upgrades all supported Identify service
 * interfaces to the same level.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public class IdentifyWrapper implements Identify {

    /** */
    private static final Logger log = Logger.getLogger(IdentifyWrapper.class.getName());

    PlanetsServiceExplorer pse = null;
    Service service = null;
    Identify i = null;
    
    /**
     * @param wsdl The WSDL to wrap as a service.
     */
    public IdentifyWrapper( URL wsdl ) {
        this.pse = new PlanetsServiceExplorer(wsdl);
        this.init();
    }

    /**
     * @param pse Construct based on a service explorer.
     */
    public IdentifyWrapper(PlanetsServiceExplorer pse) {
        this.pse = pse;
        this.init();
    }

    /**
     * 
     */
    private void init() {
        service = Service.create(pse.getWsdlLocation(), pse.getQName());
        try {
                i = (Identify) service.getPort(pse.getServiceClass());
        } catch( Exception e ) {
            log.severe("Failed to instanciate service "+ pse.getQName() +" at "+pse.getWsdlLocation() + " : Exception - "+e);
            i = null;
        }
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.identify.Identify#describe()
     */
    public ServiceDescription describe() {
            return i.describe();
    }


    /* (non-Javadoc)
     * @see eu.planets_project.services.identify.Identify#identify(eu.planets_project.services.datatypes.DigitalObject, java.util.List)
     */
    public IdentifyResult identify(DigitalObject digitalObject,
            List<Parameter> parameters) {
       
            return i.identify( digitalObject, parameters );
    }


}
