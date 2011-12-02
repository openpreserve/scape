/*
 *  Copyright 2011 The SCAPE Project Consortium.
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
package eu.scape_project.xa.tw.tmpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Template-based service code generator.
 * The service code can be evaluated by the Velocity template
 * engine. Evaluation is performed by applying the Velocity context to the
 * source code content.
 * @author shsdev https://github.com/shsdev
 * @version 0.3
 */
public class ServiceCode extends Code {
    
    /** Logger */
    private static Logger logger = LoggerFactory.getLogger(ServiceCode.class.getName());

    private List<OperationCode> operations;

    /**
     * Constructor for a service code instance
     * @param templateFilePath Path to template file
     * @throws IOException Exception while reading the template file
     */
    public ServiceCode(String filePath) throws IOException {
        super(filePath);
        operations = new ArrayList<OperationCode>();
    }

    /**
     * @return the operationSnippets
     */
    public List<OperationCode> getOperations() {
        return operations;
    }

    /**
     * @param operationSnippet the operationSnippets to add
     */
    public void addOperation(OperationCode operationSnippet) {
        this.operations.add(operationSnippet);
    }

    /**
     * Add a list of operations to the Velocity context
     * @param string Key
     * @param operations List of operations 
     */
    public void put(String string, List<OperationCode> operations) {
        getCtx().put(string, operations);
    }

    public void create(String targetFilePath) throws IOException {
        this.evaluate();
        //logger.debug("Template evaluation result:\n" + this.getCode());
        File targetFile = new File(targetFilePath);
        org.apache.commons.io.FileUtils.writeStringToFile(targetFile, this.getCode());
    }
}
