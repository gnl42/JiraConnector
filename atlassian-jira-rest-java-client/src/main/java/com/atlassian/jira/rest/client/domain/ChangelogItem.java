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

package com.atlassian.jira.rest.client.domain;

/**
 * Represents single item in Issue change history.
 * @since 0.6
 */
public class ChangelogItem {
	private final String fieldType;
	private final String field;
	private final String from;
	private final String fromString;
	private final String to;
	private final String toString;

	public ChangelogItem(String fieldType, String field, String from, String fromString, String to, String toString) {
		this.fieldType = fieldType;
		this.field = field;
		this.from = from;
		this.fromString = fromString;
		this.to = to;
		this.toString = toString;
	}

	public String getFieldType() {
		return fieldType;
	}

	public String getField() {
		return field;
	}

	public String getFrom() {
		return from;
	}

	public String getFromString() {
		return fromString;
	}

	public String getTo() {
		return to;
	}

	public String getToString() {
		return toString;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ChangelogItem)) return false;

		final ChangelogItem that = (ChangelogItem) o;

		if (!field.equals(that.field)) return false;
		if (!fieldType.equals(that.fieldType)) return false;
		if (from != null ? !from.equals(that.from) : that.from != null) return false;
		if (fromString != null ? !fromString.equals(that.fromString) : that.fromString != null) return false;
		if (to != null ? !to.equals(that.to) : that.to != null) return false;
		if (toString != null ? !toString.equals(that.toString) : that.toString != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = fieldType.hashCode();
		result = 31 * result + field.hashCode();
		result = 31 * result + (from != null ? from.hashCode() : 0);
		result = 31 * result + (fromString != null ? fromString.hashCode() : 0);
		result = 31 * result + (to != null ? to.hashCode() : 0);
		result = 31 * result + (toString != null ? toString.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ChangelogItem{" +
				"fieldType='" + fieldType + '\'' +
				", field='" + field + '\'' +
				", from='" + from + '\'' +
				", fromString='" + fromString + '\'' +
				", to='" + to + '\'' +
				", toString='" + toString + '\'' +
				'}';
	}
}
