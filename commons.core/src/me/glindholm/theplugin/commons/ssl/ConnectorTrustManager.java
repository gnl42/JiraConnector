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

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import me.glindholm.theplugin.commons.configuration.GeneralConfigurationBean;
import me.glindholm.theplugin.commons.remoteapi.rest.AbstractHttpSession;

public class ConnectorTrustManager implements X509TrustManager {
    private static final Collection<String> ALREADY_REJECTED_CERTS = new HashSet<>();
    private static Collection<String> temporarilyAcceptedCerts = Collections.synchronizedCollection(new HashSet<>());

    private final GeneralConfigurationBean configuration;
    private CertMessageDialog certMessageDialog;
    private X509TrustManager standardTrustManager;

    private static ThreadLocal<String> url = new ThreadLocal<>();

    public static String getUrl() {
        return url.get();
    }

    public ConnectorTrustManager(final GeneralConfigurationBean configuration, final CertMessageDialog certMessageDialog, final KeyStore keyStore)
            throws NoSuchAlgorithmException, KeyStoreException {
        this.configuration = configuration;
        this.certMessageDialog = certMessageDialog;

        final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore);
        final TrustManager[] trustmanagers = factory.getTrustManagers();

        // looking for a X509TrustManager instance
        for (final TrustManager trustmanager : trustmanagers) {
            if (trustmanager instanceof X509TrustManager) {
                standardTrustManager = (X509TrustManager) trustmanager;
                return;
            }
        }

        if (standardTrustManager == null) {
            throw new NoSuchAlgorithmException("cannot retrieve default trust manager found");
        }

    }

    // checkClientTrusted
    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        standardTrustManager.checkClientTrusted(chain, authType);
    }

    private boolean isSelfSigned(final X509Certificate certificate) {
        return certificate.getSubjectDN().equals(certificate.getIssuerDN());
    }

    // checkServerTrusted
    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        try {
            standardTrustManager.checkServerTrusted(chain, authType);
        } catch (final CertificateException e) {
            synchronized (ConnectorTrustManager.class) {
                final String strCert = chain[0].toString();
                if (ALREADY_REJECTED_CERTS.contains(strCert)) {
                    throw e;
                }

                if (checkChain(chain, configuration.getCerts()) || checkChain(chain, temporarilyAcceptedCerts)) {
                    return;
                }

                String message = e.getMessage();
                message = message.substring(message.lastIndexOf(":") + 1);
                if (isSelfSigned(chain[0])) {
                    message = "Self-signed certificate";
                }
                try {
                    chain[0].checkValidity();
                } catch (final CertificateExpiredException e1) {
                    message = "Certificate expired";
                } catch (final CertificateNotYetValidException e1) {
                    message = "Certificate not yet valid";
                }
                final String server = AbstractHttpSession.getUrl().getHost();

                // check if it should be accepted
                final int[] accepted = { 0 }; // 0 rejected 1 accepted temporarily 2 - accepted perm.

                try {
                    final String message1 = message;
                    EventQueue.invokeAndWait(() -> {
                        if (certMessageDialog != null) {
                            certMessageDialog.show(server, message1, chain);
                            if (certMessageDialog.isOK()) {
                                if (certMessageDialog.isTemporarily()) {
                                    accepted[0] = 1;
                                    return;
                                }
                                accepted[0] = 2;
                            }
                        }
                    });
                } catch (final InterruptedException | InvocationTargetException e1) {
                    // swallow
                }

                switch (accepted[0]) {
                case 1:
                    temporarilyAcceptedCerts.add(strCert);
                    break;
                case 2:
                    synchronized (configuration) {
                        // taken once again because something could change in the state
                        configuration.addCert(strCert);
                    }
                    break;
                default:
                    synchronized (ALREADY_REJECTED_CERTS) {
                        ALREADY_REJECTED_CERTS.add(strCert);
                    }
                    throw e;
                }

            }

        }
    }

    private boolean checkChain(final X509Certificate[] chain, final Collection<String> certs) {
        for (final X509Certificate cert : chain) {
            if (certs.contains(cert.toString())) {
                return true;
            }
        }
        return false;
    }

    // getAcceptedIssuers
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return standardTrustManager.getAcceptedIssuers();
    }

}
