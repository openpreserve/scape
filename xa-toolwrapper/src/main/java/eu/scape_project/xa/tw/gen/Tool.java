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

import java.util.StringTokenizer;

/**
 * Tool
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public class Tool {

    private String name;
    private String version;
    private String midfix;
    private String directory;

    private Tool() {
    }

    public Tool(String name, String version) {
        this.name = getCleanName(name);
        this.version = getCleanVersion(version);
        midfix = getMidfixByNameAndVersion();
        directory = getMidfixToLowercase();
    }

    private String getCleanName(String name) {
        String projectName = name.replaceAll("[^A-Za-z0-9]", "");
        return projectName;
    }

    private String getCleanVersion(String v) {
        String vrs = v.replaceAll("[^0-9]", "");
        vrs = vrs.replaceAll("\\.", "");
        return vrs;
    }

    private String getMidfixByNameAndVersion() {
        String mdf = name+version;
        return mdf;
    }

    private String getMidfixToLowercase() {
        return midfix.toLowerCase();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * @return the midfix
     */
    public String getMidfix() {
        return midfix;
    }

}
