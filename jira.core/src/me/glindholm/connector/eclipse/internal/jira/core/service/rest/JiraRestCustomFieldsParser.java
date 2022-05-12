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

package me.glindholm.connector.eclipse.internal.jira.core.service.rest;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.osgi.util.NLS;

import me.glindholm.jira.rest.client.api.domain.CustomFieldOption;
import me.glindholm.jira.rest.client.api.domain.IssueField;
import me.glindholm.jira.rest.client.api.domain.User;
import me.glindholm.jira.rest.client.internal.json.CustomFieldOptionJsonParser;
import me.glindholm.jira.rest.client.internal.json.JsonParseUtil;
import me.glindholm.jira.rest.client.internal.json.UserJsonParser;
import me.glindholm.jira.rest.client.internal.json.UsersJsonParser;

public class JiraRestCustomFieldsParser {
    private static final UsersJsonParser usersParser = new UsersJsonParser();
    private static final UserJsonParser userParser = new UserJsonParser();
    private static final CustomFieldOptionJsonParser customParser = new CustomFieldOptionJsonParser();

    private JiraRestCustomFieldsParser() throws Exception {
        throw new Exception("Utility class"); //$NON-NLS-1$
    }

    public static List<String> parseMultiUserPicker(IssueField field) throws JSONException, URISyntaxException {
        List<String> users = new ArrayList<>();
        for (User user : usersParser.parse((JSONArray) field.getValue())) {
            users.add(user.getName());
        }

        return users;
    }

    public static String parseUserPicker(IssueField field) throws JSONException, URISyntaxException {
        User user = userParser.parse((JSONObject) field.getValue());
        return user.getName();
    }

    public static String parseSelect(IssueField field) throws JSONException, URISyntaxException {
        CustomFieldOption cfo = customParser.parse((JSONObject) field.getValue());

        return cfo.getValue();
    }

    public static List<String> parseMultiSelect(IssueField field) throws JSONException, URISyntaxException {
        List<String> values = new ArrayList<>();

        JSONArray jsonArray = (JSONArray) field.getValue();

        for (int i = 0; i < jsonArray.length(); i++) {
            CustomFieldOption cfo = customParser.parse((JSONObject) jsonArray.get(i));
            values.add(cfo.getValue());
        }

        return values;
    }

    public static List<String> parseLabels(IssueField field) throws JSONException {
        List<String> labels = new ArrayList<>();

        JSONArray jsonArray = (JSONArray) field.getValue();

        for (int i = 0; i < jsonArray.length(); i++) {
            Object o = jsonArray.get(i);

            if (o instanceof String) {
                labels.add((String) o);
            }
        }

        return labels;
    }

    public static String parseGroupPicker(IssueField field) throws JSONException {
        JSONObject jsonValue = (JSONObject) field.getValue();

        String group = JsonParseUtil.getOptionalString(jsonValue, "name");

        if (group == null) {
            throw new JSONException(NLS.bind("Cannot parse field [{0}]", field.getName())); //$NON-NLS-1$
        }

        return group;
    }

    public static List<String> parseMultiGroupPicker(IssueField field) throws JSONException {
        List<String> groups = new ArrayList<>();

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
