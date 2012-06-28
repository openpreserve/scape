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
package eu.scape_project.tb.lsdr.seqfileutility.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * String utilities.
 * 
 * @author Sven Schlarb https://github.com/shsdev
 * @version 0.1
 */
public class StringUtils {

    /**
     * Get a human readable byte count
     * @param bytes Number of bytes
     * @param si 1000/1024
     * @return Human readable byte count
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Get a human readable time milliseconds
     * @param millis Milliseconds 
     * @return Human readable time milliseconds
     */
    public static String humanReadableMillis(long millis) {
        return String.format("%d hours %d minutes, %d seconds",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    /**
     * Get a string list from a string.
     * @param str String
     * @param delimiter Delimiter
     * @return String list
     */
    public static List<String> getStringListFromString(String str, String delimiter) {
        List<String> dirs = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(str, delimiter);

        while (st.hasMoreTokens()) {
            dirs.add(st.nextToken());
        }
        return dirs;
    }
}
