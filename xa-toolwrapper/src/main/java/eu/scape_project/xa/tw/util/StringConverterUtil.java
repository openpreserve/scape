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

package eu.scape_project.xa.tw.util;

/**
 * StringConverterUtil
 * @author shsdev https://github.com/shsdev
 * @version 0.3
 */
public class StringConverterUtil {

   /**
     * Package name to namespace
     *
     * @param pn e.g. eu.scape_project.pc.services
     * @return e.g. http://scape-project.eu/pc/services
     */
    public static String packageNameToNamespace(String pn) {

        String newpn = pn;
        int ind = newpn.indexOf(".");
        String tld = newpn.substring(0, ind);
        newpn = newpn.substring(ind + 1, newpn.length());
        ind = newpn.indexOf(".");
        String domain = newpn.substring(0, ind);
        domain = domain.replace("_", "-");
        newpn = newpn.substring(ind + 1, newpn.length());
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(domain);
        sb.append(".");
        sb.append(tld);
        sb.append("/");
        while (newpn.indexOf(".") != -1) {
            ind = newpn.indexOf(".");
            String path = newpn.substring(0, ind);
            sb.append(path);
            sb.append("/");
            newpn = newpn.substring(ind + 1, newpn.length());
        }
        if(!newpn.isEmpty())
            sb.append(newpn);
        return (sb.toString());

    }


    /**
     * Creates a file path from a package name
     * @param pn the Java package name
     * @return the file path
     */
    public static String packageNameToPackagePath(String pn) {
        return pn.replaceAll("\\.", "/");
    }

    /**
     * Converts variable names to property names, in an arbitrary fashion (replaces all occurrences of "_" in the var name with ".".
     * @param var the variable name;
     * @return property name
     */
    public static String varToProp(String var) {
        return var.replaceAll("_", ".");
    }

    /**
     * Converts property names to variable names, in an arbitrary fashion (replaces all occurrences of "." in the var name with "_".
     * @param var the property name
     * @return the variable name
     */
    public static String propToVar(String var) {
        return var.replaceAll("\\.", "_");
    }

    /**
     * Converts type names to file names, substitutes ":" for "_"
     * @param var the type name
     * @return the file name
     */
    public static String typeToFilename(String var) {
        return var.replaceAll(":", "_").toLowerCase();
    }

}
