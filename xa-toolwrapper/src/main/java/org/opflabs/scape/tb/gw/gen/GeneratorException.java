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
package org.opflabs.scape.tb.gw.gen;

import org.apache.log4j.Logger;

/**
 * GeneratorException
 * @author onbscs
 * @version 0.1
 */
public class GeneratorException extends Exception {
    private static Logger logger = Logger.getLogger(GeneratorException.class);

    public GeneratorException() {
    }

    public GeneratorException(String message) {
        super(message);
        logger.error(message);
    }
}
