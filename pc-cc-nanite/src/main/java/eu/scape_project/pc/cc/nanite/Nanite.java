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
 * 
 * Also found 
 * - uk.gov.nationalarchives.droid.command.action.DownloadSignatureUpdateCommand
 * which indicates how to download the latest sig file, 
 * but perhaps the SignatureManagerImpl does all that is needed?
 * 
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class Nanite {
	
	
	private SignatureManager sm;
	private ClassPathXmlApplicationContext context;
	private BinarySignatureIdentifier bsi;


	public Nanite() throws IOException, SignatureFileException {
		PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
		
		// System.getProperty("java.io.tmpdir")
		//String droidDirname = System.getProperty("user.home")+File.separator+".droid6";
		String droidDirname = System.getProperty("java.io.tmpdir")+File.separator+"droid6";
		System.out.println("GOT: "+droidDirname);
		File droidDir = new File(droidDirname);
		if( ! droidDir.isDirectory() ) {
			if( ! droidDir.exists() ) {
				droidDir.mkdirs();
			} else {
				throw new IOException("Cannot create droid folder: "+droidDirname);
			}
		}
		System.setProperty(DROID_USER, droidDir.getAbsolutePath());
		
		context = new ClassPathXmlApplicationContext("classpath*:/META-INF/ui-spring.xml");
        context.registerShutdownHook();
        
		// TODO Auto-generated method stub
/*		DroidGlobalConfig dgc = new DroidGlobalConfig();	
		dgc.init();
		System.out.println("Tjhis: "+dgc.getProperties());
		SignatureManagerImpl sm = new SignatureManagerImpl();
		sm.setConfig(dgc);
*/		
		sm = (SignatureManager) context.getBean("signatureManager");
/*		Map<SignatureType, SignatureUpdateService> signatureUpdateServices = new HashMap<SignatureType, SignatureUpdateService>();
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
		
		
		//?? DownloadSignatureUpdateCommand dsuc = new DownloadSignatureUpdateCommand();
		
		bsi = new BinarySignatureIdentifier();
		bsi.setSignatureFile(sm.getDefaultSignatures().get(SignatureType.BINARY).getFile().getAbsolutePath());
		bsi.init();
	}

	public IdentificationResultCollection identify(IdentificationRequest ir) {
		return bsi.matchBinarySignatures(ir);
	}

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
	
	public static String getMimeTypeFromResult( IdentificationResult res ) {
		String droidType = res.getMimeType();
		if( droidType != null && ! "".equals(droidType) ) {
			if( res.getVersion() != null && ! "".equals(res.getVersion()) ) {
				droidType += "; version="+res.getVersion();
			}
		} else {
			String name = res.getName();
			if( res.getVersion() != null && ! "".equals(res.getVersion()) ) {
				name += "-" + res.getVersion();
			}
			name = name.replace(" ", "-").toLowerCase();
			droidType = "application/x-"+name+"; puid="+res.getPuid();
		}
		return droidType;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SignatureManagerException 
	 * @throws ConfigurationException 
	 * @throws SignatureFileException 
	 */
	public static void main(String[] args) throws IOException, SignatureManagerException, ConfigurationException, SignatureFileException {
		File file = new File("/Users/andy/Downloads/ANSI_NISO_Z39.86-2006.pdf");
		//IdentificationRequest ir = createFileIdentificationRequest(file);
		
		byte[] data =  org.apache.commons.io.FileUtils.readFileToByteArray(file);
		IdentificationRequest ir = createByteArrayIdentificationRequest(file.toURI(), data);		
		
		Nanite nan = new Nanite();
		
		IdentificationResultCollection resultCollection = nan.identify(ir);
		System.out.println("MATCHING: "+resultCollection.getResults());
		for( IdentificationResult result : resultCollection.getResults() ) {
			String mimeType = result.getMimeType();
			if( result.getVersion() != null && ! "".equals(result.getVersion())) {
				mimeType += ";version="+result.getVersion();
			}
			System.out.println("MATCHING: "+result.getPuid()+", "+mimeType+", "+result.getName());
		}
		
	}
	

}
