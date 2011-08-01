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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.scape_project.xa.tw.gen.GeneratorException;
import eu.scape_project.xa.tw.gen.PropertiesSubstitutor;

/**
 * Operations iterator
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public class OperationsIterator implements Iterator<Integer>, Iterable<Integer> {

    private ArrayList<Integer> operationNumbers;
    private static Logger logger = LoggerFactory.getLogger(OperationsIterator.class.getName());
    private int currind = 0;

    public OperationsIterator(PropertiesSubstitutor st) throws GeneratorException {
        operationNumbers = new ArrayList<Integer>();
        HashMap<String,String> kvp = (HashMap<String, String>) st.getPropertyUtils().getKeyValuePairs();
        if(kvp.isEmpty()) {
            logger.error("Property list is empty");
            throw new GeneratorException("No properties defined.");
        }
        Set<String> keys = kvp.keySet();
        Iterator itr = keys.iterator();
        while(itr.hasNext()) {
            String operation = (String) itr.next();
            if(operation.startsWith("service.operation") && operation.contains("clicmd")) {
                Integer number = null;
                String numStr = operation.substring(18, 19);
                try {
                    number = Integer.valueOf(numStr);
                } catch(NumberFormatException ex) {
                    logger.error("Unable to determine operation number for "
                            + "operation: \""+operation+"\", \""+numStr+"\" is not a valid number");
                    throw new GeneratorException("An error occurred while trying to determine the operation number.");
                }
                logger.debug("Operation defined by property "+operation+ " identified as operation number "+numStr+".");
                operationNumbers.add(number);
            }

        }
    }

    @Override
    public boolean hasNext() {
            return (currind < operationNumbers.size());
    }

    @Override
    public Integer next() {
        Integer on = operationNumbers.get(currind);
        currind++;
        return on;
    }

    @Override
    public void remove() {
        operationNumbers.remove(currind);
    }

    @Override
    public Iterator<Integer> iterator() {
        return this; 
    }


}
