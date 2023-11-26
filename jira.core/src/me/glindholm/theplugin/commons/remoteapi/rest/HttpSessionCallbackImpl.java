package me.glindholm.theplugin.commons.remoteapi.rest;

import org.apache.commons.httpclient.HttpMethod;

/**
 * Default implementation of the {@link HttpSessionCallback}
 *
 * @author Shawn Minto
 */
public abstract class HttpSessionCallbackImpl implements HttpSessionCallback {

    @Override
    public void configureHttpMethod(final AbstractHttpSession session, final HttpMethod method) {
        session.adjustHttpHeader(method);
    }

}
