package eu.scape_project.pt.repo;

import eu.scape_project.pt.tool.Tool;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mock-up repository to use in unit-testing on the local filesystem.
 * 
 * @author Matthias Rella, DME-AIT [myrho]
 */
public class LocalToolRepository implements Repository {
	private static Log LOG = LogFactory.getLog(ToolRepository.class);
	private static JAXBContext jc;
	
	static {
		try {
			jc  = JAXBContext.newInstance(Tool.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    private final File toolsDir;

    /**
     * Construct repository from directory path.
     * @param strToolsDir directory to toolspecs
     * @throws FileNotFoundException 
     */
    public LocalToolRepository(String strToolsDir) throws FileNotFoundException {
        File toolsDir = new File( strToolsDir );
        if( !toolsDir.isDirectory() ) 
            throw new FileNotFoundException();

        this.toolsDir = toolsDir;
    }

    @Override
    public String[] getToolList() {
        return this.toolsDir.list();
    }

    @Override
    public boolean toolspecExists(String file) {
        return new File( this.toolsDir.getPath() + 
                System.getProperty("file.separator") + file + ".xml").exists();
    }

    /**
     * Gets Tool from the repository.
     * 
     * @param toolName
     * @return
     * @throws FileNotFoundException 
     */
    public Tool getTool(String toolName ) throws FileNotFoundException {
        File fileTool = new File( this.toolsDir.getPath() + 
                System.getProperty("file.separator") + toolName + ".xml");

        FileInputStream fis = new FileInputStream(fileTool);
        try {
            return fromInputStream( fis );
        } catch (JAXBException ex) {
            LOG.error(ex);
        }
        return null;
        
    }

    /**
     * Unmarshals an input stream of xml data to a Tool.
     * @param input
     * @return
     * @throws JAXBException 
     */
    private Tool fromInputStream(InputStream input) throws JAXBException {
		Unmarshaller u = jc.createUnmarshaller();
		return (Tool) u.unmarshal(new StreamSource(input));
    }

}
