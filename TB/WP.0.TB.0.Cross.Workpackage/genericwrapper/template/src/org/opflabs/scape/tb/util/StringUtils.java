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
package #PROJECT_PACKAGE_NAME#.util;

/**
 * These are generic string utilities.
 *
 * @author #GLOBAL_PROJECT_PREFIX# Project Development Team
 * @version #GLOBAL_WRAPPER_VERSION#
 */
public final class StringUtils {

    
    /**
     * Empty private constructor avoids instantiation.
     */
    private StringUtils() {
    }


    /**
     * Get file extension
     * @param path Path of the file
     * @return file extension
     */
    public static String getFileExtension(String path) {
        int dot = path.lastIndexOf(".");
        return path.substring(dot + 1);
    }
}
