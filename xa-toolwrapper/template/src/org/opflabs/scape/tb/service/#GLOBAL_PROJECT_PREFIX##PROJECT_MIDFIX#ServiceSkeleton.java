/*
 *  Copyright (c) #GLOBAL_YEAR# The #GLOBAL_PROJECT_PREFIX# Project Partners.
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

package #GLOBAL_PACKAGE_NAME#.service;

// <!-- insert_mark:response_request_packages --> //

import #GLOBAL_PACKAGE_NAME#.service.util.ServiceFileUtils;
import #GLOBAL_PACKAGE_NAME#.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.log4j.Logger;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.description.Parameter;

import #GLOBAL_PACKAGE_NAME#.*;
import #GLOBAL_PACKAGE_NAME#.util.*;

/**
 * This class provides the operations of the #GLOBAL_PROJECT_PREFIX# #PROJECT_TITLE# service.
 * The operations correspond to those defined in #GLOBAL_PROJECT_PREFIX##PROJECT_MIDFIX#ServiceSkeleton.wsdl.
 *
 * @author #GLOBAL_PROJECT_PREFIX# Project Development Team
 * @version #GLOBAL_WRAPPER_VERSION#
 */
public class #GLOBAL_PROJECT_PREFIX##PROJECT_MIDFIX#ServiceSkeleton {

    /**
     * Static logger variable.
     */
    private static Logger logger =
            Logger.getLogger(#GLOBAL_PROJECT_PREFIX##PROJECT_MIDFIX#ServiceSkeleton.class);

    /*
     * These variables can be mapped to an output port using the OutputMapping
     * property in the output configuration file (data types must be the same).
     *
     */
    private boolean processing_success;
    private int processing_returncode;
    private String processing_message;
    private String processing_log;
    private int processing_time;
    private String processing_unitid;

    {
        processing_success = false;
        processing_returncode = -1;
        processing_message = "";
        processing_log = "";
        processing_time = 0;
        processing_unitid = "http://null";
    }

    /**
     * Apply process to the input object.
     * @param inImgFile Input image file.
     * @throws IOException
     */
    private boolean process(HashMap<String, String> cliCmdKeyValPairs, int opid) {

        // Assigning values to the variables used in the command pattern.
        // If additional variables are required, they must be added in the
        // org.opflabs.scape.tb.service.CommandPatternVariables class.
        String cliCmdPattern = getValueOfServiceParameter("cliCommand"+opid);

        // Command line process
        CommandLineProcess clp =
                new CommandLineProcess(cliCmdPattern, cliCmdKeyValPairs);
        try {
            clp.init();
        } catch (IOException ex) {
            processing_message = "I/O Exception. " + ex.getMessage();
            processing_success = false;
            return false;
        }
        clp.execute();
        processing_success = (clp.getCode() == 0);
        processing_returncode = clp.getCode();
        processing_log += clp.getProcessingLog();

        // The return codes of the tool should be documented here by
        // creating a case for each code and assigning the corresponding
        // message.
        switch (processing_returncode) {
            case 0:
                processing_message = "Process finished successfully with code 0";
                infolog(processing_message);
                break;
            case -1:
                processing_message = "Process result is undefined with code -1";
                errorlog(processing_message);
                break;
            default:
                processing_message = "Process failed with error code " + processing_returncode + ". ";
//                if (clp.getErrorInputStream() != null) {
//                    processing_message += "Tool error message: " + FileUtils.getStringFromInputStream(clp.getErrorInputStream());
//                }
                errorlog(processing_message);
                break;
        }

        return processing_success;
    }

    // <!-- insert_mark:operations_code --> //
    
    /**
     * Get the value of a service parameter defined in the
     * resources/services.xml.
     * @param parm Parameter defined in the services.xml
     * @return Value of the parameter
     */
    private String getValueOfServiceParameter(String parm) {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        Parameter patternParameter = msgCtx.getParameter(parm);
        String ptn = (String) patternParameter.getValue();
        infolog("Parameter " + parm + " from services.xml: " + ptn);
        return ptn;
    }

    private void infolog(String msg) {
        processing_log += msg + ".\n";
        logger.info(msg);
    }

    private void errorlog(String msg) {
        processing_log += "ERROR: " + msg + ".\n";
        logger.error(msg);
    }
 }
