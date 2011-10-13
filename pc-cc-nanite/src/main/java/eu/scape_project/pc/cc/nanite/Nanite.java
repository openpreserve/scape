/**
 * 
 */
package eu.scape_project.pc.cc.nanite;

import static uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig.DROID_USER;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManagerException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;

/**
 * 
 * Finding the actual droid-core invocation was tricky
 * From droid command line
 * - ReportCommand which launches a profileWalker,
 * - which fires a FileEventHandler when it hits a file,
 * - AsyncDroid subtype SubmissionGateway, 
 * - which calls DroidCore,
 * - which calls uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier
 * - Following which, SubmissionGateway does some handleContainer stuff, 
 * executes the container matching engine and does some complex logic to resolve the result.
 * 
 * @see uk.gov.nationalarchives.droid.submitter.SubmissionGateway
 * @see uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier
 * 
 * Also found 
 * @see uk.gov.nationalarchives.droid.command.action.DownloadSignatureUpdateCommand
 * which indicates how to download the latest sig file, 
 * but perhaps the SignatureManagerImpl does all that is needed?
 * 
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 * @author Fabian Steeg
 * @author <a href="mailto:carl.wilson@bl.uk">Carl Wilson</a> <a
 *         href="http://sourceforge.net/users/carlwilson-bl"
 *         >carlwilson-bl@SourceForge</a> <a
 *         href="https://github.com/carlwilson-bl">carlwilson-bl@github</a>
 *
 */
public class Nanite {
	
	
	private SignatureManager sm;
	private ClassPathXmlApplicationContext context;
	private BinarySignatureIdentifier bsi;


	public Nanite() throws IOException, SignatureFileException {
		System.setProperty("consoleLogThreshold","INFO");
		System.setProperty("logFile", "./nanite.log");
		PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
		
		// System.getProperty("java.io.tmpdir")
		//String droidDirname = System.getProperty("user.home")+File.separator+".droid6";
		String droidDirname = System.getProperty("java.io.tmpdir")+File.separator+"droid6";
		//System.out.println("GOT: "+droidDirname);
		File droidDir = new File(droidDirname);
		if( ! droidDir.isDirectory() ) {
			if( ! droidDir.exists() ) {
				droidDir.mkdirs();
			} else {
				throw new IOException("Cannot create droid folder: "+droidDirname);
			}
		}
		System.setProperty(DROID_USER, droidDir.getAbsolutePath());
		
		// Fire up required classes via Spring:
		context = new ClassPathXmlApplicationContext("classpath*:/META-INF/ui-spring.xml");
        context.registerShutdownHook();
		sm = (SignatureManager) context.getBean("signatureManager");
        
		// Without Spring, you need something like...		
/*		DroidGlobalConfig dgc = new DroidGlobalConfig();	
		dgc.init();
		System.out.println("Tjhis: "+dgc.getProperties());
		SignatureManagerImpl sm = new SignatureManagerImpl();
		sm.setConfig(dgc);
		
		Map<SignatureType, SignatureUpdateService> signatureUpdateServices = new HashMap<SignatureType, SignatureUpdateService>();
		PronomSignatureService pss = new PronomSignatureService();
		pss.setFilenamePattern("DROID_SignatureFile_V%s.xml");
		PronomService pronomService;
		pss.setPronomService(pronomService);
		signatureUpdateServices.put(SignatureType.BINARY, pss);
		signatureUpdateServices.put(SignatureType.CONTAINER, new ContainerSignatureHttpService() );
		sm.setSignatureUpdateServices(signatureUpdateServices);
		sm.init();
*/
		
		//SignatureFileInfo latest = sm.downloadLatest(SignatureType.BINARY);
		
		// Now set up the Binary Signature Identifier with the right signature from the manager:
		bsi = new BinarySignatureIdentifier();
		bsi.setSignatureFile(sm.getDefaultSignatures().get(SignatureType.BINARY).getFile().getAbsolutePath());
		bsi.init();
	}

	/**
	 * 
	 * @param ir
	 * @return
	 */
	public IdentificationResultCollection identify(IdentificationRequest ir) {
		return bsi.matchBinarySignatures(ir);
	}

