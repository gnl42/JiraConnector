package com.atlassian.connector.commons.jira.cache;

/**
 * @author pmaruszak
 * @date Oct 7, 2009
 */
public final class CacheConstants {
    private CacheConstants() {
    }
    
    public static final int ANY_ID = -1000;
	public static final int NO_VERSION_ID = -1;
	public static final int RELEASED_VERSION_ID = -3;
	public static final int UNRELEASED_VERSION_ID = -2;
	public static final int UNKNOWN_COMPONENT_ID = -1;
	public static final int UNRESOLVED_ID = -1;
}
