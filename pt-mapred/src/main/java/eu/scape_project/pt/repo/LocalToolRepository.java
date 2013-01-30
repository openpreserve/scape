/*
 * Copyright 2013 ait.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 *
 * @author Matthias Rella, DME-AIT
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

    private Tool fromInputStream(InputStream input) throws JAXBException {
		Unmarshaller u = jc.createUnmarshaller();
		return (Tool) u.unmarshal(new StreamSource(input));
    }

}
