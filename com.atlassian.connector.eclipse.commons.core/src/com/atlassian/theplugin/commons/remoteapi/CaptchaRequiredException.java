/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.theplugin.commons.remoteapi;

@SuppressWarnings("serial")
public class CaptchaRequiredException extends RemoteApiLoginException {

	public CaptchaRequiredException(Throwable throwable) {
		super("Due to multiple failed login attempts, you have been temporarily banned from using the remote API."
                + " To re-enable the remote API please log into your server via the web interface.", throwable);
	}

}
