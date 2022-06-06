/*
 * Copyright (C) 2010-2012 Atlassian
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

package me.glindholm.jira.rest.client.api.domain.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Error container returned in bulk operations
 *
 * @since v2.0
 */
public class ErrorCollection {

    private final Integer status;
    private final List<String> errorMessages;
    private final Map<String, String> errors;

    public ErrorCollection(@Nullable final Integer status, final List<String> errorMessages, final Map<String, String> errors) {
        this.status = status;
        this.errors = Map.copyOf(errors);
        this.errorMessages = List.copyOf(errorMessages);
    }

    public ErrorCollection(final String errorMessage) {
        this(null, List.of(errorMessage), Collections.emptyMap());
    }

    @SuppressWarnings("unused")
    @Nullable
    public Integer getStatus() {
        return status;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    //    public static Builder builder() {
    //        return new Builder();
    //    }
    //
    @Override
    public String toString() {
        return "ErrorList [status=" + status + ", errorMessages=" + errorMessages + ", errors=" + errors + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ErrorCollection) {
            final ErrorCollection that = (ErrorCollection) obj;
            return Objects.equals(this.status, that.status)
                    && Objects.equals(this.errors, that.errors)
                    && Objects.equals(this.errorMessages, that.errorMessages);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, errors, errorMessages);
    }

    //    public static class Builder {
    //
    //        private int status;
    //        private final ImmutableMap.Builder<String, String> errors;
    //        private final ImmutableList.Builder<String> errorMessages;
    //
    //        public Builder() {
    //            errors = ImmutableMap.builder();
    //            errorMessages = ImmutableList.builder();
    //        }
    //
    //        public Builder status(final int status) {
    //            this.status = status;
    //            return this;
    //        }
    //
    //        public Builder error(final String key, final String message) {
    //            errors.put(key, message);
    //            return this;
    //
    //        }
    //
    //        public Builder errorMessage(final String message) {
    //            errorMessages.add(message);
    //            return this;
    //        }
    //
    //        public ErrorCollection build() {
    //            return new ErrorCollection(status, errorMessages.build(), errors.build());
    //        }
    //    }
}
