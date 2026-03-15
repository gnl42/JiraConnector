package me.glindholm.theplugin.commons.thirdparty.apache;

import static java.lang.System.getProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Provides SSL context building utilities (self-signed certificate support).
 * Legacy socket factory methods removed; now only used for SSLContext/KeyStore creation.
 */
public class EasySSLProtocolSocketFactory {

    public final static int SSL_PORT = 443;
    private SSLContext sslcontext = null;

    public EasySSLProtocolSocketFactory() {
    }

    private SSLContext createEasySSLContext() {
        KeyManagerFactory kmf = null;
        try {
            final KeyStore keyStore = getKeyStore();
            final String p = getProperty("javax.net.ssl.keyStorePassword");
            if (keyStore != null && p != null) {
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, p.toCharArray());
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keyStore);
            }
        } catch (final KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
        }

        try {
            final SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf != null ? kmf.getKeyManagers() : null, new TrustManager[] { getTrustManager() }, kmf != null ? new SecureRandom() : null);
            return context;
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create SSL context: " + e.getMessage(), e);
        }
    }

    public static KeyStore getKeyStore() {
        final KeyStore keyJKSStore = getKeyJKSStore(getProperty("javax.net.ssl.keyStore"), getProperty("javax.net.ssl.keyStorePassword"));
        final KeyStore trust = getKeyJKSStore(getProperty("javax.net.ssl.trustStore"), getProperty("javax.net.ssl.trustStorePassword"));
        try {
            if (trust != null && keyJKSStore != null && trust.size() > 0 && keyJKSStore.size() > 0) {
                try {
                    while (trust.aliases().hasMoreElements()) {
                        final String alias = trust.aliases().nextElement();
                        keyJKSStore.setCertificateEntry(alias, trust.getCertificate(alias));
                        trust.deleteEntry(alias);
                    }
                } catch (final KeyStoreException e) {
                }
            }
        } catch (final KeyStoreException e) {
            e.printStackTrace();
        }
        return keyJKSStore;
    }

    private static KeyStore getKeyJKSStore(final String keyStoreFileName, final String keyStorePassword) {
        KeyStore jKeyStore = null;
        try {
            if (keyStorePassword != null && !keyStorePassword.isEmpty() && keyStoreFileName != null && !keyStoreFileName.isEmpty()) {
                jKeyStore = KeyStore.getInstance("JKS");
                final File file = new File(keyStoreFileName);
                final FileInputStream inStream = new FileInputStream(file);
                jKeyStore.load(inStream, keyStorePassword.toCharArray());
            }
        } catch (final KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
        }
        return jKeyStore;
    }

    protected X509TrustManager getTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
        return new EasyX509TrustManager(getKeyStore());
    }

    public SSLContext getSSLContext() {
        if (sslcontext == null) {
            sslcontext = createEasySSLContext();
        }
        return sslcontext;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj != null && obj.getClass().equals(EasySSLProtocolSocketFactory.class);
    }

    @Override
    public int hashCode() {
        return EasySSLProtocolSocketFactory.class.hashCode();
    }
}