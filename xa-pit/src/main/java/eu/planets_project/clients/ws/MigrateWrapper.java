/**
 * 
 */
package eu.planets_project.clients.ws;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;

import org.apache.commons.io.IOUtils;

import com.sun.xml.ws.developer.JAXWSProperties;

import eu.planets_project.ifr.core.techreg.formats.Format;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistry;
import eu.planets_project.ifr.core.techreg.formats.FormatRegistryFactory;
import eu.planets_project.services.datatypes.Content;
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

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main( String[] args ) throws Exception {
    	FormatRegistry fr = FormatRegistryFactory.getFormatRegistry();
    	URI rtf = fr.createExtensionUri("rtf");
    	URI wpd = fr.createExtensionUri("wpd");
    	Format f = fr.getFormatForUri( rtf );
    	URI service  = URI.create("http://132.230.8.85:8080/psuite-pa-ufcmigrate/UfcMigrate?wsdl");
    	Migrate m = MigrateWrapper.createWrapper(service.toURL());
    	System.out.println("Description: "+m.describe().toXmlFormatted());
    	DigitalObject.Builder doin = new DigitalObject.Builder(Content.byValue( new File("/Users/andy/Downloads/Aspen letter 2.wpd") ));
    	// Do It:
    	MigrateResult mr = m.migrate(doin.build(), wpd, rtf, null);
    	InputStream migout = mr.getDigitalObject().getContent().getInputStream();
    	FileOutputStream fout = new FileOutputStream( new File("test.rtf"));
    	IOUtils.copy(migout, fout);
    	migout.close();
    	fout.close();
    }
    
}
