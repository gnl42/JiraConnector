/*
 * Copyright (C) 2012 Atlassian
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
 */
package me.glindholm.jira.rest.client.internal.async;

import java.net.URI;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import me.glindholm.jira.rest.client.api.AuthenticationHandler;

/**
 * Factory for asynchronous http clients using native Java 21 HttpClient.
 *
 * @since v2.0
 */
public class AsynchronousHttpClientFactory {
    public DisposableHttpClient createUrlValidationClient(final URI serverUri) {
        final HttpClient httpClient = createHttpClient(Duration.ofMillis(500));
        return createDisposableClient(httpClient, null);
    }


    public DisposableHttpClient createClient(final URI serverUri, final AuthenticationHandler authenticationHandler) {
        final HttpClient httpClient = createHttpClient(Duration.ofSeconds(30));
        return createDisposableClient(httpClient, authenticationHandler);
    }

    private HttpClient createHttpClient(final Duration timeout) {
        try {
            // getTrustManagers() prefers SSLContext.getDefault() which at Eclipse runtime
            // is overridden by org.eclipse.equinox.security and already covers both the
            // JVM CA store and the Eclipse user-accepted certificate store.
            final X509TrustManager defaultTm = Arrays.stream(getTrustManagers(null))
                    .filter(X509TrustManager.class::isInstance)
                    .map(X509TrustManager.class::cast)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No X509TrustManager found")); //$NON-NLS-1$

            final X509TrustManager selfSignedTm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return defaultTm.getAcceptedIssuers();
                }

                @Override
                public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                    defaultTm.checkClientTrusted(chain, authType);
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                    try {
                        defaultTm.checkServerTrusted(chain, authType);
                    } catch (final CertificateException e) {
                        // Accept if the certificate is self-signed (issuer == subject)
                        if (chain.length == 1) {
                            final X509Certificate cert = chain[0];
                            if (cert.getIssuerX500Principal().equals(cert.getSubjectX500Principal())) {
                                try {
                                    cert.verify(cert.getPublicKey());
                                } catch (final Exception ex) {
                                    throw new CertificateException("Self-signed certificate signature is invalid", ex); //$NON-NLS-1$
                                }
                                return; // self-signed and signature is valid
                            }
                        }
                        throw e; // not self-signed — rethrow the original error
                    }
                }
            };

            final SSLContext sslContext = SSLContext.getInstance("TLS"); //$NON-NLS-1$
            sslContext.init(null, new TrustManager[] { selfSignedTm }, new SecureRandom());
            return HttpClient.newBuilder()
                    .connectTimeout(timeout)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .sslContext(sslContext)
                    .build();
        } catch (final NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create HTTP client with custom SSL context", e); //$NON-NLS-1$
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialise trust manager", e); //$NON-NLS-1$
        }
    }

    private static TrustManager[] getTrustManagers(final KeyStore keyStore) throws NoSuchAlgorithmException, java.security.KeyStoreException {
        // Prefer SSLContext.getDefault() which Eclipse overrides at startup via
        // org.eclipse.equinox.security to include the Eclipse user-accepted certificate
        // store in addition to the JVM CA store.
        try {
            final TrustManager[] tms = getX509TrustManagersFromContext(SSLContext.getDefault());
            if (tms != null && tms.length > 0) {
                return tms;
            }
        } catch (final Exception ignored) {
        }
        // Outside Eclipse: fall back to the standard JVM TrustManagerFactory.
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        return tmf.getTrustManagers();
    }

    private static TrustManager[] getX509TrustManagersFromContext(final SSLContext ctx) {
        // SSLContext does not expose TrustManagers directly, so re-initialise a fresh
        // TrustManagerFactory using the default algorithm. When running inside Eclipse,
        // org.eclipse.equinox.security registers its own provider as the default so
        // this factory will include Eclipse's accepted-certificate store automatically.
        try {
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            return tmf.getTrustManagers();
        } catch (final Exception e) {
            return new TrustManager[0];
        }
    }

    public DisposableHttpClient createClient(final URI serverUri, final AuthenticationHandler authenticationHandler, final HttpClient httpClient) {
        return createDisposableClient(httpClient, authenticationHandler);
    }

    public DisposableHttpClient createClient(final HttpClient client) {
        return createDisposableClient(client, null);
    }

    private DisposableHttpClient createDisposableClient(final HttpClient httpClient, final AuthenticationHandler authenticationHandler) {
        return new AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
            @Override
            public void destroy() throws Exception {
                // Java 21 HttpClient manages its own lifecycle; nothing to dispose explicitly.
            }
        };
    }
}
