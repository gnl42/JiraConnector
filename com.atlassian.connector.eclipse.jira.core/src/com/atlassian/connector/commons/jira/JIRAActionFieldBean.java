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

package com.atlassian.connector.commons.jira;

import com.atlassian.connector.commons.FieldValueGeneratorFactory;
import com.atlassian.connector.commons.jira.beans.AbstractJIRAConstantBean;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JIRAActionFieldBean extends AbstractJIRAConstantBean implements JIRAActionField {
	private String fieldId;

	private List<String> fieldValues = new ArrayList<String>();

    private static FieldValueGeneratorFactory generatorFactory;

	public JIRAActionFieldBean(String fieldId, String name) {
		super(fieldId.hashCode(), name, null);
		this.fieldId = fieldId;
	}

	public JIRAActionFieldBean(JIRAActionField other) {
		this(other.getFieldId(), other.getName());
	}

    public static void setGeneratorFactory(FieldValueGeneratorFactory generatorFactory) {
        JIRAActionFieldBean.generatorFactory = generatorFactory;
    }

    public String getFieldId() {
		return fieldId;
	}

	public void addValue(String val) {
		fieldValues.add(val);
	}

	public List<String> getValues() {
		return fieldValues;
	}

	public void setValues(List<String> values) {
		this.fieldValues = values;
	}

	public String getQueryStringFragment() {
		// todo: I am almost absolutely sure this is wrong.
		return fieldId + "=";
	}

	public JIRAActionFieldBean getClone() {
		return new JIRAActionFieldBean(this);
	}

    public FieldInput generateFieldValue(JIRAIssue issue, JSONObject fieldDef) throws JSONException, RemoteApiException {
        if (generatorFactory == null) {
            throw new RemoteApiException("Field Value Generator Factory not set");
        }

        FieldValueGenerator generator = generatorFactory.get(this, fieldDef);
        return generator.generateJrJcFieldValue(issue, this, fieldDef);
    }
}
