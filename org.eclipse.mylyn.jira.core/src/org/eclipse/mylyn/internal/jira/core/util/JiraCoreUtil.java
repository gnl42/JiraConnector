/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.monitor.core.StatusHandler;

/**
 * @author Steffen Pingel
 */
public class JiraCoreUtil {

	public static String encode(String text, String encoding) {
		try {
			return URLEncoder.encode(text, encoding);
		} catch (UnsupportedEncodingException e) {
			try {
				return URLEncoder.encode(text, JiraClient.DEFAULT_CHARSET);
			} catch (UnsupportedEncodingException e1) {
				// should never happen
				StatusHandler.log(new Status(IStatus.ERROR, JiraCorePlugin.ID_PLUGIN, 0, "Could not encode text \""
						+ text + "\"", e));
				return text;
			}
		}
	}

}
