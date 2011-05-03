package eu.planets_project.clients.ws;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.planets_project.ifr.core.common.conf.PlanetsServerConfig;

/**
 * @author <a href="mailto:andrew.jackson@bl.uk">Andy Jackson</a>
 */
public class EndpointUtils {
    private static final Logger log = Logger.getLogger(EndpointUtils.class.getName());
    private static final Pattern urlpattern = Pattern
            .compile("http://[^><\\s]*?\\?wsdl");

    /**
     * @return a list of all available service endpoints as URIs
     */
    public static List<URI> listAvailableEndpoints() {
        log.info("ServiceLookup.listAvailableEndpoints()");
        Set<URI> uniqueSE = new HashSet<URI>();

        // Inspect the local JBossWS endpoints:
        try {
            log.info("Creating authority string");
            String authority = PlanetsServerConfig.getHostname() + ":"
                    + PlanetsServerConfig.getPort();
            log.info("authority string is " + authority);
            log.info("Creating URI for JBOSS services page");
            URI uriPage = new URI("http", authority, "/jbossws/services", null,
                    null);
            log.info("URI set up OK to " + uriPage.toASCIIString());
            // 2) extract the page's content: note: not well-formed -->
            // modifications
            log.info("Reading page contents to string");
            String pageContent = readUrlContents(uriPage);
            // 3) build a dom tree and extract the text nodes
            // String xPath = new
            // String("/*//fieldset/table/tbody/tr/td/a/@href");
            log.info("Got page content, now extracting URIs of services");
            uniqueSE.addAll(extractEndpointsFromWebPage(pageContent));
        } catch (URISyntaxException e) {
            log.severe("URI Syntax exception : " + e);
        } catch (IOException e) {
            log.severe("IO Exception reading URL contents" + e);
        }

        // Now sort the list and return it.
        log.info("Creating sorted list of URIs");
        List<URI> sList = new ArrayList<URI>(uniqueSE);
        log.info("List created and has " + sList.size() + " services");
        java.util.Collections.sort(sList);
        log.info("returning list");
        return sList;
    }

    /**
     * Takes a given http URI and extracts the page's content which is returned
     * as String.
     * @param uri The URI
     * @return the page's content
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static String readUrlContents(URI uri)
            throws FileNotFoundException, IOException {

        InputStream in = null;
        try {
            if (!uri.getScheme().equals("http")) {
                throw new FileNotFoundException("URI schema " + uri.getScheme()
                        + " not supported");
            }
            in = uri.toURL().openStream();
            boolean eof = false;
            String content = "";
            StringBuffer sb = new StringBuffer();
            while (!eof) {
                int byteValue = in.read();
                if (byteValue != -1) {
                    char b = (char) byteValue;
                    sb.append(b);
                } else {
                    eof = true;
                }
            }
            content = sb.toString();
            if (content != null) {
                // now return the services WSDL content
                return content;
            } else {
                throw new FileNotFoundException("extracted content is null");
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * As the wsdl content is not well-formed we're not able to use DOM here.
     * Parse through the xml manually to return a list of given
     * ServiceEndpointAddresses
     * @param xhtml The xml
     * @return a list of given ServiceEndpointAddresses
     */
    private static Set<URI> extractEndpointsFromWebPage(String xhtml) {
        log.info("ServiceLookup.extractEndpointsFromWebPage()");
        Set<URI> ret = new HashSet<URI>();

        // Pull out all matching URLs:
        log.info("about to try the regexp magik");
        Matcher matcher = urlpattern.matcher(xhtml);
        while (matcher.find()) {
            log.info("Found match: " + matcher.group());
            try {
                log.info("creating new URL");
                URL wsdlUrl = new URL(matcher.group());
                // Switch the authority for the 'proper' one:
                log.info("Patching in the hacked authority information");
                wsdlUrl = new URL(wsdlUrl.getProtocol(), PlanetsServerConfig
                        .getHostname(), PlanetsServerConfig.getPort(), wsdlUrl
                        .getFile());
                log.info("Got matching URL: " + wsdlUrl);
                try {
                    ret.add(wsdlUrl.toURI());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                log.severe("Could not parse URL from " + matcher.group());
            }
        }
        log.info("returning the URI set");
        return ret;
    }

}
