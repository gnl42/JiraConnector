/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/HttpRequestBodyMethodBase.java,v 1.3 2004/07/28 09:30:46 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:30:46 $
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.apache.commons.httpclient.ChunkedOutputStream;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConstants;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;


/**
 * PUT Method.
 *
 *
 * @since 1.0
 */
public abstract class HttpRequestBodyMethodBase extends HttpMethodBase {


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor.
     */
    public HttpRequestBodyMethodBase() {
        super();
    }



    /**
     * URI-setting constructor.
     *
     * @param uri the URI to request. The URI is expected
     *        to be already URL encoded.  It may be either an absolute or
     *        server relative path.
     *
     * @since 1.0
     */
    public HttpRequestBodyMethodBase(String uri) {
        super(uri);
    }


    // ------------------------------------------------------- Instance Methods


    /**
     * Request body content to be sent.
     */
    private byte[] data = null;


    /**
     * Request body content to be sent.
     */
    private File file = null;


    /**
     * Request body content to be sent.
     */
    private URL url = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Set my request body content to the contents of a file.
     *
     * @since 2.0
     */
    public void setRequestBody(File file) throws IOException {
        checkNotUsed();
        this.file = file;
    }

    /**
     * Set my request body content to the resource at the specified URL.
     *
     * @since 2.0
     */
    public void setRequestBody(URL url) throws IOException {
        checkNotUsed();
        this.url = url;
    }

    /**
     * Set my request body content to the contents of a byte array.
     *
     * @since 2.0
     */
    public void setRequestBody(byte[] bodydata) {
        checkNotUsed();
        this.data = bodydata;
    }

    /**
     * Set my request body content to the contents of a string.
     *
     * @since 2.0
     */
    public void setRequestBody(String bodydata) {
        checkNotUsed();
        setRequestBody(HttpConstants.getContentBytes(bodydata, getRequestCharSet()));
    }

    /**
     * Set my request body content to the contents of an input stream.
     * The contents will be buffered into
     * memory. To upload large entities, it is recommended to first buffer the
     * data into a temporary file, and then send that file.
     *
     * @since 2.0
     */
    public void setRequestBody(InputStream is)
        throws IOException {

        checkNotUsed();
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int nb = 0;
        while (true) {
            nb = is.read(buffer);
            if (nb == -1) {
                break;
            }
            os.write(buffer, 0, nb);
        }
        data = os.toByteArray();
    }

    /**
     * Returns true if <tt>100 Continue</tt> status code
     * is found.
     *
     * @since 2.0
     */
    public boolean readContinueCode() {
        if (getStatusLine() == null) {
            return false;
        }
        if(null != getRequestHeader("expect") &&
           getStatusLine().getStatusCode() != HttpStatus.SC_CONTINUE) {
            return false;
        }
        return true;
    }

    /**
     * Do write the request body.
     * Override the method of {@link HttpMethodBase}
     * if the method should wait until a <tt>100 Continue</tt> status code
     * is expected (@link readContinueCode)
     *
     * @since 2.0
     */
    protected boolean writeRequestBody(HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        OutputStream out = conn.getRequestOutputStream();
        if (isHttp11() && (null == getRequestHeader("Content-Length"))) {
            out = new ChunkedOutputStream(out);
        }

        InputStream inputStream = null;
        if (file != null && file.exists()) {
            inputStream = new FileInputStream(file);
        } else if (url != null) {
            inputStream = url.openConnection().getInputStream();
        } else if(data != null){
            inputStream = new ByteArrayInputStream(data);
        } else {
            return true;
        }

        byte[] buffer = new byte[4096];
        int nb = 0;
        while (true) {
            nb = inputStream.read(buffer);
            if (nb == -1) {
                break;
            }
            out.write(buffer, 0, nb);
        }
        out.flush();
        return true;
    }

    /**
     * Override the method of {@link HttpMethodBase}
     * to return the appropriate content length.
     *
     * @since 2.0
     */
    protected int getRequestContentLength() {
        if(null != data) {
            return data.length;
        } else if(null != file && file.exists()) {
            return (int)(file.length());
        } else if(url != null) {
            return -1;
        } else {
            return 0;
        }
    }


    /**
     * return true, if the method setRequestContent has been called (with a null parameter)
     *
     * @since 2.0
     */
    protected boolean isRequestContentAlreadySet() {
        return (data != null) || (file != null) || (url != null);
    }

    /**
     *
     * @since 1.0
     */
    public void recycle() {
        super.recycle();
        data = null;
        url = null;
        file = null;
    }
}

