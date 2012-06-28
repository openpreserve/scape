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

package eu.scape_project.tb.wc.archd.hdreader;

import java.io.*;
import java.util.Date;
import org.apache.hadoop.io.Writable;

/**
 *
 * @author shsdev https://github.com/shsdev
 * @version 0.2
 */
public class ArcRecord implements Writable {
    
    private String url = null;
    private String mimeType = null;
    private Date date = null;
    private int httpReturnCode = -1;
    private int length = 0;
    private String type;
    private byte[] contents = new byte[0];

    public void clear(){
        mimeType = null;
        date = null;
        httpReturnCode = -1;
        length = 0;
        //We do not clear contents, to prevent memory thrashing
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setHttpReturnCode(int httpReturnCode) {
        this.httpReturnCode = httpReturnCode;
    }

    public void setContents(InputStream input, int length) throws IOException {
        this.length = length;
        ensureSpace();
        int offset = 0;
        while (true){
            int read = input.read(contents, offset, length - offset);
            if (read > 0){
                offset +=read;
            } else {
                this.length = offset;
                break;
            }
        }

    }

    public void setType(String type) {
        this.type = type;
    }

    private synchronized void ensureSpace() {
        if (length > contents.length){
            //System.out.println("Upgrading array from "+contents.length+" to "+(2*length));
            contents = new byte[2*length];
        }
    }


    public String getUrl() {
        return url;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Date getDate() {
        return date;
    }

    public int getHttpReturnCode() {
        return httpReturnCode;
    }

    public int getLength() {
        return length;
    }

    public InputStream getContents(){
        return new ByteArrayInputStream(contents,0,length);
    }

    public String getType() {
        return type;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(url);
        out.writeUTF(mimeType);
        out.writeLong(date.getTime());
        out.writeInt(httpReturnCode);
        out.writeUTF(type);
        out.write(length);
        out.write(contents,0,length);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        url = in.readUTF();
        mimeType = in.readUTF();
        date = new Date(in.readLong());
        httpReturnCode = in.readInt();
        type = in.readUTF();
        length = in.readInt();
        ensureSpace();
        in.readFully(contents,0,length);
    }

}
