/**
 * Copyright (c) 2007, 2008, 2009, 2010 The Planets Project Partners.
 * 
 * All rights reserved. This program and the accompanying 
 * materials are made available under the terms of
 * the Apache License version 2.0 which accompanies
 * this distribution, and is available at:
 *   http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 */
package eu.planets_project.ifr.core.servreg.utils.client;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.sun.xml.ws.developer.JAXWSProperties;

import eu.planets_project.clients.ws.IdentifyWrapper;
import eu.planets_project.clients.ws.MigrateWrapper;
import eu.planets_project.clients.ws.PlanetsServiceExplorer;
import eu.planets_project.services.PlanetsService;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.identify.Identify;
import eu.planets_project.services.identify.IdentifyResult;
import eu.planets_project.services.migrate.Migrate;
import eu.planets_project.services.migrate.MigrateResult;
import eu.planets_project.services.utils.DigitalObjectUtils;

/**
 * A really simple class that allows a Planets Service to be invoked from the command line.
 * 
 * http://java.sun.com/webservices/docs/2.0/jaxws/mtom-swaref.html
 * 
 * Need to add mtom-enabled to jax-ws deployment descriptor?
 * 
 * @author AnJackson
 *
 */
public class PlanetsCommand {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        
        /* FIXME, point to log4j.properties instead of doing this? */
        /*
        java.util.logging.Logger.getLogger("com.sun.xml.ws.model").setLevel(java.util.logging.Level.WARNING); 
        java.util.logging.Logger.getAnonymousLogger().setLevel(java.util.logging.Level.WARNING);
        Logger sunlogger = Logger.getLogger("com.sun.xml.ws.model");
        sunlogger.setLevel(Level.WARNING);
        java.util.logging.Logger.getLogger( com.sun.xml.ws.util.Constants.LoggingDomain).setLevel(java.util.logging.Level.WARNING);
*/        
        /* Lots of info please: */
        java.util.logging.Logger.getAnonymousLogger().setLevel(java.util.logging.Level.FINEST);
        java.util.logging.Logger.getLogger( com.sun.xml.ws.util.Constants.LoggingDomain).setLevel(java.util.logging.Level.FINEST);
        // TODO See https://jax-ws.dev.java.net/guide/Logging.html for info on more logging to set up.
        //System.setProperty("com.sun.xml.ws.transport.local.LocalTransportPipe.dump","true");
        //System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump","true");
        //System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump","true");
        // Doing this KILLS STREAMING. Log that.
        //System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump","true");
        
        URL wsdl;
        try {
            wsdl = new URL( args[0] );
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        
        PlanetsServiceExplorer pse = new PlanetsServiceExplorer(  wsdl );
        
        System.out.println(".describe(): "+pse.getServiceDescription());
        
        Service service = Service.create(wsdl, pse.getQName());
        //service.addPort(portName, SOAPBinding.SOAP11HTTP_MTOM_BINDING, endpointAddress)
        PlanetsService ps = (PlanetsService) service.getPort(pse.getServiceClass());
        
        // TODO The client wrapper code should enforce this stuff:
        SOAPBinding binding = (SOAPBinding)((BindingProvider)ps).getBinding();
        System.out.println("Logging MTOM="+binding.isMTOMEnabled());
        ((BindingProvider)ps).getRequestContext().put( JAXWSProperties.MTOM_THRESHOLOD_VALUE, 8192);
        ((BindingProvider)ps).getRequestContext().put( JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
        System.out.println("Logging MTOM="+binding.isMTOMEnabled());
        binding.setMTOMEnabled(true);
        System.out.println("Logging MTOM="+binding.isMTOMEnabled());
        //System.out.println("Logging MTOM="+((BindingProvider)ps).getBinding().getBindingID()+" v. "+SOAPBinding.SOAP11HTTP_MTOM_BINDING);

        /* 
         * The different services are invoked in different ways...
         */
        if( pse.getQName().equals( Migrate.QNAME ) ) {
            System.out.println("Is a Migrate service. ");
            Migrate s = MigrateWrapper.createWrapper(wsdl);
            
            DigitalObject dobIn = new DigitalObject.Builder(Content.byReference( new File(args[1]))).build();
            
            MigrateResult result = s.migrate(dobIn, URI.create(args[2]), URI.create(args[3]), null);
            
            System.out.println("ServiceReport: "+result.getReport());
            
            DigitalObjectUtils.toFile( result.getDigitalObject(), new File("output" ) );
            
        } else if( pse.getQName().equals( Identify.QNAME ) ) {
            System.out.println("Is an Identify service. ");
            Identify s = new IdentifyWrapper(wsdl);
            
            DigitalObject dobIn = new DigitalObject.Builder(Content.byReference( new File(args[1]))).build();
            
            IdentifyResult result = s.identify(dobIn, null);
                        
            System.out.println("ServiceReport: "+result.getReport());
        }
    }
    
    void temp() {
     // create the command line parser
        CommandLineParser parser = new PosixParser();

        // create the Options
        Options options = new Options();
        options.addOption( "a", "all", false, "do not hide entries starting with ." );
        options.addOption( "A", "almost-all", false, "do not list implied . and .." );
        options.addOption( "b", "escape", false, "print octal escapes for nongraphic "
                                                 + "characters" );
        options.addOption( OptionBuilder.withLongOpt( "block-size" )
                                        .withDescription( "use SIZE-byte blocks" )
                                        .hasArg()
                                        .withArgName("SIZE")
                                        .create() );
        options.addOption( "B", "ignore-backups", false, "do not list implied entried "
                                                         + "ending with ~");
        options.addOption( "c", false, "with -lt: sort by, and show, ctime (time of last " 
                                       + "modification of file status information) with "
                                       + "-l:show ctime and sort by name otherwise: sort "
                                       + "by ctime" );
        options.addOption( "C", false, "list entries by columns" );
        
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "mercury", options );

        String[] args = new String[]{ "--block-size=10" };

        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );

            // validate that block-size has been set
            if( line.hasOption( "block-size" ) ) {
                // print the value of block-size
                System.out.println( line.getOptionValue( "block-size" ) );
            }
        }
        catch( ParseException exp ) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
        }
        
    }

}
