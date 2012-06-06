/*
 *  Copyright 2012 The SCAPE Project Consortium.
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

package eu.scape_project.tb.lsdr.seqfileutility;

import org.apache.hadoop.io.SequenceFile;

/**
 * Compression Type - the compression used for creating the sequence file.
 * 
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public final class CompressionType {

    /**
     * Get the compression used for creating the sequence file.
     * @param compression String of the compression type
     * @return Compression type
     */
    public static SequenceFile.CompressionType get(String compression) {
        if(compression == null)
            return SequenceFile.CompressionType.BLOCK;
        if(compression.equalsIgnoreCase("NONE")) {
            return SequenceFile.CompressionType.NONE;
        } else if(compression.equalsIgnoreCase("RECORD")) {
            return SequenceFile.CompressionType.RECORD;
        } else if(compression.equalsIgnoreCase("BLOCK")) {
            return SequenceFile.CompressionType.BLOCK;
        } else {
            // default
            return SequenceFile.CompressionType.BLOCK;
        }
    }
}
