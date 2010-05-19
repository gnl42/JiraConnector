/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/util/XMLPrinter.java,v 1.1 2004/08/02 15:45:49 unico Exp $
 * $Revision: 1.1 $
 * $Date: 2004/08/02 15:45:49 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */ 

package org.apache.webdav.lib.util;

import java.io.IOException;
import java.io.Writer;

/**
 * XMLPrinter helper class.
 */
public class XMLPrinter {
    
    
    // -------------------------------------------------------------- Constants
    
    
    /**
     * Opening tag.
     */
    public static final int OPENING = 0;
    
    
    /**
     * Closing tag.
     */
    public static final int CLOSING = 1;
    
    
    /**
     * Element with no content.
     */
    public static final int NO_CONTENT = 2;
    
    
    // ----------------------------------------------------- Instance Variables
    
    
    /**
     * Buffer.
     */
    protected StringBuffer buffer = new StringBuffer();
    
    
    /**
     * Writer.
     */
    protected Writer writer = null;
    
    
    // ----------------------------------------------------------- Constructors
    
    
    /**
     * Constructor.
     */
    public XMLPrinter() {
    }
    
    
    /**
     * Constructor.
     */
    public XMLPrinter(Writer writer) {
        this.writer = writer;
    }
    
    
    // --------------------------------------------------------- Public Methods
    
    
    /**
     * Retrieve generated XML.
     * 
     * @return String containing the generated XML
     */
    public String toString() {
        return buffer.toString();
    }
    
    
    /**
     * Write property to the XML.
     * 
     * @param namespace Namespace
     * @param namespaceInfo Namespace info
     * @param name Property name
     * @param value Property value
     */
    public void writeProperty(String namespace, String namespaceInfo, 
                              String name, String value, boolean cdata) {
        writeElement(namespace, namespaceInfo, name, OPENING);
        if (cdata)
            writeData(value);
        else
            writeText(value);
        writeElement(namespace, namespaceInfo, name, CLOSING);
    }
    
    
    /**
     * Write property to the XML.
     * 
     * @param namespace Namespace
     * @param namespaceInfo Namespace info
     * @param name Property name
     * @param value Property value
     */
    public void writeProperty(String namespace, String namespaceInfo, 
                              String name, String value) {
        writeProperty(namespace, namespaceInfo, name, value, false);
    }
    
    
    /**
     * Write property to the XML.
     * 
     * @param namespace Namespace
     * @param name Property name
     * @param value Property value
     */
    public void writeProperty(String namespace, String name, String value) {
        writeProperty(namespace, null, name, value);
    }
    
    
    /**
     * Write property to the XML.
     * 
     * @param namespace Namespace
     * @param name Property name
     */
    public void writeProperty(String namespace, String name) {
        writeElement(namespace, name, NO_CONTENT);
    }
    
    
    /**
     * Write an element.
     * 
     * @param name Element name
     * @param namespace Namespace abbreviation
     * @param type Element type
     */
    public void writeElement(String namespace, String name, int type) {
        writeElement(namespace, null, name, type);
    }
    
    
    /**
     * Write an element.
     * 
     * @param namespace Namespace abbreviation
     * @param namespaceInfo Namespace info
     * @param name Element name
     * @param type Element type
     */
    public void writeElement(String namespace, String namespaceInfo, 
                             String name, int type) {
        if ((namespace != null) && (namespace.length() > 0)) {
            switch (type) {
            case OPENING:
                if ((namespaceInfo != null) && (namespaceInfo.length() > 0)) {
                    buffer.append("<" + namespace + ":" + name + " xmlns:" 
                                  + namespace + "=\"" 
                                  + namespaceInfo + "\">");
                } else {
                    buffer.append("<" + namespace + ":" + name + ">");
                }
                break;
            case CLOSING:
                buffer.append("</" + namespace + ":" + name + ">");
                break;
            case NO_CONTENT:
            default:
                if ((namespaceInfo != null)  && (namespaceInfo.length() > 0)) {
                    buffer.append("<" + namespace + ":" + name + " xmlns:" 
                                  + namespace + "=\"" 
                                  + namespaceInfo + "\"/>");
                } else {
                    buffer.append("<" + namespace + ":" + name + "/>");
                }
                break;
            }
        } else {
            switch (type) {
            case OPENING:
                if ((namespaceInfo != null) && (namespaceInfo.length()>0)) {
                    buffer.append("<" + name + " xmlns=\"" + namespaceInfo 
                                  + "\">");
                } else {
                    buffer.append("<" + name + ">");
                }
                break;
            case CLOSING:
                buffer.append("</" + name + ">");
                break;
            case NO_CONTENT:
            default:
                if ((namespaceInfo != null) && (namespaceInfo.length()>0)) {
                    buffer.append("<" + name + " xmlns=\"" + namespaceInfo 
                                  + "\"/>");
                } else {
                    buffer.append("<" + name + "/>");
                }
                break;
            }
        }
    }
    
    
    /**
     * Write text.
     * 
     * @param text Text to append
     */
    public void writeText(String text) {
        buffer.append(text);
    }
    
    
    /**
     * Write data.
     * 
     * @param data Data to append
     */
    public void writeData(String data) {
        buffer.append("<![CDATA[");
        buffer.append(data);
        buffer.append("]]>");
    }
    
    
    /**
     * Write XML Header.
     */
    public void writeXMLHeader() {
        buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
    }
    
    
    /**
     * Send data and reinitializes buffer.
     */
    public void sendData()
        throws IOException {
        if (writer != null) {
            writer.write(buffer.toString());
            buffer = new StringBuffer();
        }
    }
    
    
}
