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

public enum State {
	DRAFT("Draft", "Draft"),
	APPROVAL("Approval", "Pending Approval"),
	REVIEW("Review", "Under Review"),
	SUMMARIZE("Summarize", "Summarize"),
    CLOSED("Closed", "Closed"),
	ABANDONED("Abandoned", "Abandoned"),
	REJECTED("Rejected", "Rejected"),
	UNKNOWN("Unknown", "Review Needs Fixing"),
	// these two are ugly. They rather should be just OPEN and CLOSED -- http://jira.atlassian.com/browse/CRUC-3394
	OPEN_SNIPPET("OpenSnippet", "Open Snippet"),
	CLOSED_SNIPPET("ClosedSnippet", "Closed Snippet"),
    DEAD("Dead", "Dead");

	private final String value;
	private String displayName;

	State(String v, String displayName) {
        value = v;
		this.displayName = displayName;
    }

    public String value() {
        return value;
    }

	public String getDisplayName() {
		return displayName;
	}

	public static State fromValue(String v) {
        for (State c : State.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}