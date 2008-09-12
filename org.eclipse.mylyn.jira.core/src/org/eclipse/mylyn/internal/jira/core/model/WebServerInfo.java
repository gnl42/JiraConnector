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

package org.eclipse.mylyn.internal.jira.core.model;

/**
 * @author Steffen Pingel
 */
public class WebServerInfo {

	private String baseUrl;

	private String characterEncoding;

	private boolean insecureRedirect;

	public WebServerInfo() {
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
