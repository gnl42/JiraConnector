/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.model;

/**
 * @author Steffen Pingel
 */
public class WebServerInfo {

	private String baseUrl;

	private String characterEncoding;

	private boolean insecureRedirect;

	private transient Statistics statistics;

	public WebServerInfo() {
	}

	public synchronized Statistics getStatistics() {
		if (statistics == null) {
			statistics = new Statistics();
		}
		return statistics;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public boolean isInsecureRedirect() {
		return insecureRedirect;
	}

	public void setInsecureRedirect(boolean insecureRedirect) {
		this.insecureRedirect = insecureRedirect;
	}

}
