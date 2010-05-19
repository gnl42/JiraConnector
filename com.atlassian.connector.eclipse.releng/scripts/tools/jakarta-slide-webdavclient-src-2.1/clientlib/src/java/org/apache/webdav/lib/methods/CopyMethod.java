/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/CopyMethod.java,v 1.6 2004/07/28 09:30:48 ib Exp $
 * $Revision: 1.6 $
 * $Date: 2004/07/28 09:30:48 $
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
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;


/**
 * COPY Method.
 *
 */
public class CopyMethod
    extends XMLResponseMethodBase {


    // ----------------------------------------------------------- Constructors


    /**
     * Method constructor.
     */
    public CopyMethod() {
    }


    /**
     * Method constructor.
     */
    public CopyMethod(String source) {
        super(source);
    }


    /**
     * Method constructor.
     */
    public CopyMethod(String source, String destination) {
        this(source);
        setDestination(destination);
    }


    /**
     * Method constructor.
     */
    public CopyMethod(String source, String destination, boolean overwrite) {
        this(source, destination);
        setOverwrite(overwrite);
    }

    public CopyMethod(String source, String destination, boolean overwrite, int depth) {
        this(source, destination, overwrite);
        setDepth(depth);
    }

    // ----------------------------------------------------- Instance Variables


    /**
     * Destination.
     */
    private String destination;


    /**
     * Overwrite.
     */
    private boolean overwrite = true;

    /**
     * Depth.
     */
    private int depth = DepthSupport.DEPTH_INFINITY;

    // ----------------------------------------------------- Instance Variables


    /**
     * Set a header value, redirecting the special case of Overwrite and Destination headers
     * to {@link #setOverwrite} and {@link #setDestination} as appropriate.
     *
     * @param headerName Header name
     * @param headerValue Header value
     */
    public void setRequestHeader(String headerName, String headerValue) {
        if (headerName.equalsIgnoreCase("Overwrite")){
            setOverwrite(! (headerValue.equalsIgnoreCase("F") ||
                           headerValue.equalsIgnoreCase("False") ) );
        }
        else if (headerName.equalsIgnoreCase("Destination")){
            setDestination(headerValue);
        }
        else if (headerName.equalsIgnoreCase("Depth")) {
            if (headerValue.equalsIgnoreCase("Infinity")) {
                setDepth(DepthSupport.DEPTH_INFINITY);
            }
            else if (headerValue.equalsIgnoreCase("0")) {
                setDepth(0);
            }
        }
        else{
            super.setRequestHeader(headerName, headerValue);
        }
    }




    /**
     * Destination setter.
     *
     * @param destination New destination value
     */
    public void setDestination(String destination) {
        checkNotUsed();
        this.destination = destination;
    }


    /**
     * Destination getter.
     *
     * @return String destination value
     */
    public String getDestination() {
        return destination;
    }


    /**
     * Overwrite setter.
     *
     * @param overwrite New overwrite value
     */
    public void setOverwrite(boolean overwrite) {
        checkNotUsed();
        this.overwrite = overwrite;
    }


    /**
     * Overwrite getter.
     *
     * @return boolean Overwrite value
     */
    public boolean isOverwrite() {
        return overwrite;
    }


    /**
     * Overwrite getter.
     *
     * @return boolean Overwrite value
     */
    public boolean getOverwrite() {
        return overwrite;
    }

    /**
     * Depth setter.
     * 
     * @param depth New depth value
     */
    public void setDepth(int depth) {
        checkNotUsed();
        this.depth = depth;
    }
    
    /**
     * Depth getter.
     * 
     * @return int Depth value
     */
    public int getDepth() {
        return this.depth;
    }

    public String getName() {
        return "COPY";
    }

    // --------------------------------------------------- WebdavMethod Methods


    /**
     * Generate additional headers needed by the request.
     *
     * @param state HttpState token
     * @param conn The connection being used for the request.
     */
    public void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {

        super.addRequestHeaders(state, conn);

        String absoluteDestination = MoveMethod.getAbsoluteDestination(conn, destination);
        super.setRequestHeader("Destination", absoluteDestination);

        if (!isOverwrite())
            super.setRequestHeader("Overwrite", "F");

        switch (depth) {
	        case DepthSupport.DEPTH_0:
	            super.setRequestHeader("Depth", "0");
	            break;
	        case DepthSupport.DEPTH_INFINITY:
	            super.setRequestHeader("Depth", "Infinity");
	            break;
        }
    }

}

