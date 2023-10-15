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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import me.glindholm.jira.rest.client.api.domain.IssueField;

/**
 * @autrhor pmaruszak
 * @date Jul 7, 2010
 */
public class JiraCustomFieldImpl implements JiraCustomField {

    private final String id;
    protected List<String> values = new ArrayList<>();
    private BasicKeyType typeKey = BasicKeyType.UNSUPPORTED;
    private final String name;

    protected JiraCustomFieldImpl(final Builder builder) {
        this(builder.id, builder.name, builder.values, builder.typeKey);
    }

    protected JiraCustomFieldImpl(final String id, final String name, final List<String> values, final BasicKeyType typeKey) {
        this.id = id;
        if (values != null) {
            this.values.addAll(values);
        }
        this.typeKey = typeKey;
        this.name = name;
    }

    @Override
    public boolean isSupported() {
        return !BasicKeyType.UNSUPPORTED.equals(typeKey)
        // && !BasicKeyType.DATE_TIME.equals(typeKey)
        ;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public BasicKeyType getTypeKey() {
        return typeKey;
    }

    // @Nullable
    @Override
    public List<String> getValues() {
        return values;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormattedValue() {
        return values.size() > 0 ? values.get(0) : "";
    }

    public enum BasicKeyType {
        NUMERIC("com.atlassian.jira.plugin.system.customfieldtypes:float"), TEXT("com.atlassian.jira.plugin.system.customfieldtypes:textfield"),
        TEXT_AREA("com.atlassian.jira.plugin.system.customfieldtypes:textarea"), URL("com.atlassian.jira.plugin.system.customfieldtypes:url"),
        // DATE_TIME("com.atlassian.jira.plugin.system.customfieldtypes:datetime"),
        DATE_PICKER("com.atlassian.jira.plugin.system.customfieldtypes:datepicker"), UNSUPPORTED("");

        private final String keyValue;

        BasicKeyType(final String key) {
            keyValue = key;
        }

        public String getKeyValue() {
            return keyValue;
        }

        public static BasicKeyType getValueOf(final String key) {
            for (final BasicKeyType type : BasicKeyType.values()) {
                if (type.getKeyValue().equalsIgnoreCase(key)) {
                    return type;
                }
            }
            return BasicKeyType.UNSUPPORTED;
        }

    }

    public static class Builder {
        private final BasicKeyType typeKey;
        private final String id;
        private final String name;
        private List<String> values = new ArrayList<>();

        public Builder(final JSONObject meta, final IssueField field) throws JSONException {
            final JSONObject schema = meta.getJSONObject("schema");
            typeKey = BasicKeyType.getValueOf(schema.getString("custom"));
            name = meta.getString("name");
            final String type = schema.getString("type");
            id = field.getId();
            values = new ArrayList<>();
            if ("array".equals(type) || (typeKey == BasicKeyType.UNSUPPORTED)) {
                // try {
                // JSONArray vals = (JSONArray) field.getValue();
                values.add("[Custom field not supported]");
                // } catch (Exception e) {
                // }
            } else {
                final Object value = field.getValue();
                if (value != null) {
                    values.add(value.toString());
                }
            }
        }

        // public Builder(Element e) {
        // if (e != null) {
        // Attribute keyAttribute = e.getAttribute("key");
        // if (keyAttribute != null && keyAttribute.getValue() != null) {
        // typeKey = BasicKeyType.getValueOf(keyAttribute.getValue());
        // }
        // Attribute idAttribute = e.getAttribute("id");
        // if (idAttribute != null && idAttribute.getValue() != null) {
        // id = idAttribute.getValue();
        // }
        // Element nameElement = e.getChild("customfieldname");
        // if (nameElement != null && nameElement.getText() != null) {
        // name = nameElement.getText();
        // }
        //
        // Element valuesElement = e.getChild("customfieldvalues");
        // if (valuesElement != null) {
        // for (int i = 0; i < valuesElement.getChildren().size(); i++) {
        // Element singleValueElement = valuesElement.getChildren().get(i);
        // if (singleValueElement != null && singleValueElement.getValue() != null) {
        // values.add(singleValueElement.getValue());
        // }
        // }
        // }
        // }
        //
        // }

        public JiraCustomField build() {
            return switch (typeKey) {
            case DATE_PICKER -> new JiraDatePickerCustomField(this);
            case URL -> new JiraUrlCustomField(this);
            default -> new JiraCustomFieldImpl(this);
            };
        }
    }
}
