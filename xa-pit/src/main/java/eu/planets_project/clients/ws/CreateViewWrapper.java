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
import eu.planets_project.services.view.CreateView;
import eu.planets_project.services.view.CreateViewResult;
import eu.planets_project.services.view.ViewActionResult;
import eu.planets_project.services.view.ViewStatus;

/**
 * This is a wrapper class that upgrades all supported Identify service
 * interfaces to the same level.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public class CreateViewWrapper implements CreateView {

    /** */
    private static final Logger log = Logger.getLogger(CreateViewWrapper.class.getName());

    PlanetsServiceExplorer pse = null;
    Service service = null;
    CreateView c = null;
    
    /**
     * @param wsdl The WSDL to wrap as a service.
     */
    public CreateViewWrapper( URL wsdl ) {
        this.pse = new PlanetsServiceExplorer(wsdl);
        this.init();
    }

    /**
     * @param pse Construct based on a service explorer.
     */
    public CreateViewWrapper(PlanetsServiceExplorer pse) {
        this.pse = pse;
        this.init();
    }

    /**
     * 
     */
    private void init() {
        service = Service.create(pse.getWsdlLocation(), pse.getQName());
        try {
            c = (CreateView) service.getPort(pse.getServiceClass());
        } catch( Exception e ) {
            log.severe("Failed to instanciate service "+ pse.getQName() +" at "+pse.getWsdlLocation() + " : Exception - "+e);
            e.printStackTrace();
            c = null;
        }
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#describe()
     */
    public ServiceDescription describe() {
        return c.describe();
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#createView(java.util.List, java.util.List)
     */
    public CreateViewResult createView(List<DigitalObject> digitalObjects,
            List<Parameter> parameters) {
        return c.createView(digitalObjects, parameters);
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#doAction(java.lang.String, java.lang.String)
     */
    public ViewActionResult doAction(String sessionIdentifier, String action) {
        return c.doAction(sessionIdentifier, action);
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.view.CreateView#getViewStatus(java.lang.String)
     */
    public ViewStatus getViewStatus(String sessionIdentifier) {
        return c.getViewStatus(sessionIdentifier);
    }


    
    
}
