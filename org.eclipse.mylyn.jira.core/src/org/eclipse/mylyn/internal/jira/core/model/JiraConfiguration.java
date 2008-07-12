/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model;

import java.util.Locale;

/**
 * @author Steffen Pingel
 */
public class JiraConfiguration {

	public static final String DEFAULT_DATE_FORMAT = "dd/MMM/yy"; //$NON-NLS-1$

	public static final String DEFAULT_DATE_TIME_FORMAT = "dd/MMM/yy hh:mm a"; //$NON-NLS-1$

	public static final Locale DEFAULT_LOCALE = Locale.US;

	private String dateFormat;

	private String dateTimeFormat;

	private boolean compressionEnabled;

	private Locale locale;

	private String characterEncoding;

	public JiraConfiguration() {
		setDateFormat(DEFAULT_DATE_FORMAT);
		setDateTimeFormat(DEFAULT_DATE_TIME_FORMAT);
		setLocale(DEFAULT_LOCALE);
		setCompressionEnabled(false);
	}

	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	public Locale getLocale() {
		return locale;
	}

	public boolean isCompressionEnabled() {
		return compressionEnabled;
	}

	public void setCompressionEnabled(boolean compressionEnabled) {
		this.compressionEnabled = compressionEnabled;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public void setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

}
