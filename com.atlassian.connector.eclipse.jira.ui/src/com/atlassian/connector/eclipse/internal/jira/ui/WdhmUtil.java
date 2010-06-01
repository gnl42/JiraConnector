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

package com.atlassian.connector.eclipse.internal.jira.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jacek Jaroczynski
 */
public class WdhmUtil {

	private static final String REGEX = "^\\s*-?\\s*(\\d+[wW])?\\s*(\\d+[dD])?\\s*(\\d+[hH])?\\s*(\\d+[mM])?\\s*$"; //$NON-NLS-1$

	private static final Pattern p = Pattern.compile(REGEX);

	/**
	 * Validates text against w*d*h*m* pattern. It accepts also nulls and empty strings.
	 * 
	 * @param text
	 * @return
	 * @deprecated Use Mylyn JiraUtil.getTimeFormat(repo)
	 */
	@Deprecated
	public static boolean isValid(String text) {
		return text == null || text.length() == 0 || isCorrect(text);
	}

	private static boolean isCorrect(String text) {
		Matcher m = p.matcher(text);
		return m.matches();
	}
}
