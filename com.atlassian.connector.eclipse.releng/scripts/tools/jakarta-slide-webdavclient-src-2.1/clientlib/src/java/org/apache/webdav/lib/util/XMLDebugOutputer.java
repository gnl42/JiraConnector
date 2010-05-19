/*
 * $Header: 
 * $Revision: 
 * $Date: 
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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import org.xml.sax.InputSource;



import java.util.StringTokenizer;

/**
 * XMLDebugOutputer helper class.
 */
public class XMLDebugOutputer {
    
    // -------------------------------------------------------------- Constants
    
    // blanks for indent (80 char)                  
    private final static String INDENT = "                                                                                ";
    private int tabWidth = 3;    


    // ----------------------------------------------------- Instance Variables
    
    // indicator of debug mode
    private boolean debug = false;
    
    
    // local parser
    private DocumentBuilderFactory dbf=null;
    private DocumentBuilder         db=null;
            

    // ----------------------------------------------------------- Constructors
    
    /**
     * Constructor
     */
    public XMLDebugOutputer() {

        try {
             dbf=DocumentBuilderFactory.newInstance();
             db =dbf.newDocumentBuilder();
            
        } catch (ParserConfigurationException e) {
        }

    //        Document doc=db.parse(file);

    };

    // --------------------------------------------------------- Public Methods

    /**
     * Print the given document to debug output. If debug is set to true;
     * @param doc
     */
    public void print(Document doc) {
        
        if (debug) {
            Node root = doc.getDocumentElement();
    
            dispatchNode(root, 0);
        }
    }  
    
    /**
     * Print the given XML string to debug output. If debug is set to true;
     * @param xmlString
     */
    public void print(String xmlString) {
 
        if (debug) {
            try {
                Document doc = db.parse(new InputSource(new StringReader(xmlString)));
                print(doc);
               
            } catch (SAXException e) {
            } catch (IOException e) {
            }
        }
        
        
    }  
    
    /**
     * Set debug information.
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    } 
    
    
    // --------------------------------------------------------- Private Methods

 
    
    private void dispatchNode(Node node, int level) {
        switch (node.getNodeType()) {
            case Node.CDATA_SECTION_NODE :
                printCDATANode(node, level);
                break;
    
            case Node.COMMENT_NODE :
                printCommentNode(node, level);
                break;
    
            case Node.TEXT_NODE :
                printTextNode(node, level);
                break;
    
            case Node.ELEMENT_NODE :
                printElementNode(node, level);
                break;
    
            default :
                break;
        }
    }
    
    private void printCDATANode(Node node, int level) {
        System.out.print(INDENT.substring(0, level * tabWidth));
        System.out.println("<![CDATA[");
    
        indentBlock(node.getNodeValue(), level + 1);
    
        System.out.print(INDENT.substring(0, level * tabWidth));
        System.out.println("]]>");
    }
    
    private void printTextNode(Node node, int level) {
        indentBlock(node.getNodeValue(), level + 1);
    }
    
    private void printCommentNode(Node node, int level) {
        System.out.print(INDENT.substring(0, level * tabWidth));
        System.out.println("<!-- ");
        indentBlock(node.getNodeValue(), level + 1);
        System.out.print(INDENT.substring(0, level * tabWidth));
        System.out.println(" -->");
    }
    
    private void printElementNode(Node node, int level) {
        String name = node.getNodeName();
        System.out.print(INDENT.substring(0, level * tabWidth));
        System.out.print("<");
        System.out.print(name);
    
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; attributes != null && i < attributes.getLength(); i++) {
            if (attributes.getLength() > 1) {
                System.out.println();
                System.out.print(INDENT.substring(0, (2 + level) * tabWidth));
            } else {
                System.out.print(" ");
            }
    
            Node attribute = attributes.item(i);
            System.out.print(attribute.getNodeName());
            System.out.print("=\"");
            System.out.print(attribute.getNodeValue());
            System.out.print("\"");
        }
        System.out.println(">");
    
        // in case of more than one attribute add a blank line as seperator
        if (attributes.getLength() > 1) {
            System.out.println();
        }
    
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                // recursive use of dispatchNode()
                dispatchNode(children.item(i), level + 1);
            }
        }
    
        System.out.print(INDENT.substring(0, level * tabWidth));
        System.out.print("</");
        System.out.print(name);
        System.out.println(">");
    }

    private void indentBlock(String block, int level) {
        StringTokenizer linetok = new StringTokenizer(block.replace('\n', ' '));
        int pos = level * tabWidth;
    
        if (linetok.countTokens() > 0) {
            System.out.print(INDENT.substring(0, level * tabWidth));
        }
    
        while (linetok.hasMoreTokens()) {
            String token = linetok.nextToken();
            pos += (token.length() + 1);
            if (pos < 80 && token.length() < (80 - (level * tabWidth))) {
                if (linetok.countTokens() > 0) {
                    System.out.print(token);
                    System.out.print(" ");
                } else {
                    System.out.println(token);
                }
            } else {
                System.out.println(token);
                if (linetok.countTokens() > 0) {
                    System.out.print(INDENT.substring(0, level * tabWidth));
                }
                pos = level * tabWidth;
            }
        }
    }


}
