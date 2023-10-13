/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.theplugin.commons.remoteapi.rest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPath;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.exception.HttpProxySettingsException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import me.glindholm.theplugin.commons.remoteapi.ServiceUnavailableException;
import me.glindholm.theplugin.commons.util.LoggerImpl;
import me.glindholm.theplugin.commons.util.UrlUtil;

/**
 * Communication stub for lightweight XML based APIs. This method should be tread-safe (at least it is used in this
 * manner), however I think there are still some issues with thread-safety here [wseliga]. E.g. as Server is not
 * immutable then this may be the cause of races
 */
public abstract class AbstractHttpSession {
    @NonNull
    protected final HttpSessionCallback callback;

    @NonNull
    private final ConnectionCfg server;
    private static final int MAX_REDIRECTS = 3;
    private String responseCharSet;

    @NonNull
    protected ConnectionCfg getServer() {
        return server;
    }

    protected String getUsername() {
        return server.getUsername();
    }

    protected String getPassword() {
        return server.getPassword();
    }

    private final Object clientLock = new Object();

    private static ThreadLocal<URL> url = new ThreadLocal<URL>();

    // TODO: replace this with a proper cache to ensure automatic purging. Responses can get quite large.
    private final Map<String, CacheRecord> cache = new WeakHashMap<String, CacheRecord>();

    /**
     * This class holds an HTTP response body, together with its last modification time and Etag.
     */
    private final class CacheRecord {
        private final byte[] document;

        private final String lastModified;

        private final String etag;

        private CacheRecord(byte[] document, String lastModified, String etag) {
            if (document == null || lastModified == null || etag == null) {
                throw new IllegalArgumentException("null");
            } else {
                this.document = document;
                this.lastModified = lastModified;
                this.etag = etag;
            }
        }

        public byte[] getDocument() {
            return document;
        }

        public String getLastModified() {
            return lastModified;
        }

        public String getEtag() {
            return etag;
        }
    }

    public static URL getUrl() {
        return url.get();
    }

    public static void setUrl(final URL urlString) {
        url.set(urlString);
    }

    public static void setUrl(final String urlString) throws MalformedURLException {
        setUrl(new URL(urlString));
    }

    protected String getBaseUrl() {
        return UrlUtil.removeUrlTrailingSlashes(server.getUrl());
    }

    /**
     * Public constructor for AbstractHttpSession
     *
     * @param server   server params used by this session
     * @param callback provider of HttpSession
     * @throws me.glindholm.theplugin.commons.remoteapi.RemoteApiMalformedUrlException
     *          for malformed url
     */
    public AbstractHttpSession(@NonNull ConnectionCfg server, @NonNull HttpSessionCallback callback)
            throws RemoteApiMalformedUrlException {
        this.server = server;
        this.callback = callback;
        String myurl = server.getUrl();
        try {
            UrlUtil.validateUrl(myurl);
        } catch (MalformedURLException e) {
            throw new RemoteApiMalformedUrlException("Malformed server URL: " + myurl, e);
        }
    }

