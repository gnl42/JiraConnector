/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.jira.rest.client.api.domain;

import java.util.List;
import java.util.Objects;

/**
 * Basic representation of a JIRA issues and errors created using batch operation.
 *
 * @since v2.0
 */
public class BulkOperationResult<T> {

    private final List<T> issues;
    private final List<BulkOperationErrorResult> errors;

    public BulkOperationResult(final List<T> issues, final List<BulkOperationErrorResult> errors) {
        this.issues = issues;
        this.errors = errors;
    }

    public List<T> getIssues() {
        return issues;
    }

    public List<BulkOperationErrorResult> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "BulkOperationResult [issues=" + issues + ", errors=" + errors + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof BulkOperationResult) {
            final BulkOperationResult<T> that = (BulkOperationResult<T>) obj;
            return Objects.equals(issues, that.issues) && Objects.equals(errors, that.errors);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(issues, errors);
    }
}
