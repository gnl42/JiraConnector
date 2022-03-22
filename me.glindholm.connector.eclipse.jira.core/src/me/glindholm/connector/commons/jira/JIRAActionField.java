/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.connector.commons.jira;

import com.atlassian.jira.rest.client.domain.input.FieldInput;

import me.glindholm.connector.commons.jira.beans.JIRAConstant;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.List;

public interface JIRAActionField extends JIRAConstant {
	String getFieldId();

	List<String> getValues();

	void setValues(List<String> values);

	void addValue(String val);

	String getName();

    FieldInput generateFieldValue(JIRAIssue issue, JSONObject fieldMetadata) throws JSONException, RemoteApiException;
}
