/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.branding.ui;

import java.util.Map;

import org.eclipse.ui.IStartup;

import com.atlassian.connector.eclipse.internal.ui.AtlassianBundlesInfo;
import com.atlassian.connector.eclipse.internal.ui.IBrandingConstants;

public class FastStartup implements IStartup {

	public void earlyStartup() {
		setupJiraSystemProperty();
	}

	private void setupJiraSystemProperty() {
		final String[] keys = new String[] { "crucible.", "fisheye.", "bamboo." };
		boolean onlyJira = true;
		for (Map.Entry<String, String> entry : AtlassianBundlesInfo.getAllInstalledBundles().entrySet()) {
			for (String key : keys) {
				if (entry.getKey().startsWith(key)) {
					onlyJira = false;
					break;
				}
			}
			if (!onlyJira) {
				break;
			}
		}
		System.setProperty(IBrandingConstants.JIRA_INSTALLED_SYSTEM_PROPERTY, Boolean.toString(onlyJira));
	}

}
