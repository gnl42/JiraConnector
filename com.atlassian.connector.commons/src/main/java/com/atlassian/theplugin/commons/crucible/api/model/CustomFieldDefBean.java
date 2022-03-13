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

package com.atlassian.theplugin.commons.crucible.api.model;

import java.util.ArrayList;
import java.util.List;

public class CustomFieldDefBean implements CustomFieldDef {
    private CustomFieldValueType type;
    private int configVersion;
    private String fieldScope;
    private String name;
    private String label;
    private CustomFieldValue defaultValue;
    private List<CustomFieldValue> values;

    public CustomFieldDefBean() {
        values = new ArrayList<CustomFieldValue>();
    }

    public CustomFieldValueType getType() {
        return type;
    }

    public void setType(CustomFieldValueType type) {
        this.type = type;
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }

    public String getFieldScope() {
        return fieldScope;
    }

    public void setFieldScope(String fieldScope) {
        this.fieldScope = fieldScope;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public CustomFieldValue getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(CustomFieldValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<CustomFieldValue> getValues() {
        return values;
    }

    public void setValues(List<CustomFieldValue> values) {
        this.values = values;
    }
}