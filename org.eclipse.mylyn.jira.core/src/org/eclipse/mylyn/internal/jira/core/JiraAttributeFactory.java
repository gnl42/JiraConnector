/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.tasks.core.deprecated.AbstractAttributeFactory;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskAttribute;

/**
 * @author Mik Kersten
 * @author Steffen Pingel
 */
@SuppressWarnings( { "restriction", "deprecation" })
@Deprecated
public class JiraAttributeFactory extends AbstractAttributeFactory {

	private static final long serialVersionUID = 8000933300692372211L;

	public JiraAttributeFactory() {
	}

	@Override
	public RepositoryTaskAttribute createAttribute(String key) {
		RepositoryTaskAttribute attribute = super.createAttribute(key);
		attribute.putMetaDataValue(IJiraConstants.META_TYPE, JiraAttribute.valueById(attribute.getId()).getType().getKey());
		return attribute;
	}

	@Override
	public boolean isHidden(String key) {
		return JiraAttribute.valueById(key).isHidden();
	}

	@Override
	public String getName(String key) {
		return JiraAttribute.valueById(key).getName();
	}

	@Override
	public boolean isReadOnly(String key) {
		return JiraAttribute.valueById(key).isReadOnly();
	}

	@Override
	public String mapCommonAttributeKey(String key) {
		if ("summary".equals(key)) {
			return RepositoryTaskAttribute.SUMMARY;
		} else if ("description".equals(key)) {
			return RepositoryTaskAttribute.DESCRIPTION;
		} else if ("priority".equals(key)) {
			return RepositoryTaskAttribute.PRIORITY;
		} else if ("resolution".equals(key)) {
			return RepositoryTaskAttribute.RESOLUTION;
		} else if ("assignee".equals(key)) {
			return RepositoryTaskAttribute.USER_ASSIGNED;
		} else if ("environment".equals(key)) {
			return IJiraConstants.ATTRIBUTE_ENVIRONMENT;
		} else if ("issuetype".equals(key)) {
			return IJiraConstants.ATTRIBUTE_TYPE;
		} else if ("components".equals(key)) {
			return IJiraConstants.ATTRIBUTE_COMPONENTS;
		} else if ("versions".equals(key)) {
			return IJiraConstants.ATTRIBUTE_AFFECTSVERSIONS;
		} else if ("fixVersions".equals(key)) {
			return IJiraConstants.ATTRIBUTE_FIXVERSIONS;
		} else if ("timetracking".equals(key)) {
			return IJiraConstants.ATTRIBUTE_ESTIMATE;
		} else if ("duedate".equals(key)) {
			return IJiraConstants.ATTRIBUTE_DUE_DATE;
		}

		if (RepositoryTaskAttribute.COMPONENT.equals(key)) {
			return IJiraConstants.ATTRIBUTE_COMPONENTS;
		}

		if (key.startsWith("issueLink")) {
			return IJiraConstants.ATTRIBUTE_LINK_PREFIX + key;
		}
		if (key.startsWith("customfield")) {
			return IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX + key;
		}

		return key;
	}

//	public String mapAttributeToKey(String key) {
//		JiraAttribute attribute = JiraAttribute.valueById(key);
//		if (!attribute.equals(JiraAttribute.UNKNOWN)) {
//			return attribute.getParamName();
//		}
//		if (key.startsWith(ATTRIBUTE_CUSTOM_PREFIX)) {
//			return key.substring(ATTRIBUTE_CUSTOM_PREFIX.length());
//		}
//		return key;
//	}

	@Override
	public Date getDateForAttributeType(String attributeKey, String dateString) {
		if (dateString == null || dateString.equals("")) {
			return null;
		}
		try {
			// RepositoryTaskAttribute.DATE_MODIFIED
			// RepositoryTaskAttribute.DATE_CREATION
			return JiraUtil.stringToDate(dateString);
		} catch (Exception e) {
			StatusHandler.log(new org.eclipse.core.runtime.Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
					"Error parsing date for attribute \"" + attributeKey + "\": \"" + dateString + "\"", e));
			return null;
		}
	}

}