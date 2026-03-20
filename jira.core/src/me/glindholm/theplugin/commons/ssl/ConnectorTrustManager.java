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

package me.glindholm.theplugin.commons.ssl;

import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * X509ExtendedTrustManager that first validates against the JVM default trust store (cacerts).
 * If that fails it pops up an SWT MessageDialog asking the user whether to accept the
 * certificate for this session (no persistence).
 *
 * Extending X509ExtendedTrustManager (rather than X509TrustManager) is important: Java's
 * SSLContext wraps plain X509TrustManager instances in AbstractTrustManagerWrapper which
 * runs its own independent PKIX check after ours, defeating the purpose. Extended managers
 * are used as-is.
 */
public class ConnectorTrustManager extends X509ExtendedTrustManager {

    /** Certs accepted for the lifetime of this JVM session only. */
    private static final Set<String> SESSION_ACCEPTED = Collections.synchronizedSet(new HashSet<>());

    /**
     * Certs for which a dialog has already been posted (asyncExec) but not yet answered,
     * so we don't flood the user with duplicate dialogs.
     */
    private static final Set<String> PENDING_PROMPT = Collections.synchronizedSet(new HashSet<>());

    private final String serverHost;
    private final X509TrustManager defaultTrustManager;

    /**
     * @param serverHost the host name shown in the accept/reject dialog
     * @param trustStore the key store to validate against, or {@code null} for the JVM default (cacerts)
     */
    public ConnectorTrustManager(final String serverHost, final KeyStore trustStore)
            throws NoSuchAlgorithmException, KeyStoreException {
        this.serverHost = serverHost;

        final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(trustStore); // null → JVM default cacerts
        X509TrustManager found = null;
        for (final TrustManager tm : factory.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                found = (X509TrustManager) tm;
                break;
            }
        }
        if (found == null) {
            throw new NoSuchAlgorithmException("No X509TrustManager found"); //$NON-NLS-1$
        }
        this.defaultTrustManager = found;
    }

    // ── client-side (unused in our scenario, just delegate) ──────────────────

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        defaultTrustManager.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket) throws CertificateException {
        checkClientTrusted(chain, authType);
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) throws CertificateException {
        checkClientTrusted(chain, authType);
    }

    // ── server-side ──────────────────────────────────────────────────────────

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        checkServerTrustedInternal(chain, authType);
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket) throws CertificateException {
        checkServerTrustedInternal(chain, authType);
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) throws CertificateException {
        checkServerTrustedInternal(chain, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return defaultTrustManager.getAcceptedIssuers();
    }

    // ─────────────────────────────────────────────────────────────────────────

    /** System property that, when set to {@code true}, automatically accepts self-signed certificates. */
    private static final String PROP_ACCEPT_SELF_SIGNED = "me.glindholm.jira.self-signed"; //$NON-NLS-1$

    private void checkServerTrustedInternal(final X509Certificate[] chain, final String authType) throws CertificateException {
        try {
            defaultTrustManager.checkServerTrusted(chain, authType);
            return; // trusted by cacerts — done
        } catch (final CertificateException e) {
            final String certKey = chain[0].getSubjectX500Principal().getName()
                    + "|" + chain[0].getSerialNumber(); //$NON-NLS-1$

            if (SESSION_ACCEPTED.contains(certKey)) {
                return; // user already accepted this session
            }

            // Auto-accept self-signed certificates when the system property is set
            if (isSelfSigned(chain[0]) && Boolean.getBoolean(PROP_ACCEPT_SELF_SIGNED)) {
                SESSION_ACCEPTED.add(certKey);
                return;
            }

            // Build a human-readable reason
            String reason = "The certificate is not trusted by the JVM trust store."; //$NON-NLS-1$
            if (isSelfSigned(chain[0])) {
                reason = "The certificate is self-signed."; //$NON-NLS-1$
            }
            try {
                chain[0].checkValidity();
            } catch (final CertificateExpiredException ex) {
                reason = "The certificate has expired."; //$NON-NLS-1$
            } catch (final CertificateNotYetValidException ex) {
                reason = "The certificate is not yet valid."; //$NON-NLS-1$
            }

            final String message = "The SSL certificate for\n\n  " + serverHost //$NON-NLS-1$
                    + "\n\ncannot be verified:\n\n  " + reason //$NON-NLS-1$
                    + "\n\nDo you want to accept it for this session?"; //$NON-NLS-1$

            final Display display = PlatformUI.isWorkbenchRunning()
                    ? PlatformUI.getWorkbench().getDisplay() : Display.getCurrent();

            if (display == null || display.isDisposed()) {
                throw e; // no UI available — reject
            }

            // Always post the dialog asynchronously — never block the caller.
            // Whether this is the UI thread mid-render or a background thread, we must
            // not block: blocking the UI thread freezes Eclipse, blocking a background
            // thread can deadlock if the UI thread is waiting on the same connection.
            // The current attempt always fails (throw); once the user clicks Yes the
            // cert is in SESSION_ACCEPTED and the next attempt succeeds.
            if (PENDING_PROMPT.add(certKey)) { // false if a dialog is already queued
                display.asyncExec(() -> {
                    PENDING_PROMPT.remove(certKey);
                    if (!display.isDisposed()) {
                        if (MessageDialog.openQuestion(display.getActiveShell(),
                                "Untrusted SSL Certificate", message)) { //$NON-NLS-1$
                            SESSION_ACCEPTED.add(certKey);
                        }
                    }
                });
            }

            throw e;
        }
    }

    private static boolean isSelfSigned(final X509Certificate cert) {
        return cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal());
    }
}