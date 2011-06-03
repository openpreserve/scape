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

import java.io.File;
import org.apache.log4j.Logger;
import org.opflabs.scape.tb.gw.util.FileUtil;

/**
 * Inserter interface
 * @author onbscs
 * @version 0.1
 */
public class Inserter {

    private static Logger logger = Logger.getLogger(JavaTypesCreator.class);

    private String point;
    private String content;

    public Inserter(String point, String content) {
        this.point = point;
        this.content = content;
    }
   /**
     * Apply substitution
     * @param srcFileAbsPath Source file
     * @param trgtFileAbsPath Target file
     */
    public void insert(String srcFileAbsPath, String trgtFileAbsPath) throws GeneratorException {
        File srcFile = new File(srcFileAbsPath);
        StringBuilder strBuf = new StringBuilder();
        if (srcFile.exists()) {
            logger.debug("Template file " + srcFile.getPath() + " exists.");
        } else {
            logger.error("Unable to find template file " + srcFile.getPath() + "");
        }
        // Service source
        String serviceSource = FileUtil.readTxtFileIntoString(srcFile);
        if (serviceSource == null) {
            throw new GeneratorException("Source does not exist.");
        }
        strBuf.append(point);
        strBuf.append("\n");
        strBuf.append(content);

        serviceSource = serviceSource.replaceAll(point, strBuf.toString());

        // Target file
        File targetFile = FileUtil.writeStringToFile(serviceSource, (trgtFileAbsPath));
        if (targetFile.exists()) {
            logger.debug("Target file " + targetFile.getPath() + " created.");
        } else {
            logger.error("Unable to create target file " + targetFile.getPath() + "");
        }
    }
}
