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

import eu.scape_project.xa.tw.tmpl.GenericCode;
import eu.scape_project.xa.tw.toolspec.Dataexchange;
import eu.scape_project.xa.tw.toolspec.Deployment;
import eu.scape_project.xa.tw.toolspec.Deployref;
import eu.scape_project.xa.tw.toolspec.Manager;
import eu.scape_project.xa.tw.toolspec.Operation;
import eu.scape_project.xa.tw.toolspec.Port;
import eu.scape_project.xa.tw.toolspec.Service;
import eu.scape_project.xa.tw.util.FileUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author shsdev https://github.com/shsdev
 * @version 0.3
 */
public class DeploymentCreator {

    private static Logger logger = LoggerFactory.getLogger(DeploymentCreator.class.getName());
    private String pomAbsPath;
    private Document doc;
    private Service service;
    private PropertiesSubstitutor st;

    public DeploymentCreator(String pomAbsPath, Service service, PropertiesSubstitutor st) {
        this.pomAbsPath = pomAbsPath;
        this.service = service;
        this.st = st;
    }

    public DeploymentCreator() {
    }

    /**
     * Insert data types
     */
    public void createPom() throws GeneratorException {
        File wsdlTemplate = new File(this.pomAbsPath);
        if (!wsdlTemplate.canRead()) {
            throw new GeneratorException("Unable to read pom.xml template file: " + this.pomAbsPath);
        }
        try {

            DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuildFact.newDocumentBuilder();
            doc = docBuilder.parse(this.pomAbsPath);
            NodeList profilesNodes = doc.getElementsByTagName("profiles");
            Node firstProfilesNode = profilesNodes.item(0);
            List<Deployref> dks = service.getDeployto().getDeployref();


            NodeList executionsNode = doc.getElementsByTagName("executions");
            Node thirdExecutionsNode = executionsNode.item(2);

            for (Deployref dk : dks) {
                boolean isDefaultDeployment = dk.isDefault();
                Deployment d = (Deployment) dk.getRef();


                //<profile>
                //    <id>deployment1</id>
                //    <properties>
                //        <tomcat.manager.url>http://localhost:8080/manager</tomcat.manager.url>
                //        <tomcat.user>tomcat</tomcat.user>
                //        <tomcat.password>TxF781!P</tomcat.password>
                //        <war.suffix>deployment1</war.suffix>
                //    </properties>
                //</profile>
                //<profile>
                //    <id>deployment2</id>
                //    <properties>
                //        <tomcat.manager.url>http://localhost:8080/manager</tomcat.manager.url>
                //        <tomcat.user>tomcat</tomcat.user>
                //        <tomcat.password>TxF781!P</tomcat.password>
                //        <war.suffix>deployment2</war.suffix>
                //    </properties>
                //</profile>
                Element profileElm = doc.createElement("profile");

                if (isDefaultDeployment) {
                    Element activationElm = doc.createElement("activation");
                    Element activeByDefaultElm = doc.createElement("activeByDefault");
                    activeByDefaultElm.setTextContent("true");
                    activationElm.appendChild(activeByDefaultElm);
                    profileElm.appendChild(activationElm);
                }



                firstProfilesNode.appendChild(profileElm);
                Element idElm = doc.createElement("id");
                idElm.setTextContent(d.getId());
                profileElm.appendChild(idElm);
                Element propertiesElm = doc.createElement("properties");
                profileElm.appendChild(propertiesElm);

                List<Port> ports = d.getPorts().getPort();
                String port = "8080";
                String type = "http";
                for (Port p : ports) {
                    if (p.getType().equals("http")) {
                        port = String.valueOf(p.getValue());
                        type = p.getType();
                    }
                }
                Element tomcatManagerUrlElm = doc.createElement("tomcat.manager.url");
                tomcatManagerUrlElm.setTextContent(type + "://" + d.getHost() + ":" + port + "/manager");


                propertiesElm.appendChild(tomcatManagerUrlElm);
                Element tomcatUserPropElm = doc.createElement("tomcat.user");
                Manager manager = d.getManager();
                tomcatUserPropElm.setTextContent(manager.getUser());
                propertiesElm.appendChild(tomcatUserPropElm);
                Element tomcatPasswordPropElm = doc.createElement("tomcat.password");
                tomcatPasswordPropElm.setTextContent(manager.getPassword());
                propertiesElm.appendChild(tomcatPasswordPropElm);
                Element warSuffixElm = doc.createElement("war.suffix");
                warSuffixElm.setTextContent(d.getId());
                propertiesElm.appendChild(warSuffixElm);

                //<execution>
                //    <id>package-deployment1</id>
                //    <phase>package</phase>
                //    <configuration>
                //        <classifier>deployment1</classifier>
                //        <webappDirectory>${project.build.directory}/${project.build.finalName}_deployment1</webappDirectory>
                //        <webResources>
                //            <resource>
                //                <directory>src/env/deployment1</directory>
                //            </resource>
                //        </webResources>
                //    </configuration>
                //    <goals>
                //        <goal>war</goal>
                //    </goals>
                //</execution>

                Element executionElm = doc.createElement("execution");
                thirdExecutionsNode.appendChild(executionElm);
                Element id2Elm = doc.createElement("id");
                id2Elm.setTextContent("package-" + d.getId());
                executionElm.appendChild(id2Elm);
                Element phaseElm = doc.createElement("phase");
                phaseElm.setTextContent("package");
                executionElm.appendChild(phaseElm);
                Element configurationElm = doc.createElement("configuration");
                executionElm.appendChild(configurationElm);
                Element classifierElm = doc.createElement("classifier");
                classifierElm.setTextContent(d.getId());
                configurationElm.appendChild(classifierElm);
                Element webappDirectoryElm = doc.createElement("webappDirectory");
                webappDirectoryElm.setTextContent("${project.build.directory}/${project.build.finalName}_" + d.getId());
                configurationElm.appendChild(webappDirectoryElm);
                Element webResourcesElm = doc.createElement("webResources");
                Element resourceElm = doc.createElement("resource");
                Element directoryElm = doc.createElement("directory");
                directoryElm.setTextContent("src/env/" + d.getId());
                resourceElm.appendChild(directoryElm);
                webResourcesElm.appendChild(resourceElm);
                configurationElm.appendChild(webResourcesElm);
                Element goalsElm = doc.createElement("goals");
                executionElm.appendChild(goalsElm);
                Element goalElm = doc.createElement("goal");
                goalElm.setTextContent("war");
                goalsElm.appendChild(goalElm);

                // Create different environment dependent configuration files.
                // Deployment environment dependent files will be stored in
                // src/env and will then be activated by choosing the corresponding
                // profile during the corresponding maven phase.
                // E.g. mvn tomcat:redeploy -P deployment1
                // will replace the deployment dependent files by the ones
                // available under src/env/deployment1.
                String generatedDir = st.getGenerateDir();
                String projMidfix = st.getProjectMidfix();
                String projDir = st.getProjectDirectory();
                String servDir = FileUtil.makePath(generatedDir, projDir,
                        "src/env", d.getId(), "WEB-INF/services", projMidfix,
                        "META-INF");
                FileUtils.forceMkdir(new File(servDir));

                String sxmlFile = FileUtil.makePath(generatedDir, projDir,
                        "src/main/webapp/WEB-INF/services", st.getProjectMidfix(),
                        "META-INF") + "services.xml";

                GenericCode deplDepServXmlCode = new GenericCode(sxmlFile);

                //<parameter name="cliCommand1">${clicmd}</parameter>
                //<parameter name="processingUnit">${tomcat_public_procunitid}</parameter>
                //<parameter name="publicHttpAccessDir">${tomcat_public_http_access_dir}</parameter>
                //<parameter name="publicHttpAccessUrl">${tomcat_public_http_access_url}</parameter>
                //<parameter name="serviceUrlFilter">${service_url_filter}</parameter>


                List<Operation> operations = service.getOperations().getOperation();
                for (Operation operation : operations) {
                    deplDepServXmlCode.put("cli_cmd_" + String.valueOf(operation.getOid()), operation.getCommand());
                }
                deplDepServXmlCode.put("tomcat_public_procunitid", d.getIdentifier());
                Dataexchange de = d.getDataexchange();

                deplDepServXmlCode.put("tomcat_public_http_access_dir", de.getAccessdir());
                deplDepServXmlCode.put("tomcat_public_http_access_url", de.getAccessurl());
                // TODO: filter
                //deplDepServXmlCode.put("service_url_filter", );
                deplDepServXmlCode.evaluate();

                deplDepServXmlCode.create(servDir + "services.xml");
                logger.debug("Writing: " + servDir + "services.xml");


                // source
                String htmlIndexSourcePath = FileUtil.makePath(generatedDir, projDir,
                        "src/main", "webapp") + "index.html";
                // substitution
                GenericCode htmlSourceIndexCode = new GenericCode(htmlIndexSourcePath);
                htmlSourceIndexCode.put("service_description", service.getDescription());
                htmlSourceIndexCode.put("tomcat_public_host", d.getHost());
                htmlSourceIndexCode.put("tomcat_public_http_port", port);
                // target
                String htmlIndexDir = FileUtil.makePath(generatedDir, projDir,"src/env", d.getId());
                FileUtils.forceMkdir(new File(htmlIndexDir));
                String htmlIndexTargetPath = FileUtil.makePath(generatedDir, projDir,
                        "src/env", d.getId()) + "index.html";
                htmlSourceIndexCode.create(htmlIndexTargetPath);


                // source
                String wsdlSourcePath = FileUtil.makePath(generatedDir, projDir,
                        "src/main", "webapp") + st.getProjectMidfix()+".wsdl";
                // substitution
                GenericCode wsdlSourceCode = new GenericCode(wsdlSourcePath);
                wsdlSourceCode.put("tomcat_public_host", d.getHost());
                wsdlSourceCode.put("tomcat_public_http_port", port);
                // target
                String wsdlDir = FileUtil.makePath(generatedDir, projDir,"src/env", d.getId());
                FileUtils.forceMkdir(new File(wsdlDir));
                String wsdlTargetPath = FileUtil.makePath(generatedDir, projDir,
                        "src/env", d.getId()) + st.getProjectMidfix()+".wsdl";
                wsdlSourceCode.create(wsdlTargetPath);


            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            FileOutputStream fos = new FileOutputStream(this.pomAbsPath);
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
            fos.close();
        } catch (Exception ex) {
            logger.error("An exception occurred: " + ex.getMessage());
        }
    }
}
