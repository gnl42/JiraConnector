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

package me.glindholm.jira.rest.client.api.domain.input;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import me.glindholm.jira.rest.client.api.domain.Comment;

/**
 * Input data used while transitioning an issue including new values for this issue and the optional comment.
 *
 * @since v0.1
 */
public class TransitionInput {
    private final int id;
    @Nullable
    private final Comment comment;

    private final List<FieldInput> fields;

    /**
     * @param id     id of the issue transition which should be performed
     * @param fields new values for the issue fields. Use empty collection if no fields are to be changed
     */
    public TransitionInput(int id, List<FieldInput> fields) {
        this(id, fields, null);
    }


    /**
     * @param id      id of the issue transition which should be performed
     * @param fields  new values for the issue fields. Use empty collection if no fields are to be changed
     * @param comment optional comment
     */
    public TransitionInput(int id, List<FieldInput> fields, @Nullable Comment comment) {
        this.id = id;
        this.comment = comment;
        this.fields = fields;
    }

    /**
     * @param id      id of the issue transition which should be performed
     * @param comment optional comment
     */
    public TransitionInput(int id, @Nullable Comment comment) {
        this(id, Collections.emptyList(), comment);
    }

    public TransitionInput(int id) {
        this(id, Collections.emptyList(), null);
    }

    /**
     * @return id of the issue transition which should be performed
     */
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
