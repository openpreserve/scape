/*
 *  Copyright 2011 The SCAPE Project Consortium.
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
package eu.scape_project.xa.tw.tmpl;

import java.io.IOException;

/**
 * Template-based service code generator.
 * The service code can be evaluated by the Velocity template
 * engine. Evaluation is performed by applying the Velocity context to the
 * source code content.
 * @author shsdev https://github.com/shsdev
 * @version 0.3
 */
public class ResultElementCode extends Code {

    /**
     * Constructor for a service code instance
     * @param filePath Path to template file
     * @throws IOException Exception while reading the template file
     */
    public ResultElementCode(String filePath) throws IOException {
        super(filePath);
    }

}
