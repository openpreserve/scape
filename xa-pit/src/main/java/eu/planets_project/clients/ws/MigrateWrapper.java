/**
 * 
 */
package eu.planets_project.clients.ws;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;

import com.sun.xml.ws.developer.JAXWSProperties;

import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.ServiceUtils;

/**
 * This is a wrapper class that upgrades all supported Migrate service
 * interfaces to the same level.
 * 
 * @author <a href="mailto:Andrew.Jackson@bl.uk">Andy Jackson</a>
 * 
 */
@WebServiceClient(name = "Migrate", targetNamespace = "http://planets-project.eu/services")
public class MigrateWrapper extends Service implements Migrate {
    /** */
    private static final Logger log = Logger.getLogger(MigrateWrapper.class.getName());

    PlanetsServiceExplorer pse = null;
    Migrate m = null;
    
    /**
     * 
     * @param wsdlLocation
     * @param serviceName
     */
    private MigrateWrapper(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
        this.pse = new PlanetsServiceExplorer(wsdlLocation);
        init();
    }
    
    /**
     * @param wsdl The WSDL to wrap as a service.
     */
    public static Migrate createWrapper( URL wsdl ) {
        MigrateWrapper mw = new MigrateWrapper(wsdl, Migrate.QNAME);
        return mw;
    }
    
    /**
     * Set up the migration service, using the right features and configuration.
     */
    private void init() {
        try {
            // Set up the service, using the standard features.
            m = this.getPort(Migrate.class);//, ServiceUtils.JAXWS_FEATURES );

            // This enables streaming when combined with the MTOM feature (above)
            ((BindingProvider)m).getRequestContext().put( 
                    JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, ServiceUtils.JAXWS_SIZE_THRESHOLD );


        } catch( Exception e ) {
            log.severe("Failed to instanciate service "+ pse.getQName() +" at "+pse.getWsdlLocation() + " : Exception - "+e);
            m = null;
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see eu.planets_project.services.migrate.Migrate#describe()
     */
    public ServiceDescription describe() {
        if( m == null ) return null;
        return m.describe();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.planets_project.services.migrate.Migrate#migrate(eu.planets_project
     * .services.datatypes.DigitalObject, java.net.URI, java.net.URI,
     * eu.planets_project.services.datatypes.Parameters)
     */
    public MigrateResult migrate(DigitalObject digitalObject, URI inputFormat,
            URI outputFormat, List<Parameter> parameters) {
        if( m == null ) return null;
        return m.migrate(digitalObject, inputFormat, outputFormat, parameters);
    }

}
