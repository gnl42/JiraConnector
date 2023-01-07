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
package me.glindholm.theplugin.commons.bamboo.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.xpath.XPath;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.exception.HttpProxySettingsException;
import me.glindholm.theplugin.commons.remoteapi.CaptchaRequiredException;
import me.glindholm.theplugin.commons.remoteapi.ProductSession;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import me.glindholm.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import me.glindholm.theplugin.commons.remoteapi.rest.HttpSessionCallback;

/**
 * @author Jacek Jaroczynski
 */
public class LoginBambooSession extends AbstractHttpSession implements ProductSession {

    protected String authToken;

    private static final String AUTHENTICATION_ERROR_MESSAGE = "User not authenticated yet, or session timed out";
    private static final String LOGIN_ACTION = "/api/rest/login.action";
    private static final String LOGOUT_ACTION = "/api/rest/logout.action";

    public LoginBambooSession(final ConnectionCfg serverData, final HttpSessionCallback callback) throws RemoteApiMalformedUrlException {
        super(serverData, callback);
    }

    /**
     * Connects to Bamboo server instance. On successful login authentication token
     * is returned from server and stored in Bamboo session for subsequent calls.
     * <p/>
     * The exception returned may have the getCause() examined for to get the actual
     * exception reason.<br>
     * If the exception is caused by a valid error response from the server (no
     * IOEXception, UnknownHostException, MalformedURLException or JDOMException),
     * the
     * {@link me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginFailedException}
     * is actually thrown. This may be used as a hint that the password is invalid.
     *
     * @param name      username defined on Bamboo server instance
     * @param aPassword for username
     * @throws me.glindholm.theplugin.commons.remoteapi.RemoteApiLoginException on
     *                                                                          connection
     *                                                                          or
     *                                                                          authentication
     *                                                                          errors
     */
    @Override
    @Deprecated
    public void login(final String name, final char[] aPassword) throws RemoteApiLoginException {
        String loginUrl, host = null;

        if (name == null || aPassword == null) {
            throw new RemoteApiLoginException("Corrupted configuration. Username or Password null");
        }
        final String pass = String.valueOf(aPassword);

        loginUrl = getBaseUrl() + LOGIN_ACTION;

        try {
            try {
                host = new URL(getBaseUrl()).getHost();
            } catch (final MalformedURLException e) {
                throw new RemoteApiException(e.getMessage(), e);
            }

            callback.getHttpClient(getServer()).getState().clearCookies();
            final Document doc = retrievePostResponseInternalImpl(loginUrl, new PostMethodPreparer() {
                @Override
                public void prepare(final PostMethod login) throws UnsupportedEncodingException {
                    login.addRequestHeader("Accept", "application/xml;q=0.9,*/*");
                    login.addParameter("os_username", name); //$NON-NLS-1$
                    login.addParameter("os_password", pass); //$NON-NLS-1$
                    login.addParameter("username", name); //$NON-NLS-1$
                    login.addParameter("password", pass); //$NON-NLS-1$
                }
            }, true, 0, null);

            final String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiLoginFailedException(exception);
            }

            @SuppressWarnings("unchecked")
            final List<Element> elements = (List<Element>) XPath.newInstance("/response/auth").selectNodes(doc);
            if (elements == null || elements.size() == 0) {
                throw new RemoteApiLoginException("Server did not return any authentication token");
            }
            if (elements.size() != 1) {
                throw new RemoteApiLoginException("Server returned unexpected number of authentication tokens (" + elements.size() + ")");
            }
            authToken = elements.get(0).getText();
        } catch (final RemoteApiLoginFailedException e) {
            throw e;
        } catch (final RemoteApiException e) {
            if (e.getCause() != null && e.getCause().getMessage().contains("maximum")) {
                throw new CaptchaRequiredException(e);
            } else if (e.getCause() instanceof UnknownHostException) {
                throw new RemoteApiLoginException("Unknown host: " + host, e);
            } else if (e.getCause() instanceof MalformedURLException) {
                throw new RemoteApiLoginException("Malformed server URL: " + getBaseUrl(), e);
            }

            throw new RemoteApiLoginException(e.getMessage(), e);
        } catch (final JDOMException e) {
            throw new RemoteApiLoginException("Server returned malformed response", e);
        } catch (final IllegalArgumentException e) {
            throw new RemoteApiLoginException("Malformed server URL: " + getBaseUrl(), e);
        } catch (final HttpProxySettingsException e) {
            throw new RemoteApiLoginException(e.getMessage(), e);
        }
        // catch (IOException e) {
        // if (e.getCause() != null && e.getCause().getMessage().contains("maximum")) {
        // throw new CaptchaRequiredException(e);
        // }
        // throw new RemoteApiLoginException(e.getMessage(), e);
        // }
    }

    @Override
    @Deprecated
    public void logout() {
        if (!isLoggedIn()) {
            return;
        }

        try {
            final String logoutUrl = getBaseUrl() + LOGOUT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
            retrieveGetResponse(logoutUrl);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem", e);
        } catch (final IOException | JDOMException | RemoteApiSessionExpiredException e) {
            /* ignore errors on logout */
        }

        authToken = null;
    }

    @Override
    public boolean isLoggedIn() {
        return authToken != null;
    }

    @Override
    protected void preprocessResult(final Document doc) throws JDOMException, RemoteApiSessionExpiredException {
        final String error = getExceptionMessages(doc);
        if (error != null) {
            if (error.startsWith(AUTHENTICATION_ERROR_MESSAGE)) {
                throw new RemoteApiSessionExpiredException("Session expired.");
            }
        }
    }

    @Override
    protected void preprocessMethodResult(final HttpMethod method) {
    }

    protected static String getExceptionMessages(final Document doc) throws JDOMException {
        final XPath xpath = XPath.newInstance("/errors/error");
        @SuppressWarnings("unchecked")
        final List<Element> elements = (List<Element>) xpath.selectNodes(doc);

        if (elements != null && elements.size() > 0) {
            final StringBuilder exceptionMsg = new StringBuilder();
            for (final Element e : elements) {
                exceptionMsg.append(e.getText());
                exceptionMsg.append("\n");
            }
            return exceptionMsg.toString();
        } else {
            /* no exception */
            return null;
        }
    }

    @Override
    protected void adjustHttpHeader(final HttpMethod method) {
        // do not do it here it's forbidden and breakes ACE
        // method.addRequestHeader(new Header("Authorization", getAuthHeaderValue()));
    }

//	private String getAuthHeaderValue() {
//		return "Basic " + StringUtil.encode(getUsername() + ":" + getPassword());
//	}
}
