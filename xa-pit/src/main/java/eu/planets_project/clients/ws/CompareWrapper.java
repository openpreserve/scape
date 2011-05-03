/**
 * 
 */
package eu.planets_project.clients.ws;

import java.net.URL;
import java.util.List;

import javax.xml.ws.Service;

import java.util.logging.Logger;

import eu.planets_project.services.compare.Compare;
import eu.planets_project.services.compare.CompareResult;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;

/**
 * This is a wrapper class that upgrades all supported Identify service
 * interfaces to the same level.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public class CompareWrapper implements Compare {

    /** */
    private static final Logger log = Logger.getLogger(CompareWrapper.class.getName());

    PlanetsServiceExplorer pse = null;
    Service service = null;
    Compare c = null;
    
    /**
     * @param wsdl The WSDL to wrap as a service.
     */
    public CompareWrapper( URL wsdl ) {
        this.pse = new PlanetsServiceExplorer(wsdl);
        this.init();
    }

    /**
     * @param pse Construct based on a service explorer.
     */
    public CompareWrapper(PlanetsServiceExplorer pse) {
        this.pse = pse;
        this.init();
    }

    /**
     * 
     */
    private void init() {
        service = Service.create(pse.getWsdlLocation(), pse.getQName());
        try {
            c = (Compare) service.getPort(pse.getServiceClass());
        } catch( Exception e ) {
            log.severe("Failed to instanciate service "+ pse.getQName() +" at "+pse.getWsdlLocation() + " : Exception - "+e);
            e.printStackTrace();
            c = null;
        }
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.compare.Compare#describe()
     */
    public ServiceDescription describe() {
        return c.describe();
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.compare.Compare#compare(eu.planets_project.services.datatypes.DigitalObject, eu.planets_project.services.datatypes.DigitalObject, java.util.List)
     */
    public CompareResult compare(DigitalObject first, DigitalObject second,
            List<Parameter> config) {
        return c.compare(first, second, config);
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.compare.Compare#convert(eu.planets_project.services.datatypes.DigitalObject)
     */
    public List<Parameter> convert(DigitalObject configFile) {
        return c.convert(configFile);
    }

    
    
}
