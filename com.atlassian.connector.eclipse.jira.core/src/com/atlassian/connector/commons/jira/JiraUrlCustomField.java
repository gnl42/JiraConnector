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

/**
 * @autrhor pmaruszak
 * @date Jul 9, 2010
 */
public class JiraUrlCustomField extends JiraCustomFieldImpl {
    public JiraUrlCustomField(Builder builder) {
        super(builder);
    }

    @Override
    public String getFormattedValue() {
        String value = "";
        if (values.size() > 0) {
            value = values.get(0);
        }

        return value;
    }
}
