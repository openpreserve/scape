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
package eu.scape_project.xa.tw.gen;

import eu.scape_project.core.Constants;
import eu.scape_project.xa.tw.conf.Configuration;
import eu.scape_project.xa.tw.gen.types.ErrType;
import eu.scape_project.xa.tw.toolspec.Deployment;
import eu.scape_project.xa.tw.toolspec.Deployref;
import java.io.File;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.scape_project.xa.tw.toolspec.Operation;
import eu.scape_project.xa.tw.toolspec.Service;
import eu.scape_project.xa.tw.toolspec.Toolspec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public class ToolspecValidator {

    private static Logger logger = LoggerFactory.getLogger(ToolspecValidator.class.getName());
    private Configuration ioc;
    private ArrayList<Error> errors;
    private final Toolspec toolspec;
    private boolean valid;

    /**
     * Public constructor for a ToolspecValidator from a ToolSpec and a Configuration object
     * 
     * @param toolspec the toolspec to validate
     * @param ioc a configuration object
     */
    public ToolspecValidator(Toolspec toolspec, Configuration ioc) {
        this.toolspec = toolspec;
        this.ioc = ioc;
        errors = new ArrayList<Error>();
        valid = true;
    }

    /**
     * @throws GeneratorException
     */
    public void validateWithXMLSchema() throws GeneratorException {
        try {
            // create a factory that understands namespaces and validates the XML input
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // read the XML file
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(ioc.getXmlConf()));
            // create a SchemaFactory and a Schema
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaFile = new StreamSource(ClassLoader.getSystemResourceAsStream(Constants.TOOLSPEC_SCHEMA_RESOURCE_PATH));
            Schema schema = schemaFactory.newSchema(schemaFile);
            // create a Validator object and validate the XML file
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(doc));
            logger.info("XML tool specification file successfully validated.");
        } catch (SAXException ex) {
            org.xml.sax.SAXParseException saxex = null;
            if (ex instanceof org.xml.sax.SAXParseException) {
                saxex = (org.xml.sax.SAXParseException) ex;
                logger.error("SAX parse error: " + saxex.getLocalizedMessage());
            } else {
                logger.error("SAXException:", ex);
            }
            throw new GeneratorException("SAXException occurred while validating instance.");
        } catch (IOException ex) {
            throw new GeneratorException("IOException occurred while validating instance.");
        } catch (ParserConfigurationException ex) {
            throw new GeneratorException("ParserConfigurationException occurred while validating instance.");
        }
    }

    /**
     * @throws GeneratorException
     */
    public void validate() throws GeneratorException {
        if (toolspec == null) {
            throw new GeneratorException("Tool specification is not available");
        }
        try {
            List<Service> services = toolspec.getServices().getService();
            // for each service a different maven project will be generated
            for (Service service : services) {
                validateService(service, toolspec.getVersion());
            }
        } catch (IOException ex) {
            throw new GeneratorException("IOException occurred.");
        }
    }

    private void validateService(Service service, String toolVersion) throws GeneratorException, IOException {
        List<Deployref> dks = service.getDeployto().getDeployref();
        int defaults = 0;
        for (Deployref dk : dks) {
            boolean isDefaultDeployment = dk.isDefault();
            if (isDefaultDeployment) {
                defaults++;
            }
            Deployment d = (Deployment) dk.getRef();
        }
        logger.info("Defaults:"+defaults);
        check(defaults == 0, ErrType.ERROR, "No referenced deployment is marked as default deployment for service \"" + service.getName() + "\".");
        check(defaults > 1, ErrType.ERROR, "Multiple deployment references are marked as default deployment for service \"" + service.getName() + "\".");
        checkpoint();
        List<Operation> operations = service.getOperations().getOperation();
        for (Operation operation : operations) {
            validateOperation(operation);
        }
    }

    /**
     * @return the valid
     */
    public boolean isValid() {
        return valid;
    }

    private void check(boolean condition, ErrType errType, String errMsg) {
        if (condition) {
            errors.add(new Error(errType, errMsg));
            if (errType.equals(ErrType.ERROR)) {
                this.valid = false;
            }
        }
    }

    /**
     * @throws GeneratorException
     */
    public void checkpoint() throws GeneratorException {
        if (this.errors != null && this.errors.size() > 0) {
            for (Error err : errors) {
                if (err.errType.equals(ErrType.ERROR)) {
                    logger.error(err.errMsg);
                } else if (err.errType.equals(ErrType.WARNING)) {
                    logger.warn(err.errMsg);
                }
            }
            errors = null;
            errors = new ArrayList<Error>();
        }
        if (!this.valid) {
            throw new GeneratorException("Invalid tool specification.");
        }
    }

    private void validateOperation(Operation operation) {
    }

    class Error {

        private ErrType errType;
        private String errMsg;

        public Error(ErrType errType, String errMsg) {
            this.errType = errType;
            this.errMsg = errMsg;
        }

        @Override
        public String toString() {
            return errType.toString() + ": " + errMsg;
        }
    }
}
