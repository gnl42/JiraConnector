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

package com.atlassian.jira.restjavaclient.domain;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class TransitionInput {
	private final int id;
	@Nullable
	private final Comment comment;

	private final Collection<FieldInput> fields;

	public TransitionInput(int id, Collection<FieldInput> fields) {
		this(id, fields, null);
	}


	public TransitionInput(int id, Collection<FieldInput> fields, Comment comment) {
		this.id = id;
		this.comment = comment;
		this.fields = fields;
	}

	public TransitionInput(int id, Comment comment) {
		this(id, Collections.<FieldInput>emptyList(), comment);
	}

	public TransitionInput(int id) {
		this(id, Collections.<FieldInput>emptyList(), null);
	}

	public int getId() {
		return id;
	}

	@Nullable
	public Comment getComment() {
		return comment;
	}

	public Iterable<FieldInput> getFields() {
		return fields;
	}
}
