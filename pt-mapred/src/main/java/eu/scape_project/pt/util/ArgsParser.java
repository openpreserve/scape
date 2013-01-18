/*
 * Copyright 2013 ait.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.scape_project.pt.util;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parses a commandline for setParameters, values and redirect symbols
 *
 * @author Matthias Rella, DME-AIT
 */
public class ArgsParser {

    private static Log LOG = LogFactory.getLog(ArgsParser.class);

    /**
     * Set of parameters which are recognized by this parser.
     */
    private Set<String> setParameters = new HashSet<String>();

    /**
     * Map of recognized key-value pairs.
     */
    private Map<String, String> mapArguments = new HashMap<String, String>();

    /**
     * Tokenizer which reads input command line.
     */
    private StreamTokenizer tokenizer;

    /**
     * File name after "&lt" in command line.
     */
    private String strStdinFile = "";

    /**
     * File name after "&gt" in command line.
     */
    private String strStdoutFile = "";

    /**
     * Public interface for parsing a command line. The form of the
     * command line should be (--key="value")* (&lt stdinfile)? (&gt stdoutfile)?.
     * Results can be fetched via getters.
     * @param String strCmdLine input String
     * @throws IOException 
     */
    public void parse(String strCmdLine) throws IOException {

        mapArguments = new HashMap<String, String>();
        strStdinFile = "";
        strStdoutFile = "";
        tokenizer = new StreamTokenizer(
                new StringReader(strCmdLine));

        root();
    }

    /**
     * Reads next token from Tokenizer and increases column number
     * @return
     * @throws IOException 
     */
    private int nextToken() throws IOException {
        //colnr += tokenizer.sval != null ? tokenizer.sval.length() : 1;
        int token = tokenizer.nextToken();
        LOG.debug("tokenizer.sval = " + tokenizer.sval + ", token = " + token );
        return token;
    }

    /**
     * Matches (--key="value")* (< stdin)? (> stdout)?
     *
     * @throws IOException
     */
    private void root() throws IOException {
        while (nextToken() == '-') {
            pair();
        }
        if (tokenizer.ttype == '<') {
            stdin();
            nextToken();
        }
        if (tokenizer.ttype == '>') {
            stdout();
        }
    }

    /**
     * Matches -key="value"
     *
     * @throws IOException
     */
    private void pair() throws IOException {
        String key = null;
        String value = null;
        if (nextToken() == '-') {
            if (nextToken() == StreamTokenizer.TT_WORD
                    && setParameters.contains(tokenizer.sval)) {
                key = tokenizer.sval;
            } else {
                LOG.error("unrecognized token, expecting key word");
                return;
            }
            if (nextToken() == '=') {
                LOG.debug("= found");
                if (nextToken() == '"') {
                    LOG.debug("\" found");
                    LOG.debug("value found");
                    value = tokenizer.sval;
                    mapArguments.put(key, value);
                    return;
                }
                else
                    LOG.error("unrecognized token, expecting '\"'");
            }
            else
                LOG.error("unrecognized token, expecting '='");
        }
        LOG.error("unrecognized token "
                + (tokenizer.ttype >= 32 ? (char)tokenizer.ttype : ""));

    }

    /**
     * Matches a string (quoted or not)
     * @throws IOException 
     */
    private void stdin() throws IOException {
        LOG.debug("stdin");
        nextToken();
        strStdinFile = tokenizer.sval;
    }

    /**
     * Matches a string (quoted or not)
     * @throws IOException 
     */
    private void stdout() throws IOException {
        LOG.debug("stdout");
        nextToken();
        strStdoutFile = tokenizer.sval;
    }

    /**
     * Sets parameters (keys) to be recognized by parser.
     * @param parameters 
     */
    public void setParameters(Set<String> parameters) {
        this.setParameters = parameters;
    }

    /**
     * Gets recognized arguments (key-value pairs)
     * @return Map mapArguments
     */
    public Map<String, String> getArguments() {
        return this.mapArguments;
    }

    /**
     * Gets recognized stdin file name
     * @return String
     */
    public String getStdinFile() {
        return this.strStdinFile == "" ? null : "";
    }

    /**
     * Gets recognized stdout file name
     * @return String
     */
    public String getStdoutFile() {
        return this.strStdoutFile == "" ? null : "";
    }


}
