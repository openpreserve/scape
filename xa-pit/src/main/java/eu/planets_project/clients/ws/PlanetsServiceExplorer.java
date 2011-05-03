package eu.planets_project.clients.ws;

import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.Service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.planets_project.services.PlanetsService;
import eu.planets_project.services.characterise.Characterise;
import eu.planets_project.services.compare.CommonProperties;
import eu.planets_project.services.compare.Compare;
import eu.planets_project.services.compare.CompareProperties;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.fixity.Fixity;
import eu.planets_project.services.identify.Identify;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.modify.Modify;
import eu.planets_project.services.validate.Validate;
import eu.planets_project.services.view.CreateView;

/**
 * 
 * @author <a href="mailto:andrew.jackson@bl.uk">Andy Jackson</a>
 *
 */
public class PlanetsServiceExplorer {
	private static Logger log = Logger.getLogger(PlanetsServiceExplorer.class.getName());

    private URL wsdlLocation = null;
    private QName qName = null;
    
    // Create a static hashmap, mapping QNames to the interfaces:
    private static HashMap<QName, Class<?>> classmap = new HashMap<QName, Class<?>>();
    static {
        classmap.put(CommonProperties.QNAME, CommonProperties.class);
        classmap.put(Identify.QNAME, Identify.class);
        classmap.put(Migrate.QNAME, Migrate.class);
        classmap.put(Modify.QNAME, Modify.class);
        classmap.put(Validate.QNAME, Validate.class);
        classmap.put(Characterise.QNAME, Characterise.class);
        classmap.put(CreateView.QNAME, CreateView.class);
        classmap.put(Compare.QNAME, Compare.class);
        classmap.put(CompareProperties.QNAME, CompareProperties.class);
        classmap.put(Fixity.QNAME, Fixity.class);
    }

    /**
     * Probes for the QName on construction.
     * @param wsdlLocation The location of the WSDL of the service.
     */
    public PlanetsServiceExplorer(URL wsdlLocation) {
    	log.fine("Creating new instance");
        this.wsdlLocation = wsdlLocation;
        this.qName = determineServiceQNameFromWsdl();
    }

    /**
     * @return the wsdlLocation
     */
    public URL getWsdlLocation() {
        return wsdlLocation;
    }

    /**
     * @return the qName
     */
    public QName getQName() {
        return qName;
    }

    /**
     * Attempts to instantiate a service, and so checks if the thing is
     * essentially working.
     * 
     * @return true if an instanstiable PlanetsService
     */
    public boolean isServiceInstanciable() {
        Service service = Service.create(wsdlLocation, qName);
        PlanetsService s = (PlanetsService) service.getPort(getServiceClass());
        if ( s  != null ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the service description for this service, or null.
     */
    public ServiceDescription getServiceDescription() {
        Service service = Service.create(wsdlLocation, qName);
        PlanetsService s = (PlanetsService) service.getPort(getServiceClass());
        if ( s  != null ) {
            return s.describe();
        } else {
            return null;
        }
    }

    /**
     * @return the service class
     */
    public Class<?> getServiceClass() {
        return classmap.get(qName);
    }

    /**
     * This method examines a given service end-point and attempt to determine
     * the QName of the wsdl:service.
     * 
     * @param wsdlLocation
     * @return the QName
     */
    private QName determineServiceQNameFromWsdl() {
    	log.fine("determining qname");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        // Using factory get an instance of document builder
        DocumentBuilder db;
        try {
        	log.fine("new doc builder");
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }

        // parse using builder to get DOM representation of the XML file
        Document dom;
        try {
        	log.fine("parsing wsdl");
            dom = db.parse(wsdlLocation.openStream());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // get the root elememt
        Element root = dom.getDocumentElement();
        log.fine("getting root element");
        return new QName(root.getAttribute("targetNamespace"), root
                .getAttribute("name"));
    }

}
