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

import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @autrhor pmaruszak
 * @date Jul 7, 2010
 */
public class JiraCustomFieldImpl implements JiraCustomField {

    private String id;
    protected List<String> values = new ArrayList<String>();
    private BasicKeyType typeKey = BasicKeyType.UNSUPPORTED;
    private String name;

    protected JiraCustomFieldImpl(Builder builder) {
        this(builder.id, builder.name, builder.values, builder.typeKey);
    }

    protected JiraCustomFieldImpl(String id, String name, List<String> values, BasicKeyType typeKey) {
        this.id = id;
        if (values != null) {
            this.values.addAll(values);
        }
        this.typeKey = typeKey;
        this.name = name;
    }


    public boolean isSupported() {
        return !BasicKeyType.UNSUPPORTED.equals(typeKey)
//                && !BasicKeyType.DATE_TIME.equals(typeKey)
          ;
    }

    public String getId() {
        return id;
    }

    public BasicKeyType getTypeKey() {
        return typeKey;
    }

    //    @Nullable
    public List<String> getValues() {
        return values;
    }

    public String getName() {
        return name;
    }

    public String getFormattedValue() {
        return values.size() > 0 ? values.get(0) : "";
    }

    public enum BasicKeyType {
        NUMERIC("me.glindholm.jira.plugin.system.customfieldtypes:float"),
        TEXT("me.glindholm.jira.plugin.system.customfieldtypes:textfield"),
        TEXT_AREA("me.glindholm.jira.plugin.system.customfieldtypes:textarea"),
        URL("me.glindholm.jira.plugin.system.customfieldtypes:url"),
//        DATE_TIME("me.glindholm.jira.plugin.system.customfieldtypes:datetime"),
        DATE_PICKER("me.glindholm.jira.plugin.system.customfieldtypes:datepicker"),
        UNSUPPORTED("");

        private final String keyValue;

        BasicKeyType(String key) {
            this.keyValue = key;
        }

        public String getKeyValue() {
            return keyValue;
        }

        public static BasicKeyType getValueOf(String key) {
            for (BasicKeyType type : BasicKeyType.values()) {
                if (type.getKeyValue().equalsIgnoreCase(key)) {
                    return type;
                }
            }
            return BasicKeyType.UNSUPPORTED;
        }

        public FieldInput generateJrJcFieldValue(JIRAActionField field) {
            if (UNSUPPORTED == this
//                    || DATE_TIME == this
                    ) {
                return null;
            }
            List<String> values = field.getValues();
            if (values != null && values.size() > 0) {
                String str = values.get(0);
                if (NUMERIC == this) {
                    return new FieldInput(field.getFieldId(), str != null && str.length() > 0 ? Float.valueOf(str) : 0);
                } else if (DATE_PICKER == this) {
                    if (StringUtils.isEmpty(str)) {
                        return new FieldInput(field.getFieldId(), null);
                    }
                    DateTime dt = DateTimeFormat.forPattern("dd/MMM/yy").withLocale(Locale.US).parseDateTime(str);
                    return new FieldInput(field.getFieldId(), JsonParseUtil.formatDate(dt));
                }
                // text and URL fields
                return new FieldInput(field.getFieldId(), str);
            }
            return new FieldInput(field.getFieldId(), null);
        }
    }

    public static class Builder {
        private BasicKeyType typeKey;
        private String id;
        private String name;
        private List<String> values = new ArrayList<String>();

        public Builder(JSONObject meta, Field field) throws JSONException {
            JSONObject schema = meta.getJSONObject("schema");
            this.typeKey = BasicKeyType.getValueOf(schema.getString("custom"));
            this.name = meta.getString("name");
            String type = schema.getString("type");
            this.id = field.getId();
            values = Lists.newArrayList();
            if ("array".equals(type)) {
//                try {
//                    JSONArray vals = (JSONArray) field.getValue();
                    values.add("[Custom field not supported]");
//                } catch (Exception e) {
//                }
            } else {
                if (this.typeKey == BasicKeyType.UNSUPPORTED) {
                    values.add("[Custom field not supported]");
                } else {
                    Object value = field.getValue();
                    if (value != null) {
                        values.add(value.toString());
                    }
                }
            }
        }

        public Builder(Element e) {
            if (e != null) {
                Attribute keyAttribute = e.getAttribute("key");
                if (keyAttribute != null && keyAttribute.getValue() != null) {
                    typeKey = BasicKeyType.getValueOf(keyAttribute.getValue());
                }
                Attribute idAttribute = e.getAttribute("id");
                if (idAttribute != null && idAttribute.getValue() != null) {
                    this.id = idAttribute.getValue();
                }
                Element nameElement = (Element) e.getChild("customfieldname");
                if (nameElement != null && nameElement.getText() != null) {
                    name = nameElement.getText();
                }

                Element valuesElement = (Element) e.getChild("customfieldvalues");
                if (valuesElement != null) {
                    for (int i = 0; i < valuesElement.getChildren().size(); i++) {
                        Element singleValueElement = (Element) valuesElement.getChildren().get(i);
                        if (singleValueElement != null && singleValueElement.getValue() != null) {
                            values.add(singleValueElement.getValue());
                        }
                    }
                }
            }

        }

        public JiraCustomField build() {
            switch (typeKey) {
                case DATE_PICKER:
                    return new JiraDatePickerCustomField(this);
//                case DATE_TIME:
//                    return new JiraDateTimeCustomField(this);
                case URL:
                    return new JiraUrlCustomField(this);
                default:
                    return new JiraCustomFieldImpl(this);
            }
        }
    }
}
