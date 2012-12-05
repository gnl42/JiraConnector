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

package com.atlassian.connector.eclipse.internal.jira.core.service.rest;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;

public class JiraRestCustomFieldsParser {
	private JiraRestCustomFieldsParser() throws Exception {
		throw new Exception("Utility class"); //$NON-NLS-1$
	}

	public static List<String> parseMultiUserPicker(Field field) {
		List<String> users = new ArrayList<String>();

		JSONArray jsonArray = (JSONArray) field.getValue();

		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				Object o = jsonArray.get(i);

				if (o instanceof JSONObject) {
					users.add(JsonParseUtil.getOptionalString((JSONObject) o, "displayName"));
				}

				o.toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return users;
	}

}
