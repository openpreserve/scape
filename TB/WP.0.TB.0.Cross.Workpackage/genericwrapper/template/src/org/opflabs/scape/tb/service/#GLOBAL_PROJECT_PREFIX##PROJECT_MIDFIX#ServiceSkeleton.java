/*******************************************************************************
 * Copyright (c) #GLOBAL_YEAR# The #GLOBAL_PROJECT_PREFIX# Project Partners.
 *
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies
 * this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package #PROJECT_PACKAGE_NAME#.service;

import #PROJECT_PACKAGE_NAME#.Request;
import #PROJECT_PACKAGE_NAME#.RequestType;
import #PROJECT_PACKAGE_NAME#.Response;
import #PROJECT_PACKAGE_NAME#.ResponseType;
import #PROJECT_PACKAGE_NAME#.service.util.ServiceFileUtils;
import #PROJECT_PACKAGE_NAME#.util.FileUtils;
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

import #PROJECT_PACKAGE_NAME#.*;
import #PROJECT_PACKAGE_NAME#.util.*;

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

    private boolean success = false;
    private int returncode = -1;
    private int processing_returncode;
    private String processing_message;
    private String processing_log;
    private int processing_time;

    /**
     * Apply process to the input object.
     * @param inImgFile Input image file.
     * @throws IOException
     */
    private boolean process(HashMap<String, String> cliCmdKeyValPairs) {

        // Assigning values to the variables used in the command pattern.
        // If additional variables are required, they must be added in the
        // org.opflabs.scape.tb.service.CommandPatternVariables class.
        String cliCmdPattern = getValueOfServiceParameter("CLICommand");

        // Command line process
        CommandLineProcess clp =
                new CommandLineProcess(cliCmdPattern, cliCmdKeyValPairs);
        try {
            clp.init();
        } catch (IOException ex) {
            processing_message = "I/O Exception. " + ex.getMessage();
            success = false;
            return false;
        }
        clp.execute();
        success = (clp.getCode() == 0);
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
                if (clp.getErrorInputStream() != null) {
                    processing_message += "Tool error message: " + FileUtils.getStringFromInputStream(clp.getErrorInputStream());
                }
                errorlog(processing_message);
                break;
        }

        return success;
    }
    
    /**
     * Default operation of the web service. Takes the request message object
     * as input, processes data and creates the response message object. This
     * web service operation has only http references (URLs) to binary data
     * files in the request message. See determineFileType web service operation
     * where binary data are transmitted as attachments.
     * @param base64BinaryRequest Request message object
     * @return Response message object
     * @throws java.io.IOException
     */
    public Response #SERVICE_DEFAULT_OPERATION#(Request Request) {

        infolog("========= PROCESSING REQUEST =========");

        // Request message object
        RequestType requestObj = Request.getRequest();

        // Response message object
        Response Response = new Response();
        ResponseType responseObj = new ResponseType();

        // Required for copying output files
        String publicHttpAccessDir = getValueOfServiceParameter("publicHttpAccessDir");

        // Required for providing access to output files
        String publicHttpAccessUrl = getValueOfServiceParameter("publicHttpAccessUrl");

        HashMap cliCmdKeyValPairs = new HashMap<String, String>();

        // input code
        long startMillis = System.currentTimeMillis();
        process(cliCmdKeyValPairs);
        long timeMillis = System.currentTimeMillis() - startMillis;
        processing_time = (int)timeMillis;

        // output code


        Response.setResponse(responseObj);
        return Response;
    }

    /**
     * Create error message response object.
     * @param base64BinaryResponse Response message object
     * @param responseObj Response object
     * @param msg Error message
     * @return Error response message object
     */
    Response getErrorResponse(Response Response,
            ResponseType responseObj) {
        errorlog("Process terminated with error: " + processing_message);
        Response.setResponse(responseObj);
        return Response;
    }

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
