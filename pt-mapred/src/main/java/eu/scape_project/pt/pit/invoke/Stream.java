/*
 * Copyright 2012 ait.
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
package eu.scape_project.pt.pit.invoke;

import eu.scape_project.pt.proc.FileProcessor;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author ait
 */
public class Stream {

    private static Log LOG = LogFactory.getLog(Stream.class);
    InputStream in = null;
    OutputStream out = null;
    File file = null;

    public Stream(InputStream in) {
        this.in = in;
    }

    public Stream(OutputStream out) {
        this.out = out;
    }

    public Stream(File file) {
        this.file = file;
    }

    public InputStream getInputStream() {
        if (in != null) {
            return in;
        }
        if (file != null) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                LOG.error(ex);
            }
        }
        return null;
    }

    public OutputStream getOutputStream() {
        if (out != null) {
            return out;
        }
        if (file != null) {
            try {
                return new FileOutputStream(file);
            } catch (FileNotFoundException ex) {
                LOG.error(ex);
            }
        }
        return null;
    }

    public File getFile() {
        if (file != null) {
            return file;
        }
        // In other cases, take the input stream and push it into a temp file.
        // And delete the temp file on finalize (?)
        if (in != null ) {
            File tmpFile = null;
            try {
                tmpFile = File.createTempFile("tmp", "stream");
                
                FileOutputStream ostream = new FileOutputStream(tmpFile);
                IOUtils.copy(in, ostream);
                ostream.close();
                return tmpFile;
            } catch (IOException ex) {
                LOG.error(ex);
            }                       
            
            
        }
        if( out != null ) {
            
        }
        return null;
    }
}
