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

package com.atlassian.theplugin.commons.ssl;

import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.awt.*;
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

public class ConnectorTrustManager implements X509TrustManager {
    private static final Collection<String> ALREADY_REJECTED_CERTS = new HashSet<String>();
    private static Collection<String> temporarilyAcceptedCerts =
            Collections.synchronizedCollection(new HashSet<String>());

    private final GeneralConfigurationBean configuration;
    private CertMessageDialog certMessageDialog;
    private X509TrustManager standardTrustManager;

    private static ThreadLocal<String> url = new ThreadLocal<String>();


    public static String getUrl() {
        return url.get();
    }


    public ConnectorTrustManager(GeneralConfigurationBean configuration, CertMessageDialog certMessageDialog,
                                 KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException {
        this.configuration = configuration;
        this.certMessageDialog = certMessageDialog;

        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore);
        TrustManager[] trustmanagers = factory.getTrustManagers();

        //looking for a X509TrustManager instance
        for (TrustManager trustmanager : trustmanagers) {
            if (trustmanager instanceof X509TrustManager) {
                standardTrustManager = (X509TrustManager) trustmanager;
                return;
            }
        }

        if (standardTrustManager == null) {
            throw new NoSuchAlgorithmException("cannot retrieve default trust manager found");
        }

    }

    //checkClientTrusted
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        standardTrustManager.checkClientTrusted(chain, authType);
    }


    private boolean isSelfSigned(X509Certificate certificate) {
        return certificate.getSubjectDN().equals(certificate.getIssuerDN());
    }

    //checkServerTrusted
    public void checkServerTrusted(final X509Certificate[] chain, String authType) throws CertificateException {
        try {
            standardTrustManager.checkServerTrusted(chain, authType);
        } catch (final CertificateException
                e) {
            synchronized (ConnectorTrustManager.class) {
                String strCert = chain[0].toString();
                if (ALREADY_REJECTED_CERTS.contains(strCert)) {
                    throw e;
                }

                if (checkChain(chain, configuration.getCerts())
                        ||
                        checkChain(chain, temporarilyAcceptedCerts)) {
                    return;
                }

                String message = e.getMessage();
                message = message.substring(message.lastIndexOf(":") + 1);
                if (isSelfSigned(chain[0])) {
                    message = "Self-signed certificate";
                }
                try {
                    chain[0].checkValidity();
                } catch (CertificateExpiredException e1) {
                    message = "Certificate expired";
                } catch (CertificateNotYetValidException e1) {
                    message = "Certificate not yet valid";
                }
                final String server = AbstractHttpSession.getUrl().getHost();

                // check if it should be accepted
                final int[] accepted = new int[]{0}; // 0 rejected 1 accepted temporarily 2 - accepted perm.

                try {
                    final String message1 = message;
                    EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
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
                        }
                    });
                } catch (InterruptedException e1) {
                    // swallow
                } catch (InvocationTargetException e1) {
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

    private boolean checkChain(X509Certificate[] chain, Collection<String> certs) {
        for (X509Certificate cert : chain) {
            if (certs.contains(cert.toString())) {
                return true;
            }
        }
        return false;
    }

    //getAcceptedIssuers
    public X509Certificate[] getAcceptedIssuers() {
        return standardTrustManager.getAcceptedIssuers();
    }

}
