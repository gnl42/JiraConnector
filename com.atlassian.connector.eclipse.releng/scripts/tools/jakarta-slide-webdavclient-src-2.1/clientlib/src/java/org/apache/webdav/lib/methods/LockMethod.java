/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/LockMethod.java,v 1.6.2.2 2004/10/02 17:33:49 luetzkendorf Exp $
 * $Revision: 1.6.2.2 $
 * $Date: 2004/10/02 17:33:49 $
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

package org.apache.webdav.lib.methods;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.util.URIUtil;

import org.apache.webdav.lib.WebdavState;
import org.apache.webdav.lib.properties.LockEntryProperty;
import org.apache.webdav.lib.util.DOMUtils;
import org.apache.webdav.lib.util.DOMWriter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Web resources can be locked to ensure that only one user is updating
 * the resource at a time.  Locking helps to prevent the "lost update" problem.
 * There are two types of lock currently defined by the WebDAV specification:
 * exclusive locks and shared locks.
 *
 * <p>   Per the specification, a lock indicates that someone is updating the
 * resource, (hence the lock is a "write lock"), although the specification
 * notes that the the syntax is extensible, and permits the eventual creation
 * of locking for other access types.
 *
 * <h3>Shared and Exclusive Locks</h3>
 *
 * <p>   The most basic form of lock is an <em>exclusive lock</em>. This is a
 * lock where the access right in question is only granted to a single client.
 * The need for this arbitration results from a desire to avoid having to merge
 * results.  However, there are times when the goal of a lock is not to exclude
 * others from exercising an access right but rather to provide a mechanism for
 * principals to indicate that they intend to exercise their access rights.
 * <em>Shared locks</em> are provided for this case. A shared lock allows
 * multiple clients to receive a lock. Hence any user with appropriate
 * access can get the lock.
 *
 * <p>   With shared locks there are two trust sets that affect a resource.
 * The first trust set is created by access permissions.  Principals who are
 * trusted, for example, may have permission to write to the resource.  Among
 * those who have access permission to write to the resource, the set of
 * principals who have taken out a shared lock also must trust each other,
 * creating a (typically) smaller trust set within the access permission write
 * set.
 *
 * <h3>Lock Compatibility</h3>
 *
 * <p>   The following table indicates what happens if a new lock request
 * is sent to a resource that is already locked: </p>
 *
 * <table border="1">
 * <tr><th>             </th><th colspan="2">     Lock Request       </th></tr>
 * <tr><th>Current Lock </th><th>Exclusive Lock </th><th>Shared Lock </th></tr>
 * <tr><td>None         </td><td>Success        </td><td>Sucess      </td></tr>
 * <tr><td>Shared       </td><td>Failure        </td><td>Sucess      </td></tr>
 * <tr><td>Exclusive    </td><td>Failure        </td><td>Failure     </td></tr>
 * </table>
 *
 */
