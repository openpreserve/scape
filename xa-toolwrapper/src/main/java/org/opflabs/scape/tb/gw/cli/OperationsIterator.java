/*
 *  Copyright 2011 IMPACT (www.impact-project.eu)/SCAPE (www.scape-project.eu)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.opflabs.scape.tb.gw.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;
import org.opflabs.scape.tb.gw.gen.GeneratorException;
import org.opflabs.scape.tb.gw.gen.ProjectPropertiesSubstitutor;

/**
 * Operations iterator
 * @author onbscs
 * @version 0.1
 */
class OperationsIterator implements Iterator<Integer>, Iterable<Integer> {

    private ArrayList<Integer> operationNumbers;
    private static Logger logger = Logger.getLogger(OperationsIterator.class);
    private int currind = 0;

    OperationsIterator(ProjectPropertiesSubstitutor st) throws GeneratorException {
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
                logger.info("Operation number "+numStr+" added.");
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
