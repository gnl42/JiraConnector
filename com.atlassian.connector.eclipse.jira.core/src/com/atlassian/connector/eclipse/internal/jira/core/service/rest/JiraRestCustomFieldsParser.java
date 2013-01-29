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
import org.eclipse.osgi.util.NLS;

import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;

public class JiraRestCustomFieldsParser {
	private JiraRestCustomFieldsParser() throws Exception {
		throw new Exception("Utility class"); //$NON-NLS-1$
	}

	public static List<String> parseMultiUserPicker(Field field) throws JSONException {
		List<String> users = new ArrayList<String>();

		JSONArray jsonArray = (JSONArray) field.getValue();

		for (int i = 0; i < jsonArray.length(); i++) {
			Object o = jsonArray.get(i);

			if (o instanceof JSONObject) {
				users.add(JsonParseUtil.getOptionalString((JSONObject) o, "name"));
			}
		}

		return users;
	}

	public static String parseUserPicker(Field field) throws JSONException {

		JSONObject jsonValue = (JSONObject) field.getValue();

		String value = JsonParseUtil.getOptionalString(jsonValue, "name");

		if (value == null) {
			throw new JSONException(NLS.bind("Cannot parse field [{0}]", field.getName())); //$NON-NLS-1$
		}

		return value;
	}

	public static String parseSelect(Field field) throws JSONException {

		JSONObject jsonValue = (JSONObject) field.getValue();

		String value = JsonParseUtil.getOptionalString(jsonValue, "value");

		if (value == null) {
			throw new JSONException(NLS.bind("Cannot parse field [{0}]", field.getName())); //$NON-NLS-1$
		}

		return value;
	}

	public static List<String> parseMultiSelect(Field field) throws JSONException {
		List<String> values = new ArrayList<String>();

		JSONArray jsonArray = (JSONArray) field.getValue();

		for (int i = 0; i < jsonArray.length(); i++) {
			Object o = jsonArray.get(i);

			if (o instanceof JSONObject) {
				values.add(JsonParseUtil.getOptionalString((JSONObject) o, "value"));
			}
		}

		return values;
	}

	public static List<String> parseLabels(Field field) throws JSONException {
		List<String> labels = new ArrayList<String>();

		JSONArray jsonArray = (JSONArray) field.getValue();

		for (int i = 0; i < jsonArray.length(); i++) {
			Object o = jsonArray.get(i);

			if (o instanceof String) {
				labels.add((String) o);
			}
		}

		return labels;
	}

	public static String parseGroupPicker(Field field) throws JSONException {
		JSONObject jsonValue = (JSONObject) field.getValue();

		String group = JsonParseUtil.getOptionalString(jsonValue, "name");

		if (group == null) {
			throw new JSONException(NLS.bind("Cannot parse field [{0}]", field.getName())); //$NON-NLS-1$
		}

		return group;
	}

	public static List<String> parseMultiGroupPicker(Field field) throws JSONException {
		List<String> groups = new ArrayList<String>();

		JSONArray jsonArray = (JSONArray) field.getValue();

		for (int i = 0; i < jsonArray.length(); i++) {
			Object o = jsonArray.get(i);

			if (o instanceof JSONObject) {
				groups.add(JsonParseUtil.getOptionalString((JSONObject) o, "name"));
			}
		}

		return groups;
	}

}
