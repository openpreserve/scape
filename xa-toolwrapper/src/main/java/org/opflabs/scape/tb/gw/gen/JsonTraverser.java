/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opflabs.scape.tb.gw.gen;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author onbscs
 */
public abstract class JsonTraverser {

    private static Logger logger = Logger.getLogger(JsonTraverser.class);
    boolean isFirstLevel;
    protected JsonNode rootJsn;
    boolean isListCurrJsn;
    boolean isRestrCurrJsn;

    public JsonTraverser() {
        isFirstLevel = true;
        isListCurrJsn = false;
        isRestrCurrJsn = false;
    }

    public void apply(String jsonPath) throws GeneratorException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(jsonPath);
            // Json object mapper
            ObjectMapper mapper = new ObjectMapper();
            // Root node
            rootJsn = mapper.readTree(fis);
            traverse(rootJsn);
        } catch (Exception ex) {
            logger.error("Unable to read json file: " + jsonPath);
            throw new GeneratorException();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                logger.error("Unable to close input stream for file: " + jsonPath);
                throw new GeneratorException();
            }
        }
    }

    /**
     * Traverse the Json tree
     * @param jsn Root Json node
     */
    private void traverse(JsonNode jsn) {
        Iterator<JsonNode> jsnIt = jsn.getElements();
        for (Iterator<String> itstr = jsn.getFieldNames(); itstr.hasNext();) {
            String currStr = itstr.next();
            logger.info(currStr);
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

    protected abstract void processLeafFirstLevel(String nodeName, JsonNode currJsn, String dataType);
}
