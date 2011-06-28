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

package #GLOBAL_PACKAGE_NAME#.service.util;

import java.io.File; 
import java.io.IOException;
import javax.activation.DataHandler;
import #GLOBAL_PACKAGE_NAME#.util.FileUtils;
import javax.activation.FileDataSource;

/**
 * The client file utils class is a collection of static methods for common
 * file operations, like conversion from a file to a base64 binary and vice
 * versa.
 *
 * @author #GLOBAL_PROJECT_PREFIX# Project Development Team
 * @version #GLOBAL_WRAPPER_VERSION#
 */
public class ServiceFileUtils {

    /**
     * Get a temporary file from a base64 binary data object.
     * @param binaryData Binary data object
     * @param filePrefix File prefix of the temporary file.
     * @param fileExt File extension of the temporary file.
     * @return Temporary file
     * @throws java.io.IOException
     */
//    public static File getTmpFileFromBase64Binary(final Base64Binary binaryData,
//            String filePrefix, String fileExt) throws IOException {
//        DataHandler dh = binaryData.getBase64Binary();
//        return FileUtils.writeInputStreamToTmpFile(
//                dh.getInputStream(),
//                filePrefix, fileExt);
//    }

    /**
     * Get a base64 binary data object from an input file.
     * @param file Input file
     * @return Base64 binary data object
     */
//    public static Base64Binary getBase64BinaryFromFile(final File file) {
//        Base64Binary base64Binary =
//                new Base64Binary();
//        ContentType_type0 param =
//                new ContentType_type0();
//        // Creating a javax.activation.FileDataSource from the input file.
//        FileDataSource fileDataSource = new FileDataSource(file);
//        DataHandler dataHandler = new DataHandler(fileDataSource);
//        base64Binary.setBase64Binary(dataHandler);
//        param.setContentType_type0(dataHandler.getContentType());
//        base64Binary.setContentType(param);
//        return base64Binary;
//    }

}