	/**
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static IdentificationRequest createFileIdentificationRequest( File file ) throws FileNotFoundException, IOException {
		URI uri = file.toURI();
        RequestMetaData metaData = new RequestMetaData( file.length(), file
                .lastModified(), file.getName());
        
        RequestIdentifier identifier = new RequestIdentifier(uri);
		identifier.setParentId(1L);
        //identifier.setParentResourceId(parentId);
        //identifier.setResourceId(nodeId);
        
        IdentificationRequest ir = new FileSystemIdentificationRequest(metaData, identifier);
        // Attach the byte arrays of content:
        ir.open(new FileInputStream(file));
		return ir;
	}
	
	public static IdentificationRequest createByteArrayIdentificationRequest( URI uri, byte[] data ) throws IOException {
        RequestMetaData metaData = new RequestMetaData( (long)data.length, null, uri.toString() );
        
        RequestIdentifier identifier = new RequestIdentifier(uri);
		identifier.setParentId(1L);
        //identifier.setParentResourceId(parentId);
        //identifier.setResourceId(nodeId);
        
        IdentificationRequest ir = new ByteArrayIdentificationRequest(metaData, identifier, data);
        // Attach the byte arrays of content:
        //ir.open(new ByteArrayInputStream(data));
		return ir;
	}


	/**
	 * 
	 * @param uri
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static IdentificationRequest createInputStreamIdentificationRequest( URI uri, InputStream in ) throws IOException {
        RequestMetaData metaData = new RequestMetaData( (long)in.available(), null, uri.toString() );
        
        RequestIdentifier identifier = new RequestIdentifier(uri);
		identifier.setParentId(1L);
        //identifier.setParentResourceId(parentId);
        //identifier.setResourceId(nodeId);
        
        IdentificationRequest ir = new FileSystemIdentificationRequest(metaData, identifier);
        // Attach the byte arrays of content:
        ir.open(in);
		return ir;
	}
	
	/**
	 * 
	 * @param res
	 * @return
	 */
	public static String getMimeTypeFromResult( IdentificationResult res ) {
		String mimeType = res.getMimeType();
		// Is there a mimeType?
		if( mimeType != null && ! "".equals(mimeType) ) {
			// Patch on a version parameter if there isn't one there already:
			if( !mimeType.contains("version=") && 
					res.getVersion() != null && ! "".equals(res.getVersion()) ) {
				mimeType += "; version="+res.getVersion();
			}
		} else {
			// If there isn't a MIME type, make one up:
			String name = res.getName();
			if( res.getVersion() != null && ! "".equals(res.getVersion()) ) {
				name += "-" + res.getVersion();
			}
			name = name.replace("\"", "");
			name = name.replace(" ", "-").toLowerCase();
			// Add the puid as a parameter:
			mimeType = "application/x-"+name+"; puid="+res.getPuid();
		}
		return mimeType;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SignatureManagerException 
	 * @throws ConfigurationException 
	 * @throws SignatureFileException 
	 */
	public static void main(String[] args) throws IOException, SignatureManagerException, ConfigurationException, SignatureFileException {
		File file = new File(args[0]);
		//IdentificationRequest ir = createFileIdentificationRequest(file);
		
		byte[] data =  org.apache.commons.io.FileUtils.readFileToByteArray(file);
		IdentificationRequest ir = createByteArrayIdentificationRequest(file.toURI(), data);		
		
		Nanite nan = new Nanite();
		
		IdentificationResultCollection resultCollection = nan.identify(ir);
		//System.out.println("MATCHING: "+resultCollection.getResults());
		for( IdentificationResult result : resultCollection.getResults() ) {
			String mimeType = result.getMimeType();
			if( result.getVersion() != null && ! "".equals(result.getVersion())) {
				mimeType += ";version="+result.getVersion();
			}
			System.out.println("MATCHING: "+result.getPuid()+", "+result.getName()+" "+result.getVersion());
			System.out.println("Content-Type: "+Nanite.getMimeTypeFromResult(result));
		}
		
	}
	

}
