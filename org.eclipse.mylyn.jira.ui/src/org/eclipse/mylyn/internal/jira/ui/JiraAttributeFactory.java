/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.tasks.core.AbstractAttributeFactory;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;

/**
 * @author Mik Kersten
 */
public class JiraAttributeFactory extends AbstractAttributeFactory {

	private static final long serialVersionUID = 8000933300692372211L;

	public static final String ATTRIBUTE_TYPE = "attribute.jira.type";
	
	public static final String ATTRIBUTE_ISSUE_KEY = "attribute.jira.issue_key";

	public static final String ATTRIBUTE_ENVIRONMENT = "attribute.jira.environment";
	public static final String ATTRIBUTE_COMPONENTS = "attribute.jira.components";
	public static final String ATTRIBUTE_FIXVERSIONS = "attribute.jira.fixversions";
	public static final String ATTRIBUTE_AFFECTSVERSIONS = "attribute.jira.affectsversions";
	public static final String ATTRIBUTE_ESTIMATE = "attribute.jira.estimate";

	public static final String JIRA_DATE_FORMAT = "dd MMM yyyy HH:mm:ss z";

	@Override
	public boolean getIsHidden(String key) {
		return false;
	}

	@Override
	public String getName(String key) {
		return key;
	}

	@Override
	public boolean isReadOnly(String key) {
		return true;
	}

	@Override
	public String mapCommonAttributeKey(String key) {
		if (RepositoryTaskAttribute.TASK_KEY.equals(key)) {
			return ATTRIBUTE_ISSUE_KEY;
		}
		return key;
	}

	public Date getDateForAttributeType(String attributeKey, String dateString) {
		if (dateString == null || dateString.equals("")) {
			return null;
		}
		try {
			// String mappedKey =
			// attributeFactory.mapCommonAttributeKey(attributeKey);
			// Date parsedDate = null;
			// if (mappedKey.equals(RepositoryTaskAttribute.DATE_MODIFIED)) {
			// parsedDate = modified_ts_format.parse(dateString);
			// } else if
			// (mappedKey.equals(RepositoryTaskAttribute.DATE_CREATION)) {
			// parsedDate = creation_ts_format.parse(dateString);
			// }
			// return parsedDate;
			return new SimpleDateFormat(JIRA_DATE_FORMAT, Locale.US).parse(dateString);
		} catch (Exception e) {
			MylarStatusHandler.log(e, "Error while date for attribute " + attributeKey + ": " + dateString);
			return null;
		}
	}
	
}