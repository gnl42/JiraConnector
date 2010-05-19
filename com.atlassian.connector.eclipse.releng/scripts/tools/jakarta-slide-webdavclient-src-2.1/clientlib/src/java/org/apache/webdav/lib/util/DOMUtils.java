/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/util/DOMUtils.java,v 1.2.2.1 2004/08/30 08:09:04 ib Exp $
 * $Revision: 1.2.2.1 $
 * $Date: 2004/08/30 08:09:04 $
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

import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class provides some basic utility methods for working with
 * XML Document objects.  Many of these utilities provide JAXP 1.0 "brute
 * force" implementations of functions that are available in JAXP 1.1.
 *
 * @version $Revision: 1.2.2.1 $
 */
public class DOMUtils {

    protected static Class[] getElementsByNSParameterTypes =
        { String.class, String.class };

    /**
     *  Determine the namespace prefix being used for DAV.
     *  Generally, DAV responses say something like:
     *
     *  <PRE>
     *  &lt;D:multistatus xmlns:D="DAV:"&gt;
     *  </PRE>
     *
     *  <P>  In this case, the "D:" is the prefix for DAV.
     *  @deprecated
     */
    public static String findDavPrefix(Document document) {
        Element multistatus = document.getDocumentElement();
        NamedNodeMap list = multistatus.getAttributes();
        String prefix = "DAV:";
        for (int i = 0; i < list.getLength(); i++) {
            try {
                Attr attr = (Attr) list.item(i);
                if (attr.getName() != null &&
                    attr.getName().startsWith("xmlns") &&
                    attr.getValue().equals("DAV:")) {
                    int indx = attr.getName().indexOf(":");
                    if ((indx >= 0) && (indx < attr.getName().length()-1)) {
                        prefix = attr.getName().substring(indx + 1) + ":";
                    } else {
                        prefix = "";
                    }
                }
            } catch (ClassCastException e) {
            }
        }
        return prefix;
    }


    /**
     *  Recursively scans all child elements, appending any text nodes.
     *
     *  <PRE>
     *  &lt;customer&gt;Joe Schmoe&lt;/customer&gt;
     *  </PRE>
     *
     *  <P>  In this case, calling this method on the
     *  <CODE>customer</CODE> element returns "Joe Schmoe".
     */
    public static String getTextValue(Node node) {

        // I *thought* that I should be able to use element.getNodeValue()...

        StringBuffer text = new StringBuffer();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.TEXT_NODE
                || nodeList.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
                text.append(((Text) nodeList.item(i)).getData());
            } else {
                text.append(getTextValue(nodeList.item(i)));
            }
        }
        return text.toString();
    }

    /**
     *  Get the status code out of the normal status response.
     *
     *  <P>  Each <code>DAV:propstat</code> node contains a
     *  status line, such as:
     *
     *  <PRE>
     *  &lt;DAV:status&gt;HTTP/1.1 200 OK&lt;/DAV:status&gt;
     *  </PRE>
     *
     *  <P>  In this case, calling this method on the
     *  text string returns 200.
     */
    public static int parseStatus(String statusString) {
        int status = -1;
        if (statusString != null) {
            StringTokenizer tokenizer = new StringTokenizer(statusString);
            if (tokenizer.countTokens() >= 2) {
                tokenizer.nextElement();
                String statusCode = tokenizer.nextElement().toString();
                try {
                    status = Integer.parseInt(statusCode);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "Status code is not numeric");
                }
            } else {
                throw new IllegalArgumentException(
                    "There aren't enough words in the input argument");
            }
        }
        return status;
    }

    public static String getElementNamespaceURI(Element element) {
        String namespace = null;

        if (element == null) {
            throw new IllegalArgumentException(
                "The element cannot be null");
        } else {
            try {
                namespace = element.getNamespaceURI();                
            }
            catch (NoSuchMethodError e) {
                String tagName = element.getTagName();
                String attribute = "xmlns";
                int index = tagName.indexOf(":");
                if (index > 0 && index < (tagName.length()-1)) {
                    attribute += (":" + tagName.substring(0,index));
                }

                boolean found = false;
                for (Node node = element; !found && node != null;
                    node = node.getParentNode()) {
                    try {
                        String tmp = ((Element) node).getAttribute(attribute);
                        if (tmp != null && !tmp.equals("")) {
                            namespace = tmp;
                            found = true;
                        }
                    }
                    catch (ClassCastException f) {
                        // this will happen for Documents
                    }
                }
            }
        }

        return namespace;
    }

    public static String getElementLocalName(Element element) {
        String localName = null;

        if (element == null) {
            throw new IllegalArgumentException(
                "The element cannot be null");
        } else {
            try {
                localName = element.getLocalName();
            }
            catch (NoSuchMethodError e) {
                localName = element.getTagName();
                int index = localName.indexOf(":");
                if (index > 0 && index < (localName.length()-1)) {
                    localName = localName.substring(index + 1);
                }
            }
        }
        return localName;
    }

    /**
     *
     */
    public static NodeList getElementsByTagNameNS(
        Node node, String tagName, String namespace) {

        NodeList list = null;
        
        if (node == null) {
            return null;
        } 
        else if (!(node instanceof Document) && !(node instanceof Element)) {
            throw new IllegalArgumentException(
                    "The node parameter must be an Element or a Document node");
        }
        else {
            try {
                list = ((Element) node).getElementsByTagNameNS(namespace, tagName);
            }
            catch (NoSuchMethodError e) {
                Vector vector = new Vector();
                getChildElementsByTagNameNS(vector, node, tagName, namespace);
                list = new NodeListImpl(vector);
            }
        }
        return list;
    }


    protected static void getChildElementsByTagNameNS(
        Vector vector, Node node, String tagName, String namespace) {

        NodeList list = node.getChildNodes();
        for (int i = 0; list != null && i < list.getLength(); i++) {
            try {
                Element element = (Element) list.item(i);

                if (tagName.equals(getElementLocalName(element)) &&
                    namespace.equals(getElementNamespaceURI(element))) {

                    vector.addElement(element);
                } else {
                    // RECURSIVE!  DANGER, WILL ROBINSON!
                    getChildElementsByTagNameNS(vector, element,
                                                tagName, namespace);
                }
            } catch (ClassCastException e) {
            }
        }
    }


    /**
     * Get the first element matched with the given namespace and name.
     *
     * @param node The node.
     * @param namespac The namespace.
     * @param name The name.
     * @return The wanted first element.
     */
    public static Element getFirstElement(Node node, String namespace,
                                          String name) {
        NodeList children = node.getChildNodes();
        if (children == null)
            return null;
        for (int i = 0; i < children.getLength(); i++) {
            try {
                Element child = (Element) children.item(i);
                if (name.equals(getElementLocalName(child)) &&
                    namespace.equals(getElementNamespaceURI(child))) {
                    return child;
                }
            } catch (ClassCastException e) {
            }
        }
        return null;
    }


    // ---------------------------------------------------------- Inner Classes


    /**
     * This class provides an implementation of NodeList, which is used by
     * the getElementsByTagNameNS() method.
     */
    static class NodeListImpl implements NodeList {
        private Vector vector = null;

        NodeListImpl(Vector vector) {
            this.vector = vector;
        }

        public int getLength() {
            return vector.size();
        }

        public Node item(int i) {
            return (Node) vector.elementAt(i);
        }
    }
}
