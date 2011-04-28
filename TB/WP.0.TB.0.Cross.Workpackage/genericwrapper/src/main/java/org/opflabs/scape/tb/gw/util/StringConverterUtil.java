/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opflabs.scape.tb.gw.util;

/**
 *
 * @author onbscs
 */
public class StringConverterUtil {

    public static String packageNameToPackagePath(String pn) {
        String pp = pn.replaceAll("\\.", "/");
        return pp;
    }

    public static String varToProp(String var) {
        String prop = var.replaceAll("_", ".").toLowerCase();
        return prop;
    }

    public static String propToVar(String var) {
        String prop = var.replaceAll("\\.", "_").toUpperCase();
        return prop;
    }

    public static String typeToFilename(String var) {
        String ret = var.replaceAll(":", "_").toLowerCase();
        return ret;
    }

}
