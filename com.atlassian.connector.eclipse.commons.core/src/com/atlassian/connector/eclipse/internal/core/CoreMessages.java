package com.atlassian.connector.eclipse.internal.core;

import org.eclipse.osgi.util.NLS;

public class CoreMessages extends NLS {
	private static final String BUNDLE_NAME = "com.atlassian.connector.eclipse.internal.core.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, CoreMessages.class);
	}

	public static String Captcha_authentication_required;

}
