package eu.planets_project.clients.ws;

import java.net.URL;
import java.util.logging.Logger;

import javax.xml.ws.Service;

import eu.planets_project.services.PlanetsService;
import eu.planets_project.services.datatypes.ServiceDescription;

/**
 * 
 * @author <a href="mailto:andrew.jackson@bl.uk">Andy Jackson</a>
 *
 */
public class DiscoveryUtils {
    /** */
    private static final Logger log = Logger.getLogger(DiscoveryUtils.class.getName());
     
    /**
     * Attempts to determine the service description for the given WSDL.
     * 
     * @param wsdlLocation
     * @return the service description from the service endpoint describe() method
     */
    public static ServiceDescription getServiceDescription( URL wsdlLocation ) {
        try {
        	PlanetsServiceExplorer se = new PlanetsServiceExplorer(wsdlLocation);
            PlanetsService s = (PlanetsService) createServiceObject(se.getServiceClass(), wsdlLocation);
            if( s == null ) return null;
            ServiceDescription sd = s.describe();
            return sd;
        } catch( Exception e ) {
            log.severe("Runtime exception while inspecting WSDL: "+wsdlLocation+" : "+e);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * A generic method that can be used as a short-cut for instanciating Planets services.
     * 
     * e.g. Migrate m = DiscoveryUtils.createServiceClass( Migrate.class, wsdlLocation);
     * 
     * If the given WSDL points to one of the older 'Basic' service forms, it will be wrapped up 
     * to present the new API and hide the old one.
     * 
     * @param <T> Any recognised service class (extends PlanetsService).
     * @param serviceClass The class of the Planets service to instanciate, e.g. Identify.class
     * @param wsdlLocation The location of the WSDL.
     * @return A new instance of the the given class, wrapping the referenced service.
     */
    public static <T> T createServiceObject( Class<T> serviceClass, URL wsdlLocation ) {
        PlanetsServiceExplorer se = new PlanetsServiceExplorer(wsdlLocation);
        Service service;
        if( serviceClass == null || wsdlLocation == null ) return null;
        service = Service.create(wsdlLocation, se.getQName());
        return (T) service.getPort(serviceClass);
    }
    
}
