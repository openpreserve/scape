/*
 * Copyright 2011 The SCAPE Project Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package eu.scape_project.xa.tw.gen;

import eu.scape_project.xa.tw.gen.types.IOType;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * The JSON traverser is an abstract class that provides methods for traversing
 * the whole JSON tree. 
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public abstract class JsonTraverser {

    private static Logger logger = LoggerFactory.getLogger(JsonTraverser.class.getName());
    
    protected JsonNode rootJsn;
    protected IOType iotype;
    boolean isFirstLevel;
    boolean isListCurrJsn;
    boolean isRestrCurrJsn;

    public JsonTraverser() {
        isListCurrJsn = false;
        isRestrCurrJsn = false;
    }

    public void apply(String jsonPath, IOType iotype) throws GeneratorException, IOException {

        
        
        isFirstLevel = true;
        this.iotype = iotype;
        
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(jsonPath);
            // Json object mapper
            ObjectMapper mapper = new ObjectMapper();
            // Root node
            rootJsn = mapper.readTree(fis);
            traverse(rootJsn);
        } catch (IOException ex) {
            throw new GeneratorException("Unable to read json file: " + jsonPath);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                throw new GeneratorException("Unable to close input stream for file: " + jsonPath);
            }
        }
    }

    /**
     * Traverse the Json tree
     * @param jsn Root Json node
     */
    private void traverse(JsonNode jsn) throws GeneratorException {
        Iterator<JsonNode> jsnIt = jsn.getElements();
        for (Iterator<String> itstr = jsn.getFieldNames(); itstr.hasNext();) {
            String currStr = itstr.next();
            logger.debug("Processing Json node: "+currStr);
            JsonNode currJsn = jsnIt.next();
            JsonNode cardJsn = currJsn.get("Cardinality");
            isListCurrJsn = (cardJsn != null && cardJsn.getTextValue().equalsIgnoreCase("list"));
            isRestrCurrJsn = (currJsn.get("Restriction") != null);
            if (currJsn.has("Datatype")) {
                String dataType = currJsn.findValue("Datatype").getTextValue();
                if (isFirstLevel) {
                    processLeafFirstLevel(currStr, currJsn, dataType);
                }
                processLeaf(currStr, currJsn, dataType);
            } else {
                if (isFirstLevel) {
                    processNodeFirstLevel(currStr, currJsn);
                }
                processNode(currStr, currJsn);
                traverse(currJsn);
            }

        }
        isFirstLevel = false;
    }

    protected abstract void processNode(String nodeName, JsonNode currJsn);

    protected abstract void processLeaf(String nodeName, JsonNode currJsn, String dataType);

    protected abstract void processNodeFirstLevel(String nodeName, JsonNode currJsn);

    protected abstract void processLeafFirstLevel(String nodeName, JsonNode currJsn, String dataType)  throws GeneratorException;
}
