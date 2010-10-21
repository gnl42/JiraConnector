/*
 * Copyright (C) 2010 Atlassian
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

/**
 * New value fo selected field - used while changing issue fields - e.g. while transitioning issue. 
 *
 * @since v0.1
 */
public class FieldInput {
	private final String id;
	private final Object value;

	/**
	 * @param id field id
	 * @param value new value for this issue field
	 */
	public FieldInput(String id, Object value) {
		this.id = id;
		this.value = value;
	}

	/**
	 * @return field id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return new value for this issue field
	 */
	public Object getValue() {
		return value;
	}
}
