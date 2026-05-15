/*******************************************************************************
 * Copyright (c) 2013, 2024 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *     Tasktop Technologies - initial API and implementation
 *     ArSysOp - ongoing support
 *     See git history
 *******************************************************************************/

package org.eclipse.mylyn.commons.sdk.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Provides URLs for testing connections.
 *
 * @author Steffen Pingel
 */
@SuppressWarnings("nls")
public class TestUrl {

	public static final TestUrl DEFAULT = probeLocalhost();

	private final String URL_HTTPS_OK = TestConfiguration.getUrlServicesCiDefault().toString() + "/";

	private final String URL_HTTP_OK = URL_HTTPS_OK;

	private final String URL_HTTP_404_NOT_FOUND = URL_HTTPS_OK + "notfound";

	private final String URL_HTTP_CONNECTION_REFUSED = TestConfiguration.getUrlServicesCiDefault().toString()
			+ ":9999/";

	private final String URL_HTTP_CONNECTION_TIMEOUT = "http://google.com:9999/";

	private final String URL_HTTP_UNKNOWN_HOST = "http://nonexistant.mylyn.org";


	private final String host;

	public URL getConnectionRefused() {
		return createUrl(URL_HTTP_CONNECTION_REFUSED);
	}

	private static TestUrl probeLocalhost() {
		try (var socket = new Socket()) {
			socket.connect(new InetSocketAddress("localhost", 2080), 100);
			return new TestUrl("localhost");
		} catch (final IOException e) {
			return new TestUrl(null);
		}
	}

	public URL getConnectionTimeout() {
		return createUrl(URL_HTTP_CONNECTION_TIMEOUT);
	}

	public URL getHttpNotFound() {
		return createUrl(URL_HTTP_404_NOT_FOUND);
	}

	public URL getHttpOk() {
		return createUrl(URL_HTTP_OK);
	}

	public URL getHttpsOk() {
		return createUrl(URL_HTTPS_OK);
	}

	public URL getUnknownHost() {
		return createUrl(URL_HTTP_UNKNOWN_HOST);
	}

	private URL createUrl(String url) {
		if (host != null) {
			url = url.replace("mylyn.org", host);
		}
		try {
			return new URI(url).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private TestUrl(final String host) {
		this.host = host;
	}

}
