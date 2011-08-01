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

import eu.scape_project.xa.tw.util.StringConverterUtil;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * Abstract class for code templates that can be evaluated by the Velocity template
 * engine. Evaluation is performed by applying the Velocity context to the
 * source code content.
 * 
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public abstract class Code {

    private VelocityContext ctx;
    private String code;

    public Code(String templateFilePath) throws IOException {
        ctx = new VelocityContext();
        this.code = FileUtils.readFileToString(new File(templateFilePath));
    }

    /**
     * Get the code (after the Velocity evaluation is completed)
     * @return The code after the Velocity evaluation is completed
     */
    public String getCode() {
        return code;
    }

    /**
     * Add a key value pair to the Velocity context
     * @param string Key
     * @param val Value
     */
    public void put(String key, String val) {
        getCtx().put(key, val);
    }



    public void put(VelocityContext context) {
        Object[] keys = (Object[]) context.getKeys();
        for (Object obj : keys) {
            String key = (String) obj;
            this.getCtx().put(StringConverterUtil.propToVar(key), (String) context.get(key));
        }
    }

    /**
     * Apply the Velocity evaluation
     * @param context
     */
    public void evaluate() {
        StringWriter sw = new StringWriter();
        StringReader sr = new StringReader(this.getCode());
        VelocityContext cont = getCtx();
        Velocity.evaluate(cont, sw, Code.class.getName(), sr);
        this.setCode(sw.toString());
    }

    /**
     * @return the ctx
     */
    public VelocityContext getCtx() {
        return ctx;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

}
