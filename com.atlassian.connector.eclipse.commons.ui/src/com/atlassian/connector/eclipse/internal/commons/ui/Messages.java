/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.commons.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.atlassian.connector.eclipse.internal.commons.ui.messages"; //$NON-NLS-1$

	public static String RemoteApiLockedDialog_info;

	public static String RemoteApiLockedDialog_2;

	public static String RemoteApiLockedDialog_3;

	public static String RemoteApiLockedDialog_4;

	public static String RemoteApiLockedDialog_5;

	public static String RemoteApiLockedDialog_permission_denied;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
