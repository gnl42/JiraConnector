/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/MkWorkspaceMethod.java,v 1.4 2004/08/02 15:45:48 unico Exp $
 * $Revision: 1.4 $
 * $Date: 2004/08/02 15:45:48 $
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
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.webdav.lib.util.WebdavStatus;


/**
 * The MkWorkspace method is used to create a new workspace. New workspaces
 * can only be created in the workspace collection of the server. A workspace
 * can contain version controled resources and any other. Each resource
 * must identify its workspace.
 *
 * It is not allowed to create a new workspace inside an exiting workspace.
 *
 *
 * <h3>Example Request</h3>
 * <pre>
 * MKWORKSPACE /ws/myWs/ HTTP/1.1
 * Host: www.server.org
 * </pre>
 *
 * <h3>Example Response</h3>
 * <pre>
 * HTTP/1.1 201 Created
 * </pre>
 *
 */
public class MkWorkspaceMethod
    extends XMLResponseMethodBase {


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public MkWorkspaceMethod() {
    }


    /**
     * Method constructor.
     */
    public MkWorkspaceMethod(String path) {
        super(path);
    }


    // --------------------------------------------------- WebdavMethod Methods


    public String getName() {
        return "MKWORKSPACE";
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
            if (code == WebdavStatus.SC_CONFLICT     ||
                code == WebdavStatus.SC_FORBIDDEN ) {
                parseXMLResponse(input);
            }
        }
        catch (IOException e) {
                // FIX ME:  provide a way to deliver non xml data
        }
    }


}
