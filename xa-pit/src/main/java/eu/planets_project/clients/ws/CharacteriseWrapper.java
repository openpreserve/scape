/**
 * 
 */
package eu.planets_project.clients.ws;

import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.xml.ws.Service;

import java.util.logging.Logger;

import eu.planets_project.services.characterise.Characterise;
import eu.planets_project.services.characterise.CharacteriseResult;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.Property;
import eu.planets_project.services.datatypes.ServiceDescription;

/**
 * This is a wrapper class that upgrades all supported Identify service
 * interfaces to the same level.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 *
 */
public class CharacteriseWrapper implements Characterise {

    /** */
    private static final Logger log = Logger.getLogger(CharacteriseWrapper.class.getName());

    PlanetsServiceExplorer pse = null;
    Service service = null;
    Characterise c = null;
    
    /**
     * @param wsdl The WSDL to wrap as a service.
     */
    public CharacteriseWrapper( URL wsdl ) {
        this.pse = new PlanetsServiceExplorer(wsdl);
        this.init();
    }

    /**
     * @param pse Construct based on a service explorer.
     */
    public CharacteriseWrapper(PlanetsServiceExplorer pse) {
        this.pse = pse;
        this.init();
    }

    /**
     * 
     */
    private void init() {
        service = Service.create(pse.getWsdlLocation(), pse.getQName());
        try {
            c = (Characterise) service.getPort(pse.getServiceClass());
        } catch( Exception e ) {
            log.severe("Failed to instanciate service "+ pse.getQName() +" at "+pse.getWsdlLocation() + " : Exception - "+e);
            e.printStackTrace();
            c = null;
        }
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.characterise.Characterise#describe()
     */
    public ServiceDescription describe() {
        return c.describe();
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.characterise.Characterise#characterise(eu.planets_project.services.datatypes.DigitalObject, eu.planets_project.services.datatypes.Parameters)
     */
    public CharacteriseResult characterise(DigitalObject digitalObject,
            List<Parameter> parameters) {
        return c.characterise(digitalObject, parameters);
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.characterise.Characterise#listProperties(java.net.URI)
     */
    public List<Property> listProperties(URI formatURI) {
        return c.listProperties(formatURI);
    }

    
    
}
