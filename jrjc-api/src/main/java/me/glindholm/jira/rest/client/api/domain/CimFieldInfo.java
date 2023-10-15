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

package me.glindholm.jira.rest.client.api.domain;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import me.glindholm.jira.rest.client.api.GetCreateIssueMetadataOptions;
import me.glindholm.jira.rest.client.api.IdentifiableEntity;
import me.glindholm.jira.rest.client.api.IssueRestClient;
import me.glindholm.jira.rest.client.api.NamedEntity;

/**
 * Contains information about field in IssueType.
 * <p>
 * The CIM prefix stands for CreateIssueMetadata as this class is used in output of
 * {@link IssueRestClient#getCreateIssueMetadata(GetCreateIssueMetadataOptions)}
 *
 * @since v1.0
 */
public class CimFieldInfo implements Serializable, NamedEntity, IdentifiableEntity<String> {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final boolean required;
    @Nullable
    private final String name;
    private final FieldSchema schema;
    private final Set<StandardOperation> operations;
    @Nullable
    private final List<Object> allowedValues;
    @Nullable
    private final URI autoCompleteUri;

    public CimFieldInfo(final String id, final boolean required, @Nullable final String name, final FieldSchema schema, final Set<StandardOperation> operations,
            @Nullable final List<Object> allowedValues, @Nullable final URI autoCompleteUri) {
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
    public List<Object> getAllowedValues() {
        return allowedValues;
    }

    /**
     * Returns URI to Auto Complete feature for this field. To make use of it append searched text to
     * returned address.
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
     * Returns ToStringHelper with all fields inserted. Override this method to insert additional
     * fields.
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
    public boolean equals(final Object obj) {
        if (obj instanceof final CimFieldInfo that) {
            return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(required, that.required)
                    && Objects.equals(schema, that.schema) && Objects.equals(operations, that.operations) && Objects.equals(allowedValues, that.allowedValues)
                    && Objects.equals(autoCompleteUri, that.autoCompleteUri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, required, schema, operations, allowedValues, autoCompleteUri);
    }
}
