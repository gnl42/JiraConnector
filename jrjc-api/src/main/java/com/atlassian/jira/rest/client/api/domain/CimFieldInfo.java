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

package com.atlassian.jira.rest.client.api.domain;

import java.io.Serializable;
import java.net.URI;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.IdentifiableEntity;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.NamedEntity;

/**
 * Contains information about field in IssueType.
 * <p>
 * The CIM prefix stands for CreateIssueMetadata as this class is used in output of {@link IssueRestClient#getCreateIssueMetadata(GetCreateIssueMetadataOptions)}
 *
 * @since v1.0
 */
public class CimFieldInfo implements NamedEntity, IdentifiableEntity<String>, Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final boolean required;
    @Nullable
    private final String name;
    private final FieldSchema schema;
    private final Set<StandardOperation> operations;
    @Nullable
    private final Iterable<Object> allowedValues;
    @Nullable
    private final URI autoCompleteUri;


    public CimFieldInfo(String id, boolean required, @Nullable String name, FieldSchema schema,
            Set<StandardOperation> operations, @Nullable Iterable<Object> allowedValues, @Nullable URI autoCompleteUri) {
        this.id = id;
        this.required = required;
        this.name = name;
        this.schema = schema;
        this.operations = operations;
        this.allowedValues = allowedValues;
        this.autoCompleteUri = autoCompleteUri;
    }

    @Override
    public String getId() {
        return id;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Returns schema of this field that describes type of that field and contained items type.
     *
     * @return schema of this field.
     */
    public FieldSchema getSchema() {
        return schema;
    }

    /**
     * Returns set of operations allowed for this field.
     *
     * @return set of operations allowed for this field.
     */
    public Set<StandardOperation> getOperations() {
        return operations;
    }

    /**
     * Returns list of values that are allowed to be used as value to this field.
     *
     * @return list of allowed values.
     */
    @Nullable
    public Iterable<Object> getAllowedValues() {
        return allowedValues;
    }

    /**
     * Returns URI to Auto Complete feature for this field. To make use of it append searched text to returned address.
     * <p>
     * Example:<br>
     * {@code URI uriToGetResponseFrom = new URI(getAutoCompleteUri() + "typedLetters"); }
     *
     * @return URI to Auto Complete feature for this field
     */
    @SuppressWarnings("UnusedDeclaration")
    @Nullable
    public URI getAutoCompleteUri() {
        return autoCompleteUri;
    }

    /**
     * Returns ToStringHelper with all fields inserted. Override this method to insert additional fields.
     *
     * @return ToStringHelper
     */
    protected String getToStringHelper() {
        return toString();
    }

    @Override
    public String toString() {
        return "CimFieldInfo [id=" + id + ", required=" + required + ", name=" + name + ", schema=" + schema + ", operations=" + operations + ", allowedValues="
                + allowedValues + ", autoCompleteUri=" + autoCompleteUri + "]";
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CimFieldInfo) {
            CimFieldInfo that = (CimFieldInfo) obj;
            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.required, that.required)
                    && Objects.equals(this.schema, that.schema)
                    && Objects.equals(this.operations, that.operations)
                    && Objects.equals(this.allowedValues, that.allowedValues)
                    && Objects.equals(this.autoCompleteUri, that.autoCompleteUri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, required, schema, operations, allowedValues, autoCompleteUri);
    }
}
