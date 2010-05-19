/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/SearchMethod.java,v 1.5 2004/08/02 15:45:47 unico Exp $
 * $Revision: 1.5 $
 * $Date: 2004/08/02 15:45:47 $
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
import java.util.Enumeration;
import java.util.Vector;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.webdav.lib.util.WebdavStatus;

/**
 * This class implements the WebDAV SEARCH Method.
 *
 * <P>     The SEARCH method initiates a server-side search. The body of the
 * request defines the query. The server responds with a text/xml entity
 * matching the WebDAV PROPFIND response.
 *
 * <P>     According to
 * <ahref="http://www.webdav.org/dasl/protocol/draft-dasl-protocol-00.html">
 * the DASL draft</a> a typical request looks like this:
 *
 * <PRE>
 * SEARCH /folder/ HTTP/1.1
 * Host: www.foo.bar
 * Content-type: text/xml; charset="utf-8"
 * Content-Length: xxxx
 *
 * &lt;?xml version="1.0"?>
 * &lt;D:searchrequest xmlns:D = "DAV:" >
 * &lt;D:basicsearch>
 *   &lt;D:select>
 *     &lt;D:prop>&lt;D:getcontentlength/>&lt;/D:prop>
 *   &lt;/D:select>
 *   &lt;D:from>
 *     &lt;D:scope>
 *       &lt;D:href>/folder/&lt;/D:href>
 *       &lt;D:depth>infinity&lt;/D:depth>
 *     &lt;/D:scope>
 *   &lt;/D:from>
 * &lt;/D:basicsearch>
 * &lt;/D:searchrequest>
 * </PRE>
 *
 * <P>     However, other query grammars may be used. A typical request using
 * the
 * <ahref="http://msdn.microsoft.com/library/default.asp?URL=/library/psdk/exchsv2k/_exch2k_sql_web_storage_system_sql.htm">
 * SQL-based grammar</a> implemented in Microsoft's Web Storage System
 * (currently shipping with Exchange 2000 and SharePoint Portal Server)
 * looks like this:
 *
 * <PRE>
 * SEARCH /folder/ HTTP/1.1
 * Host: www.foo.bar
 * Content-type: text/xml; charset="utf-8"
 * Content-Length: xxxx
 *
 * &lt;?xml version="1.0"?>
 * &lt;D:searchrequest xmlns:D = "DAV:" >
 *   &lt;D:sql>
 *   SELECT "DAV:contentclass", "DAV:displayname"
 *     FROM "/folder/"
 *    WHERE "DAV:ishidden" = false
 *      AND "DAV:isfolder" = false
 *   &lt;/D:sql>
 * &lt;/D:searchrequest>
 * </PRE>
 *
 */

public class SearchMethod extends XMLResponseMethodBase {


    // -------------------------------------------------------------- Constants


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public SearchMethod() {
    }


    /**
     * Method constructor.
     */
    public SearchMethod(String path) {
        super(path);
    }


    /**
     * Construct a SearchMethod using the given XML request body.
     *
     * @param path  Relative path to the WebDAV resource
     * (presumably a collection).
     * @param query Complete request body in XML including a search query in
     * your favorite grammar.
     */
    public SearchMethod(String path, String query) {
        this(path);
        preloadedQuery = query;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The namespace abbreviation that prefixes DAV tags
     */
    protected String prefix = null;

    private String preloadedQuery = null;

    // ------------------------------------------------------------- Properties


    // --------------------------------------------------- WebdavMethod Methods

    public String getName() {
        return "SEARCH";
    }

    public void recycle() {
        super.recycle();
        prefix = null;
    }


    /**
     * Generate additional headers needed by the request.
     *
     * @param state State token
     * @param conn the connection
     */
    public void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {

        // set the default utf-8 encoding, if not already present
        if (getRequestHeader("Content-Type") == null ) super.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
        super.addRequestHeaders(state, conn);

    }

    /**
     * DAV requests that contain a body must override this function to
     * generate that body.
     *
     * <p>The default behavior simply returns an empty body.</p>
     */
    protected String generateRequestBody() {

        if (preloadedQuery == null || preloadedQuery.trim().length() < 1) {
            // TODO  Must support some mechanism for delegating the
            // generation of the query to a pluggable query grammar
            // support class or package. Right now, executing this
            // method object without first explicitly setting the
            // query is an error.
            return "";
        } else {
            return preloadedQuery;
        }

    }

    /**
     * This method returns an enumeration of URL paths.  If the PropFindMethod
     * was sent to the URL of a collection, then there will be multiple URLs.
     * The URLs are picked out of the <code>&lt;D:href&gt;</code> elements
     * of the response.
     *
     * @return an enumeration of URL paths as Strings
     */
    public Enumeration getAllResponseURLs() {
        checkUsed();
        return getResponseURLs().elements();
    }


    /**
     * Returns an enumeration of <code>Property</code> objects.
     */
    public Enumeration getResponseProperties(String urlPath) {
        checkUsed();

        Response response = (Response) getResponseHashtable().get(urlPath);
        if (response != null) {
            return response.getProperties();
        } else {
            return (new Vector()).elements();
        }

    }

    /**
     * Parse response.
     *
     * @param input Input stream
     */
    public void parseResponse(InputStream input, HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        try
        {
            int code = getStatusLine().getStatusCode();
            if (code == WebdavStatus.SC_BAD_REQUEST ||
                code == WebdavStatus.SC_MULTI_STATUS ||
                code == WebdavStatus.SC_FORBIDDEN ||
                code == WebdavStatus.SC_CONFLICT ) {
                parseXMLResponse(input);
            }
        }
        catch (IOException e) {
                // FIX ME:  provide a way to deliver non xml data
        }
    }



}





