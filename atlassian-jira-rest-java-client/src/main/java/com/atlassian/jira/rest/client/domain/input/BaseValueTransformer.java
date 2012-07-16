/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.rest.client.domain.input;

import com.atlassian.jira.rest.client.IdentifiedEntity;
import com.atlassian.jira.rest.client.NamedEntity;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.CustomFieldOption;
import com.google.common.collect.ImmutableMap;

/**
* Transforms most of standard fields values into form understandable by input generator.
*
* @since v1.0
*/
public class BaseValueTransformer implements ValueTransformer {

	public Object apply(Object rawValue) {
		if (rawValue == null) {
			return null;
		}
		else if (rawValue instanceof Number) {
			return rawValue.toString();
		}
		else if (rawValue instanceof String) {
			return rawValue;
		}
		else if (rawValue instanceof BasicProject) {
			return new ComplexIssueInputFieldValue(ImmutableMap.<String, Object>of("key", ((BasicProject) rawValue).getKey()));
		}
		else if (rawValue instanceof CustomFieldOption) {
			final CustomFieldOption cfo = (CustomFieldOption) rawValue;
			return new ComplexIssueInputFieldValue(ImmutableMap.<String, Object>of("id", cfo.getId().toString(), "value", cfo.getValue()));
		}
		else if (rawValue instanceof IdentifiedEntity) {
			final IdentifiedEntity identifiedEntity = (IdentifiedEntity) rawValue;
			return new ComplexIssueInputFieldValue(ImmutableMap.<String, Object>of("id", identifiedEntity.getId().toString()));
		}
		else if (rawValue instanceof NamedEntity) {
			final NamedEntity namedEntity = (NamedEntity) rawValue;
			return new ComplexIssueInputFieldValue(ImmutableMap.<String, Object>of("name", namedEntity.getName()));
		}

		return CANNOT_HANDLE;
	}

}
