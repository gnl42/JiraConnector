package com.atlassian.theplugin.commons.ssl;

import java.security.cert.X509Certificate;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jul 2, 2009
 * 
 * Allows to create system/ide specific dialog for accepting SSL certificates
 */
public interface CertMessageDialog {
    /**
     *  show messaaged dialog in front
     * @param host  url of host we are accepting certificate
     * @param message   message/question to be asked
     * @param chain certificates to be shown
     */
    void show(String host, String message, X509Certificate[] chain);

    /**
     *
     * @return  true if certificate accepted by user
     */
    //@returns:
    boolean isOK();

    /**
     *
     * @return  true if certificate is accepted only temporarily
     *
     */
    boolean isTemporarily();
}
