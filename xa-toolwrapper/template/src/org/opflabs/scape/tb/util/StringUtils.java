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

import org.apache.axis2.databinding.types.URI;

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


    public static String getFilenameFromURI(URI uri, boolean preserveExtension) {
        int slashIndex = uri.toString().lastIndexOf('/');
        int dotIndex = uri.toString().lastIndexOf('.');
        String filenameWithoutExtension = null;
        if (dotIndex == -1 || preserveExtension) {
            filenameWithoutExtension = uri.toString().substring(slashIndex + 1);
        } else {
            filenameWithoutExtension = uri.toString().substring(slashIndex + 1, dotIndex);
        }
        return filenameWithoutExtension;
    }
}
