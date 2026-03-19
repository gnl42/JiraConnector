package me.glindholm.theplugin.commons.remoteapi.rest;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.exception.HttpProxySettingsException;

/**
 * Interface for the callback used by AbstractHttpSession for HttpClient setup.
 * Retained for API compatibility; actual HTTP is now handled via java.net.http.HttpClient.
 *
 * @author Shawn Minto
 */
public interface HttpSessionCallback {

    void disposeClient(ConnectionCfg server);
}