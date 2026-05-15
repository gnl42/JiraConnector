/*******************************************************************************
 * Copyright (c) 2011, 2024 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *     Tasktop Technologies - initial API and implementation
 *     Guy Perron - add Windows support
 *     ArSysOp - ongoing support
 *     See git history
 *******************************************************************************/

package org.eclipse.mylyn.commons.sdk.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.core.runtime.Assert;
import org.eclipse.osgi.util.NLS;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * @author Steffen Pingel
 */
@SuppressWarnings("nls")
public class TestConfiguration {

	static final String URL_SERVICES_LOCALHOST = System.getProperty("localhost.test.server", "https://mylyn.local");

	static final String URL_SERVICES_DEFAULT = System.getProperty("mylyn.test.server", "https://mylyn.frank-becker.de");

	static final String URL_SERVICES_CI_DEFAULT = System.getProperty("mylyn.test.server.ci",
			"https://mylyn.frank-becker.de");

	public static TestConfiguration defaultConfiguration;

	public static TestConfiguration getDefault() {
		if (defaultConfiguration == null) {
			defaultConfiguration = new TestConfiguration();
			defaultConfiguration.setDefaultOnly(CommonTestUtil.runHeartbeatTestsOnly());
		}
		return defaultConfiguration;
	}

	public static void setDefault(final TestConfiguration defaultConfiguration) {
		TestConfiguration.defaultConfiguration = defaultConfiguration;
	}

	private boolean localOnly;

	private boolean defaultOnly;

	private boolean headless;

	public boolean isDefaultOnly() {
		return defaultOnly;
	}

	public boolean isHeadless() {
		return headless;
	}

	public boolean isLocalOnly() {
		return localOnly;
	}

	public void setDefaultOnly(final boolean heartbeat) {
		defaultOnly = heartbeat;
	}

	public void setHeadless(final boolean headless) {
		this.headless = headless;
	}

	public void setLocalOnly(final boolean localOnly) {
		this.localOnly = localOnly;
	}

	public <T> List<T> discover(final Class<T> clazz, final String fixtureType) {
		return discover(clazz, fixtureType, isDefaultOnly());
	}

	public <T> T discoverDefault(final Class<T> clazz, final String fixtureType) {
		final List<T> fixtures = discover(clazz, fixtureType, true);
		if (fixtures.isEmpty()) {
			throw new RuntimeException(NLS.bind("No default fixture available for {0}", fixtureType));
		}
		return fixtures.get(0);
	}

	public <T> List<T> discover(final Class<T> clazz, final String fixtureType, final boolean defaultOnly) {
		List<T> fixtures = Collections.emptyList();
		final var exception = new Exception[1];

		if (!CommonTestUtil.ignoreLocalTestServices()) {
			try {
				final var file = CommonTestUtil.getFile(clazz, "local.json");
				fixtures = discover(file.toURI().toASCIIString(), "", clazz, fixtureType, defaultOnly, "local.json",
						exception);
			} catch (MylynResourceMissingException | IOException e) {
				// ignore
			}

			if (fixtures.isEmpty()) {
				fixtures = discover(URL_SERVICES_LOCALHOST + "/mylyn_idx/service", URL_SERVICES_LOCALHOST, clazz,
						fixtureType, defaultOnly, "URL_SERVICES_LOCALHOST", exception);
			}
		}
		if (fixtures.isEmpty() && !CommonTestUtil.ignoreGlobalTestServices()) {
			fixtures = discover(URL_SERVICES_DEFAULT + "/mylyn_idx/service", URL_SERVICES_DEFAULT, clazz, fixtureType,
					defaultOnly, "URL_SERVICES_DEFAULT", exception);
		}

		if (fixtures.isEmpty() && CommonTestUtil.runOnCIServerTestsOnly()) { //CommonTestUtil.runOnCIServerTestsOnly()) {
			throw new RuntimeException(
					NLS.bind("Failed to discover any fixtures for kind {0} with defaultOnly={1} ({2} and {3})",
							fixtureType, Boolean.toString(defaultOnly), URL_SERVICES_LOCALHOST, URL_SERVICES_DEFAULT),
					exception[0]);
		}
		return fixtures;
	}

	private static <T> List<T> discover(final String location, final String baseUrl, final Class<T> clazz, final String fixtureType,
			final boolean defaultOnly, final String kind, final Exception[] result) {
		Assert.isNotNull(fixtureType);
		final var configurations = getConfigurations(location, result);
		if (configurations != null) {
			for (final FixtureConfiguration configuration : configurations) {
				if (configuration != null) {
					if (configuration.getUrl() != null && !configuration.getUrl().startsWith("http")) {
						configuration.setUrl(baseUrl + configuration.getUrl());
					}
					configuration.getProperties().put("kind", kind);
				}
			}
			return loadFixtures(configurations, clazz, fixtureType, defaultOnly);
		}
		return Collections.emptyList();
	}

	private static <T> List<T> loadFixtures(final List<FixtureConfiguration> configurations, final Class<T> clazz,
			final String fixtureType, final boolean defaultOnly) {
		final List<T> result = new ArrayList<>();
		final var defaultOverwriteUrl = System.getProperty("mylyn.tests.configuration.url", "");
		for (final FixtureConfiguration configuration : configurations) {
			if (configuration != null && fixtureType.equals(configuration.getType())
					&& (!defaultOnly || defaultOverwriteUrl.equals("") && configuration.isDefault()
							|| configuration.url.equals(defaultOverwriteUrl))) {
				try {
					final var constructor = clazz.getConstructor(FixtureConfiguration.class);
					result.add(constructor.newInstance(configuration));
				} catch (final Exception e) {
					throw new RuntimeException("Unexpected error creating test fixture", e);
				}
			}
		}
		return result;
	}

	private static List<FixtureConfiguration> getConfigurations(final String url, final Exception[] result) {
		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = { new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
				}

				@Override
				public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
				}
			} };
			// Install the all-trusting trust manager
			final var sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// Create all-trusting host name verifier
			final HostnameVerifier allHostsValid = (hostname, session) -> true;
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			final var connection = new URL(url).openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			final var in = new InputStreamReader(connection.getInputStream());
			try (in) {
				final TypeToken<List<FixtureConfiguration>> type = new TypeToken<>() {
				};
				return new Gson().fromJson(in, type.getType());
			}
		} catch (final IOException e) {
			result[0] = new IOException("IOException accessing " + url, e);
			return null;
		} catch (final NoSuchAlgorithmException e) {
			result[0] = new NoSuchAlgorithmException("NoSuchAlgorithmException accessing " + url, e);
			return null;
		} catch (final KeyManagementException e) {
			result[0] = new KeyManagementException("KeyManagementException accessing " + url, e);
			return null;
		}
	}

	public static String getUrlServicesCiDefault() {
		return URL_SERVICES_CI_DEFAULT;
	}

}
