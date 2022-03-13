package com.atlassian.theplugin.commons.remoteapi;

/**
 * User: kalamon
 * Date: Jul 6, 2009
 * Time: 12:19:34 PM
 */
public class RemoteApiBadServerVersionException extends RemoteApiException {
    
    private static final long serialVersionUID = -2702749713783814931L;

    public RemoteApiBadServerVersionException(String message) {
        super(message);
    }
}