public class LockMethod
    extends XMLResponseMethodBase implements DepthSupport {


    // -------------------------------------------------------------- Constants


    public static final short SCOPE_EXCLUSIVE =
        LockEntryProperty.SCOPE_EXCLUSIVE;
    public static final short SCOPE_SHARED = LockEntryProperty.SCOPE_SHARED;

    public static final short TYPE_WRITE = LockEntryProperty.TYPE_WRITE;

    // The timeout value for TimeType "Second" MUST NOT be greater than 2^32-1.
    public static final int TIMEOUT_INFINITY = Integer.MAX_VALUE;


    // ----------------------------------------------------- Instance Variables


    /**
     * The scope of lock we're requesting.  The default scope is
     * SCOPE_EXCLUSIVE.
     */
    private short scope = SCOPE_EXCLUSIVE;

    /**
     * Depth.
     */
    private int depth = DEPTH_INFINITY;


    /**
     * Opaque token of the lock we're trying to refresh.
     */
    private String refreshOpaqueToken = null;


    /**
     * Lock timeout.
     */
    private int timeout = TIMEOUT_INFINITY;


    /**
     * Lock owner.
     */
    private String owner = null;


    /**
     * Lock token.
     */
    private String lockToken = null;
    
    private boolean typeTransaction = false;


    // ----------------------------------------------------------- Constructors


    /**
     * Creates a lock method that can <em>start a transaction</em> when server supports
     * them in a 
     * <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/wss/wss/_webdav_lock.asp">MS like style</a>.
     * The transacion handle of the started transaction will be returned as the lock token. 
     * To let subsequent requests participate in the transaction add a  
     * <code>Transaction</code> header with the lock token as value. You will have to enclose it in '&lt;' and '>' just
     * like ordinary lock tokens.  
     * <br><br>
     * To either commit or abort the transaction
     * use {@link UnlockMethod}. 
     *
     * @param path any path inside Slide's scope
     * @param owner of this transaction 
     * @param timeout timeout of this transaction
     * @param isTransaction <code>true</code> when this method is used to starte a transaction
     * 
     */
    public LockMethod(String path, String owner, int timeout, boolean isTransaction) {
        this(path);
        setOwner(owner);
        setTimeout(timeout);
        setTypeTransaction(isTransaction);
    }
    
    /**
     * Method constructor.
     */
    public LockMethod() {
    }


    /**
     * Method constructor.
     */
    public LockMethod(String path) {
        super(path);
    }


    /**
     * Method constructor.
     */
    public LockMethod(String path, String refreshOpaqueToken, int timeout) {
        this(path);
        this.refreshOpaqueToken = refreshOpaqueToken;
        setTimeout(timeout);
    }


    /**
     * Method constructor.
     */
    public LockMethod(String path, String owner, short scope, int timeout) {
        this(path);
        setOwner(owner);
        setScope(scope);
        setTimeout(timeout);
    }

    /**
     * Method constructor.
     * @deprecated The timeout value MUST NOT be greater than 2^32-1.
     */
    public LockMethod(String path, String refreshOpaqueToken, long timeout) {
        this(path, refreshOpaqueToken, (int) timeout);
    }


    /**
     * Method constructor.
     * @deprecated The timeout value MUST NOT be greater than 2^32-1.
     */
    public LockMethod(String path, String owner, short scope, long timeout) {
        this(path, owner, scope, (int) timeout);
    }

    // ------------------------------------------------------------- Properties




    /**
     * Set a header value, redirecting the special cases of Depth and Time headers
     * to {@link #setDepth} and {@link #setTimeout} as appropriate.
     *
     * @param headerName Header name
     * @param headerValue Header value
     */
    public void setRequestHeader(String headerName, String headerValue) {
        if (headerName.equalsIgnoreCase("Depth")){
            int depth = -1;
            if (headerValue.equals("0")){
                depth = DEPTH_0;
            }
            if (headerValue.equals("1")){
                depth = DEPTH_1;
            }
            else if (headerValue.equalsIgnoreCase("infinity")){
                depth = DEPTH_INFINITY;
            }
            setDepth(depth);
        }
        else if(headerName.equalsIgnoreCase("Timeout")){
            if (headerValue.startsWith("Second-"))
                headerValue = headerValue.substring("Second-".length());
            try {
                setTimeout(Integer.parseInt(headerValue));
            } catch (NumberFormatException e) {
            }
        }
        else if(headerName.equalsIgnoreCase("Owner")){
            setOwner(headerValue);
        }
        else{
            super.setRequestHeader(headerName, headerValue);
        }
    }

    public boolean isTypeTransaction() {
        return typeTransaction;
    }

    public void setTypeTransaction(boolean typeTransaction) {
        this.typeTransaction = typeTransaction;
    }

    /**
     * Depth setter.
     *
     * @param depth New depth value
     */
    public void setDepth(int depth) {
        checkNotUsed();
        if (depth != DEPTH_0 && depth != DEPTH_INFINITY) {
            throw new IllegalArgumentException
            ("invalid depth value for lock method " + depth);
        }
        this.depth = depth;
    }


    /**
     * Depth getter.
     *
     * @return int depth value
     */
    public int getDepth() {
        return depth;
    }


    public String getLockToken() {
        checkUsed();
        return this.lockToken;
    }



    public boolean isRefresh() {
        return !((this.refreshOpaqueToken == null ) ||
                 (this.refreshOpaqueToken.equals("")));
    }


    public short getScope() {
        return this.scope;
    }


    /**
     * Sets the owner of the lock.  This method provides only "basic" owner
     * information.  Thus, <code>setOwner("Jezebel Lipshitz")</code> will
     * produce an <code>owner</code> element in the request document like this:
     *
     * <pre>
     *   &lt;D:owner&gt;Jezebel Lipshitz&lt;/D:owner&gt;
     * </pre>
     *
     * <p>  Examples in the Webdav specification suggest that one can use
     * e-mail addresses, home page URLs, or other information; this
     * implementation doesn't handle any of that.
     */
    public void setOwner(String owner) {
        checkNotUsed();
        this.owner = owner;
    }

    /** Return the owner of the lock as reported by the server. */
    public String getOwner() {
        return owner;
    }


    public void setScope(short scope) {
        checkNotUsed();
        if (scope != SCOPE_SHARED && scope != SCOPE_EXCLUSIVE) {
            throw new IllegalArgumentException("invalid scope value");
        }
        this.scope = scope;
    }


    /**
     * get the timeout value.
     *
     * @return timeout
     */
    public int getTimeout() {
        return this.timeout;
    }


    /**
     * Set the timeout value.
     */
    public void setTimeout(int timeout) {
        checkNotUsed();
        if (timeout < 0) {
            throw new IllegalArgumentException("invalid timeout value: " +
                                                   timeout);
        }
        this.timeout = timeout;
    }

    /**
     * Set the timeout value.
     * @deprecated The timeout value MUST NOT be greater than 2^32-1.
     */
    public void setTimeout(long timeout) {
        setTimeout((int) timeout);
    }


    // --------------------------------------------------- WebdavMethod Methods

    public String getName() {
        return "LOCK";
    }

    public void recycle() {
        super.recycle();
        this.refreshOpaqueToken = null;
        this.depth = DEPTH_INFINITY;
        this.scope = SCOPE_EXCLUSIVE;
        this.timeout = TIMEOUT_INFINITY;
        this.typeTransaction = false;
    }


    /**
     * Generate additional headers needed by the request.
     *
     * @param state State token
     * @param conn The connection being used for the request.
     */
    public void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {

        // set the default utf-8 encoding, if not already present
        if (getRequestHeader("Content-Type") == null ) super.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
        super.addRequestHeaders(state, conn);

        switch (depth) {
            case DEPTH_0:
                super.setRequestHeader("Depth", "0");
                break;
            case DEPTH_INFINITY:
                super.setRequestHeader("Depth", "infinity");
                break;
            default:
        }

        if (timeout == TIMEOUT_INFINITY) {
            super.setRequestHeader("Timeout", "Infinite, Second-" + TIMEOUT_INFINITY);
        } else {
            super.setRequestHeader("Timeout", "Second-" + timeout);
        }

        if (isRefresh()) {
            super.setRequestHeader("If", "(<" + refreshOpaqueToken + ">)");
        }

    }

    /**
     * DAV requests that contain a body must override this function to
     * generate that body.
     *
     * <p>The default behavior simply returns an empty body.</p>
     */
    protected String generateRequestBody() {

        String result = "";

        if (!isRefresh()) {

            if (this.owner == null || this.owner.equals("")) {
                throw new IllegalStateException
                    ("The owner property has not been set");
            }

            try {

                DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.newDocument();

                Element lockinfo = document.createElement("DAV:lockinfo");
                document.appendChild(lockinfo);
                lockinfo.setAttribute("xmlns:DAV", "DAV:");

                Element lockscope = document.createElement("DAV:lockscope");
                lockinfo.appendChild(lockscope);

                if (this.scope == SCOPE_EXCLUSIVE) {
                    Element exclusive =
                        document.createElement("DAV:exclusive");
                    lockscope.appendChild(exclusive);
                } else {
                    Element shared = document.createElement("DAV:shared");
                    lockscope.appendChild(shared);
                }

                Element locktype = document.createElement("DAV:locktype");
                lockinfo.appendChild(locktype);

                if (typeTransaction) {
                    Element transaction = document.createElement("DAV:transaction");
                    locktype.appendChild(transaction);
                } else {
                    Element write = document.createElement("DAV:write");
                    locktype.appendChild(write);
                }

                Element owner = document.createElement("DAV:owner");
                lockinfo.appendChild(owner);

                Text text = document.createTextNode(this.owner);
                owner.appendChild(text);

                StringWriter stringWriter = new StringWriter();
                DOMWriter domWriter = new DOMWriter(stringWriter, false);
                domWriter.print(document);

                result = stringWriter.getBuffer().toString();

            } catch (DOMException e) {
            } catch (ParserConfigurationException e) {
            }

        }

        return result;
    }

    /**
     * Parse response.
     *
     * @param input Input stream
     */
    public void parseResponse(InputStream input, HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        int status = getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_OK      ||
            status == HttpStatus.SC_CREATED ||
            status == HttpStatus.SC_MULTI_STATUS ) {

            parseXMLResponse(input);

            NodeList list = getResponseDocument().getDocumentElement()
                  .getElementsByTagNameNS("DAV:", "locktoken");

            if (list.getLength() == 1) {
                Element locktoken = (Element) list.item(0);
                NodeList list2 = locktoken.getElementsByTagNameNS("DAV:", "href");
                if (list2.getLength() == 1) {
                    this.lockToken = DOMUtils.getTextValue(list2.item(0));
                    if (state instanceof WebdavState) {
                       /* 
                        * lockMethod/unlockMethod take unescaped URIs but LockMathod.getPath()
                        * func returns an escaped URI so searching for the lock by path name in 
                        * the state object doesn't work. Convert escaped back to unescaped.
                        */
                        ((WebdavState) state).addLock(URIUtil.decode(getPath()), 
                                this.lockToken);
                    }
                }
            }

            list = getResponseDocument().getDocumentElement()
                  .getElementsByTagNameNS("DAV:", "owner");

            if (list.getLength() == 1) {
                Element owner = (Element)list.item(0);

                this.owner = DOMUtils.getTextValue(owner);
            }
        }
    }
}

