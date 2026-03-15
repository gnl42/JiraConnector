package me.glindholm.theplugin.commons.remoteapi.rest;

/**
 * Default implementation of the {@link HttpSessionCallback}.
 * Retained for API compatibility; actual HTTP is now handled via java.net.http.HttpClient.
 *
 * @author Shawn Minto
 */
public abstract class HttpSessionCallbackImpl implements HttpSessionCallback {
}