    protected Document retrieveGetResponse(String urlString) throws IOException, JDOMException,
            RemoteApiSessionExpiredException {

        final SAXBuilder builder = new SAXBuilder();

        ByteArrayInputStream in = new ByteArrayInputStream(doConditionalGet(urlString));
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(reader);
        StringBuilder allInput = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            allInput.append(line);
        }
        in.close();
        br.close();
        reader.close();

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(allInput.toString().getBytes());
            final Document doc = builder.build(bis);
            preprocessResult(doc);
            return doc;
        } catch (JDOMException e) {
            throw new JDOMException(e.getMessage() + "\n\n" + allInput.toString() + "\n");
        }
    }

    /**
     * PayPal
     * This method should be use to fetch standard non-XML text resources (like Bamboo build logs), when there is no
     * intention to parse them by XML and you want to respect HTTP encoding standards (e.g. ISO-8859-1 if there is no
     * charset info set in the response header. This method does not cache results, nor it supports conditional get.
     *
     * @param urlString URL
     * @return response encoded as String. Encoding respects content type sent by the server in the response headers
     * @throws IOException in case of any problem or bad URL
     */
    protected String doUnconditionalGetForTextNonXmlResource(final String urlString) throws IOException {
        UrlUtil.validateUrl(urlString);
        setUrl(urlString);
        synchronized (clientLock) {
            HttpClient client;
            try {
                client = callback.getHttpClient(server);
            } catch (HttpProxySettingsException e) {
                throw createIOException("Connection error. Please set up HTTP Proxy settings", e);
            }

            GetMethod method;

            try {
                method = new GetMethod(urlString);
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid url " + urlString);
            }

            try {
                // method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
                method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
                method.getParams().setSoTimeout(client.getParams().getSoTimeout());
                callback.configureHttpMethod(this, method);
                client.executeMethod(method);

                if (method.getStatusCode() != HttpStatus.SC_OK) {
                    throw new IOException("HTTP " + method.getStatusCode() + " ("
                            + HttpStatus.getStatusText(method.getStatusCode()) + ")\n" + method.getStatusText());
                } else {
                    return method.getResponseBodyAsString();
                }
            } catch (NullPointerException e) {
                throw createIOException("Connection error", e);
            } finally {
                method.releaseConnection();
            }
        }
    }

    /**
     * Use it only for retrieving XML information, as it will ignored content-type charset in response header (if such
     * present)
     *
     * @param urlString URL to retrieve data from
     * @return response as raw bytes (ignoring charset info in response headers). This is OK for XML parser, as servers
     *         supported by us use either encoding info in XML header or use UFT-8
     * @throws IOException in case of IO problem
     */
    protected byte[] doConditionalGet(String urlString) throws IOException {

        UrlUtil.validateUrl(urlString);
        setUrl(urlString);
        synchronized (clientLock) {
            HttpClient client;
            try {
                client = callback.getHttpClient(server);
            } catch (HttpProxySettingsException e) {
                throw createIOException("Connection error. Please set up HTTP Proxy settings", e);
            }

            GetMethod method;

            try {
                method = new GetMethod(urlString);
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid url " + urlString);
            }

            CacheRecord cacheRecord = cache.get(urlString);
            callback.configureHttpMethod(this, method);

            if (cacheRecord != null) {
//                System.out.println(String.format("%s in cache, adding If-Modified-Since: %s and If-None-Match: %s headers.",
//                    urlString, cacheRecord.getLastModified(), cacheRecord.getEtag()));
                method.addRequestHeader("If-Modified-Since", cacheRecord.getLastModified());
                method.addRequestHeader("If-None-Match", cacheRecord.getEtag());
            }

            method.addRequestHeader("Accept", "application/xml;q=0.9,*/*");

            try {
                // method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
                method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
                method.getParams().setSoTimeout(client.getParams().getSoTimeout());

                client.executeMethod(method);

                if (method.getStatusCode() == HttpStatus.SC_NOT_MODIFIED && cacheRecord != null) {
//					System.out.println("Cache record valid, using cached value: " + new String(cacheRecord.getDocument()));
                    return cacheRecord.getDocument();
                } else if (method.getStatusCode() != HttpStatus.SC_OK) {
                    final String errorDescription = "HTTP " + method.getStatusCode() + " ("
                            + HttpStatus.getStatusText(method.getStatusCode()) + ")";
                    LoggerImpl.getInstance().info(errorDescription + "\n" + method.getStatusText());

                    throw createIOException(errorDescription, new Exception(method.getResponseBodyAsString()));
                } else {
                    final byte[] result = method.getResponseBody();
                    final String lastModified = method.getResponseHeader("Last-Modified") == null ? null
                            : method.getResponseHeader("Last-Modified").getValue();
                    final String eTag = method.getResponseHeader("Etag") == null ? null : method.getResponseHeader(
                            "Etag").getValue();

                    if (lastModified != null && eTag != null) {
                        cacheRecord = new CacheRecord(result, lastModified, eTag);
                        cache.put(urlString, cacheRecord);
                    }
                    return result;
                }
            } catch (NullPointerException e) {
                throw createIOException("Connection error", e);
            } catch (AuthenticationException e) {
                // bug PL-1275
                throw createIOException("Connection error", e);
            } finally {
                method.releaseConnection();
            }
        }
    }

    public String getResponseCharSet() {
        return responseCharSet;
    }
