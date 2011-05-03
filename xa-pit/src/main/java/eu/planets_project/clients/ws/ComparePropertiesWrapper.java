/**
 * 
 */
package eu.planets_project.clients.ws;

import java.net.URL;
import java.util.List;

import javax.xml.ws.Service;

import java.util.logging.Logger;

import eu.planets_project.services.characterise.CharacteriseResult;
import eu.planets_project.services.compare.CompareProperties;
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
public class ComparePropertiesWrapper implements CompareProperties {

    /** */
    private static final Logger log = Logger.getLogger(ComparePropertiesWrapper.class.getName());

    PlanetsServiceExplorer pse = null;
    Service service = null;
    CompareProperties c = null;
    
    /**
     * @param wsdl The WSDL to wrap as a service.
     */
    public ComparePropertiesWrapper( URL wsdl ) {
        this.pse = new PlanetsServiceExplorer(wsdl);
        this.init();
    }

    /**
     * @param pse Construct based on a service explorer.
     */
    public ComparePropertiesWrapper(PlanetsServiceExplorer pse) {
        this.pse = pse;
        this.init();
    }

    /**
     * 
     */
    private void init() {
        service = Service.create(pse.getWsdlLocation(), pse.getQName());
        try {
            c = (CompareProperties) service.getPort(pse.getServiceClass());
        } catch( Exception e ) {
            log.severe("Failed to instanciate service "+ pse.getQName() +" at "+pse.getWsdlLocation() + " : Exception - "+e);
            e.printStackTrace();
            c = null;
        }
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.PlanetsService#describe()
     */
    public ServiceDescription describe() {
        return c.describe();
    }
    
    /* (non-Javadoc)
     * @see eu.planets_project.services.compare.CompareProperties#compare(eu.planets_project.services.characterise.CharacteriseResult, eu.planets_project.services.characterise.CharacteriseResult, java.util.List)
     */
    public CompareResult compare(CharacteriseResult first,
            CharacteriseResult second, List<Parameter> config) {
        return c.compare(first, second, config);
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.compare.CompareProperties#convertConfig(eu.planets_project.services.datatypes.DigitalObject)
     */
    public List<Parameter> convertConfig(DigitalObject configFile) {
        return c.convertConfig(configFile);
    }

    /* (non-Javadoc)
     * @see eu.planets_project.services.compare.CompareProperties#convertInput(eu.planets_project.services.datatypes.DigitalObject)
     */
    public CharacteriseResult convertInput(DigitalObject inputFile) {
        return c.convertInput(inputFile);
    }
    
}