//
//    private String getContentType(final HttpClient postMethod) {
//                  String characterEncoding = getResponseCharSet();
//                  if (characterEncoding == null && postMethod != null) {
//                      try {
//                          characterEncoding = callback.getHttpClient(server).
// getHostConfiguration().getParams(). postMethod.getCharacterEncoding();
//                      } catch (HttpProxySettingsException e) {
//
//                      }
//                  }
//                  if (characterEncoding == null) {
//                          characterEncoding = postMethod.getDefaultCharacterEncoding();
//                  }
//                  return "application/x-www-form-urlencoded; charset=" + characterEncoding; //$NON-NLS-1$
//          }

    /**
     * Helper method needed because IOException in Java 1.5 does not have constructor taking "cause"
     *
     * @param message message
     * @param cause   chained reason for this exception
     * @return constructed exception
     */
    private IOException createIOException(String message, Throwable cause) {
        final IOException ioException = new IOException(message);
        ioException.initCause(cause);
        return ioException;
    }

    protected Document retrievePostResponse(String urlString, Document request) throws IOException, JDOMException, RemoteApiException {
        return retrievePostResponse(urlString, request, null);
    }

    protected Document retrievePostResponse(String urlString, Document request, StringBuilder txtHolder) throws IOException, JDOMException,
            RemoteApiException {
        return retrievePostResponse(urlString, request, true, txtHolder);
    }

    protected Document retrievePostResponse(String urlString, Document request, boolean expectResponse)
            throws JDOMException, RemoteApiException {
        return retrievePostResponse(urlString, request, expectResponse, null);
    }

    protected Document retrievePostResponse(String urlString, Document request, boolean expectResponse, StringBuilder txtHolder)
            throws JDOMException, RemoteApiException {
        XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
        String requestString = serializer.outputString(request);
        return retrievePostResponse(urlString, requestString, expectResponse, txtHolder);
    }

    protected Document retrievePostResponse(String urlString, String request, boolean expectResponse)
            throws JDOMException, RemoteApiException {
        return retrievePostResponse(urlString, request, expectResponse, null);
    }

    protected Document retrievePostResponse(String urlString, String request, boolean expectResponse, StringBuilder txtHolder)
            throws JDOMException, RemoteApiException {
        return retrievePostResponseInternal(urlString, request, expectResponse, 0, txtHolder);
    }

    protected interface PostMethodPreparer {
        void prepare(PostMethod postMethod) throws UnsupportedEncodingException;
    }

    private Document retrievePostResponseInternal(String urlString, final String request, boolean expectResponse,
                                                  int redirectCounter, StringBuilder txtHolder) throws JDOMException, RemoteApiException {
        return retrievePostResponseInternalImpl(urlString, new PostMethodPreparer() {

            public void prepare(PostMethod postMethod) throws UnsupportedEncodingException {
                if (request != null && !"".equals(request)) {
                    postMethod.setRequestEntity(new StringRequestEntity(request, "application/xml", "UTF-8"));
                }
            }
        }, expectResponse, redirectCounter, txtHolder);
    }

    protected Document retrievePostResponseInternalImpl(String urlString, PostMethodPreparer postMethodPreparer,
                                                        boolean expectResponse, int redirectCounter, StringBuilder txtHolder)
            throws JDOMException, RemoteApiException {
        Document doc = null;
        String baseUrl = urlString;

        for (int i = 0; i <= MAX_REDIRECTS; i++) {
            try {
                UrlUtil.validateUrl(baseUrl);
                setUrl(baseUrl);
            } catch (MalformedURLException e) {
                throw new RemoteApiException(e.getMessage(), e);
            }

            synchronized (clientLock) {
                HttpClient client;
                try {
                    client = callback.getHttpClient(server);
                } catch (HttpProxySettingsException e) {
                    throw new RemoteApiException("Connection error. Please set up HTTP Proxy settings", e);
                }

                PostMethod method = new PostMethod(baseUrl);

                try {
                    // method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
                    method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
                    method.getParams().setSoTimeout(client.getParams().getSoTimeout());
                    method.addRequestHeader("Accept", "application/xml");

                    callback.configureHttpMethod(this, method);
                    postMethodPreparer.prepare(method);

                    client.executeMethod(method);

                    final int httpStatus = method.getStatusCode();
                    if (httpStatus == HttpStatus.SC_NO_CONTENT) {
                        return doc;
                    } else if (httpStatus == HttpStatus.SC_MOVED_PERMANENTLY
                            || httpStatus == HttpStatus.SC_MOVED_TEMPORARILY) {

                        Header newLocation = method.getResponseHeader("Location");
                        if (newLocation == null) {
                            throw new RemoteApiException(
                                    "Connection error. Received redirection without new target address");
                        }
                        String lUrl = newLocation.getValue();
                        if (lUrl.endsWith("/success")) {
                            String newBaseUrl = lUrl.substring(0, lUrl.lastIndexOf("/success"));
                            if (!baseUrl.startsWith(newBaseUrl)) {
                                // need to login to make sure HttpClient picks up the session cookie
                                baseUrl = newBaseUrl + "/";
                                continue;
                            }
                        } else if (lUrl.endsWith("/JiraLockedError")) {
                            throw new RemoteApiException("JIRA is locked. Please contact your JIRA administrator.");

						} else {
                            throw new RemoteApiException(
                                    "Connection error. Received too many redirects (more than " + MAX_REDIRECTS + ")");
                        }

                    } else if (httpStatus == HttpStatus.SC_FORBIDDEN) {
                        final String errorDescription = "HTTP " + HttpStatus.SC_FORBIDDEN + " ("
                                + HttpStatus.getStatusText(HttpStatus.SC_FORBIDDEN) + ")";
                        LoggerImpl.getInstance().info(errorDescription + "\n" + method.getStatusText());

                        throw new RemoteApiException(errorDescription, new Exception(method.getResponseBodyAsString()));
                    } else if (httpStatus != HttpStatus.SC_OK
                            && httpStatus != HttpStatus.SC_CREATED
                            && !method.getResponseBodyAsString().startsWith("<html>")) {

                        Document document;
                        SAXBuilder builder = new SAXBuilder();
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
                        String response = writer.toString();
                        if (txtHolder != null) {
                            txtHolder.append(response);
                        }
                        document = builder.build(new ByteArrayInputStream(response.getBytes()));
                        throw buildExceptionText(method.getStatusCode(), document);

                    } else if (httpStatus == HttpStatus.SC_NOT_ACCEPTABLE) {
                        final String errorDescription = "HTTP " + httpStatus + " ("
                                + "Authentication failed (probably invalid username or password)."
                                + HttpStatus.getStatusText(httpStatus) + ")";
                        LoggerImpl.getInstance().info(errorDescription + "\n" + method.getStatusText());
                        throw new RemoteApiException(errorDescription, new Exception(method.getResponseBodyAsString()));

                        //RECEIVED STATUS AS html
                    } else if (httpStatus != HttpStatus.SC_OK && httpStatus != HttpStatus.SC_CREATED) {
                        final String errorDescription = "HTTP " + httpStatus + " ("
                                + HttpStatus.getStatusText(httpStatus) + ")";
                        LoggerImpl.getInstance().info(errorDescription + "\n" + method.getStatusText());
                        throw new RemoteApiException(errorDescription, new Exception(method.getResponseBodyAsString()));
                    }

                    this.responseCharSet = method.getResponseCharSet();
                    if (expectResponse) {
                        SAXBuilder builder = new SAXBuilder();
                        doc = builder.build(method.getResponseBodyAsStream());
                        preprocessResult(doc);
                    }
                    break;

                } catch (NullPointerException e) {
                    throw new RemoteApiException("Connection error to [" + baseUrl + "]", e);
                } catch (IOException e) {
                    throw new RemoteApiException(e.getMessage(), e);
                    // TODO PLE-1245 we may need below extended description for some reason (if yes, then restore it)
                    // throw new RemoteApiException(IOException.class.getSimpleName() + " encountered while posting data to ["
                    // + urlString + "]: " + e.getMessage(), e);
                } finally {
                    preprocessMethodResult(method);
                    method.releaseConnection();
                }
            }
        }
        return doc;
    }

    protected Document retrievePostResponseWithForm(String urlString, final Map<String, String> form, boolean expectResponse)
            throws JDOMException, RemoteApiException {
        return retrievePostResponseInternalImpl(urlString, new PostMethodPreparer() {

            public void prepare(PostMethod postMethod) throws UnsupportedEncodingException {
                if (form != null) {
                    for (Map.Entry<String, String> formEntry : form.entrySet()) {
                        postMethod.addParameter(formEntry.getKey(), formEntry.getValue());
                    }
                }
            }
        }, expectResponse, 0, null);
    }

    /**
     * This method will connect to server, and return the results of the push You must set Query first, which is the
     * contents of your XML file
     */
    protected Document retrievePostResponse(String urlString, Part[] parts, boolean expectResponse)
            throws JDOMException, RemoteApiException {
        return retrievePostResponseInternal(urlString, parts, expectResponse, 0);
    }

    private Document retrievePostResponseInternal(String urlString, Part[] parts,
                                                  boolean expectResponse, int redirectCounter)
            throws JDOMException, RemoteApiException {

        Document doc = null;

        synchronized (clientLock) {
            HttpClient client;
            try {
                client = callback.getHttpClient(server);
            } catch (HttpProxySettingsException e) {
                throw new RemoteApiException("Connection error to [" + urlString
                        + "]. Please set up HTTP Proxy settings", e);
            }

            PostMethod method = new PostMethod(urlString);

            try {
                //create new post method, and set parameters

                method.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
                method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

                // Create the multi-part request
                method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
                callback.configureHttpMethod(this, method);

                client.executeMethod(method);
                final int httpStatus = method.getStatusCode();
                if (httpStatus == HttpStatus.SC_NO_CONTENT) {
                    return doc;
                } else if (httpStatus == HttpStatus.SC_MOVED_PERMANENTLY
                        || httpStatus == HttpStatus.SC_MOVED_TEMPORARILY) {
                    if (redirectCounter < MAX_REDIRECTS) {
                        Header newLocation = method.getResponseHeader("Location");
                        if (newLocation == null) {
                            throw new RemoteApiException(
                                    "Connection error. Received redirection without new target address");
                        }
                        return retrievePostResponseInternal(
                                newLocation.getValue(), parts, expectResponse, redirectCounter + 1);
                    } else {
                        throw new RemoteApiException(
                                "Connection error. Received too many redirects (more than " + MAX_REDIRECTS + ")");
                    }
                } else if (httpStatus != HttpStatus.SC_OK && httpStatus != HttpStatus.SC_CREATED) {

                    Document document;
                    SAXBuilder builder = new SAXBuilder();
                    document = builder.build(method.getResponseBodyAsStream());
                    throw buildExceptionText(method.getStatusCode(), document);
                }

                if (expectResponse) {
                    SAXBuilder builder = new SAXBuilder();
                    doc = builder.build(method.getResponseBodyAsStream());
                    preprocessResult(doc);
                }
            } catch (NullPointerException e) {
                throw new RemoteApiException("Connection error to [" + urlString + "]", e);
            } catch (IOException e) {
                throw new RemoteApiException(IOException.class.getSimpleName() + " encountered while posting data to ["
                        + urlString + "]: " + e.getMessage(), e);
            } finally {
                preprocessMethodResult(method);
                method.releaseConnection();
            }
        }
        return doc;
    }

    private RemoteApiException buildExceptionText(final int statusCode, final Document document) throws JDOMException {
        StringBuilder textBuilder = new StringBuilder().append("Server returned HTTP ")
                .append(statusCode)
                .append(" (")
                .append(HttpStatus.getStatusText(statusCode))
                .append(")\n")
                .append("Reason: ");

        {
            XPath xpath = XPath.newInstance("error/code");
            @SuppressWarnings("unchecked")
			final List<Element> nodes = (List<Element>) xpath.selectNodes(document);
            if (nodes != null && !nodes.isEmpty()) {
                textBuilder.append(nodes.get(0).getValue()).append(" ");
            }
        }

        {
            XPath xpath = XPath.newInstance("error/message");
            @SuppressWarnings("unchecked")
			final List<Element> messages = (List<Element>) xpath.selectNodes(document);
            if (messages != null && !messages.isEmpty()) {
                textBuilder.append("\nMessage: ").append(messages.get(0).getValue());
            }
        }

        {
            XPath xpath = XPath.newInstance("status/message");
            @SuppressWarnings("unchecked")
			final List<Element> messages = (List<Element>) xpath.selectNodes(document);
            if (messages != null && !messages.isEmpty()) {
                textBuilder.append("\nMessage: ").append(messages.get(0).getValue());
            }
        }

        String serverStackTrace = null;
        {
            XPath xpath = XPath.newInstance("error/stacktrace");
            @SuppressWarnings("unchecked")
			final List<Element> nodes = (List<Element>) xpath.selectNodes(document);
            if (nodes != null && !nodes.isEmpty()) {
                serverStackTrace = "\nStacktrace from the server:\n";
                serverStackTrace += nodes.get(0).getValue();
            }
        }

        return new RemoteApiException(textBuilder.toString(), serverStackTrace);
    }

    protected Document retrieveDeleteResponse(String urlString, boolean expectResponse) throws IOException,
            JDOMException, RemoteApiSessionExpiredException {
        return retrieveDeleteResponseInternal(urlString, expectResponse, 0);
    }

    protected Document retrieveDeleteResponseInternal(String urlString, boolean expectResponse, int redirectCounter)
            throws IOException, JDOMException, RemoteApiSessionExpiredException {
        UrlUtil.validateUrl(urlString);

        Document doc = null;
        synchronized (clientLock) {
            HttpClient client;
            try {
                client = callback.getHttpClient(server);
            } catch (HttpProxySettingsException e) {
                throw createIOException("Connection error. Please set up HTTP Proxy settings", e);
            }

            DeleteMethod method = new DeleteMethod(urlString);

            try {
                // method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
                method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
                method.getParams().setSoTimeout(client.getParams().getSoTimeout());
                callback.configureHttpMethod(this, method);

                client.executeMethod(method);

                int statusCode = method.getStatusCode();
                if (statusCode == HttpStatus.SC_NO_CONTENT) {
                    return null;
                }
                if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
                        || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                    if (redirectCounter < MAX_REDIRECTS) {
                        Header newLocation = method.getResponseHeader("Location");
                        if (newLocation == null) {
                            throw new IOException(
                                    "Connection error. Received redirection without new target address");
                        }
                        return retrieveDeleteResponseInternal(
                                newLocation.getValue(), expectResponse, redirectCounter + 1);
                    } else {
                        throw new IOException(
                                "Connection error. Received too many redirects (more than " + MAX_REDIRECTS + ")");
                    }
                }
                if (method.getStatusCode() != HttpStatus.SC_OK) {
                    throw new IOException("HTTP status code " + method.getStatusCode() + ": " + method.getStatusText());
                }

                if (expectResponse) {
                    SAXBuilder builder = new SAXBuilder();
                    doc = builder.build(method.getResponseBodyAsStream());
                    preprocessResult(doc);
                }
            } catch (NullPointerException e) {
                throw createIOException("Connection error", e);
            } finally {
                method.releaseConnection();
            }
        }
        return doc;
    }

    protected abstract void adjustHttpHeader(HttpMethod method);

    protected abstract void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException;

    protected abstract void preprocessMethodResult(HttpMethod method) throws RemoteApiException, ServiceUnavailableException;

    public static String getServerNameFromUrl(String urlString) {
        int pos = urlString.indexOf("://");
        if (pos != -1) {
            urlString = urlString.substring(pos + 1 + 2);
        }
        pos = urlString.indexOf("/");
        if (pos != -1) {
            urlString = urlString.substring(0, pos);
        }
        return urlString;
    }
}
