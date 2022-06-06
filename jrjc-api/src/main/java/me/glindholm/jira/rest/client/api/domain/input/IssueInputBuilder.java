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

package me.glindholm.jira.rest.client.api.domain.input;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.glindholm.jira.rest.client.api.domain.BasicComponent;
import me.glindholm.jira.rest.client.api.domain.BasicPriority;
import me.glindholm.jira.rest.client.api.domain.BasicProject;
import me.glindholm.jira.rest.client.api.domain.BasicUser;
import me.glindholm.jira.rest.client.api.domain.IssueFieldId;
import me.glindholm.jira.rest.client.api.domain.IssueType;
import me.glindholm.jira.rest.client.api.domain.Version;

/**
 * Builder for IssueInput class.
 *
 * @since 1.0
 */
public class IssueInputBuilder {

    private static final DateTimeFormatter JIRA_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private final ValueTransformerManager valueTransformerManager = new ValueTransformerManager()
            .registerTransformer(new BaseValueTransformer());

    private Map<String, FieldInput> fields = new HashMap<>();
    private final List<PropertyInput> properties = new ArrayList<>();

    /**
     * Creates {@link IssueInputBuilder} without any fields pre-populated. Remember to fill required fields for the target
     * issue action.
     */
    public IssueInputBuilder() {
    }

    public IssueInputBuilder(String projectKey, Long issueTypeId) {
        setProjectKey(projectKey);
        setIssueTypeId(issueTypeId);
    }

    public IssueInputBuilder(BasicProject project, IssueType issueType) {
        setProject(project);
        setIssueType(issueType);
    }

    @SuppressWarnings("unused")
    public IssueInputBuilder(String projectKey, Long issueTypeId, String summary) {
        this(projectKey, issueTypeId);
        setSummary(summary);
    }

    @SuppressWarnings("unused")
    public IssueInputBuilder(BasicProject project, IssueType issueType, String summary) {
        this(project, issueType);
        setSummary(summary);
    }

    public IssueInputBuilder setSummary(String summary) {
        return setFieldInput(new FieldInput(IssueFieldId.SUMMARY_FIELD, summary));
    }

    public IssueInputBuilder setProjectKey(String projectKey) {
        return setFieldInput(new FieldInput(IssueFieldId.PROJECT_FIELD, ComplexIssueInputFieldValue.with("key", projectKey)));
    }

    public IssueInputBuilder setProject(BasicProject project) {
        return setProjectKey(project.getKey());
    }

    public IssueInputBuilder setIssueTypeId(Long issueTypeId) {
        return setFieldInput(new FieldInput(
                IssueFieldId.ISSUE_TYPE_FIELD,
                ComplexIssueInputFieldValue.with("id", issueTypeId.toString())
                ));
    }

    public IssueInputBuilder setIssueType(IssueType issueType) {
        return setIssueTypeId(issueType.getId());
    }

    /**
     * Puts given FieldInput into fields collection.
     * <p>
     * <strong>Recommended</strong> way to set field value is to use {@link IssueInputBuilder#setFieldValue(String, Object)}.
     *
     * @param fieldInput FieldInput to insert.
     * @return this
     */
    public IssueInputBuilder setFieldInput(FieldInput fieldInput) {
        fields.put(fieldInput.getId(), fieldInput);
        return this;
    }

    /**
     * Puts new {@link FieldInput} with given id and value into fields collection.
     * <p>
     * <strong>Recommended</strong> way to set field value is to use {@link IssueInputBuilder#setFieldValue(String, Object)}.
     *
     * @param id    Field's id
     * @param value Complex value for field
     * @return this
     */
    @SuppressWarnings("unused")
    public IssueInputBuilder setFieldValue(String id, ComplexIssueInputFieldValue value) {
        return setFieldInput(new FieldInput(id, value));
    }

    /**
     * Sets value of field. This method transforms given value to one of understandable by input generator.
     *
     * @param id    Field's id
     * @param value Field's value
     * @return this
     * @throws CannotTransformValueException When transformer cannot transform given value
     */
    public IssueInputBuilder setFieldValue(String id, Object value) throws CannotTransformValueException {
        return setFieldInput(new FieldInput(id, valueTransformerManager.apply(value)));
    }

    public IssueInputBuilder setDescription(String summary) {
        return setFieldInput(new FieldInput(IssueFieldId.DESCRIPTION_FIELD, summary));
    }

    public IssueInputBuilder setAssignee(BasicUser assignee) {
        return setAssigneeName(assignee.getName());
    }

    public IssueInputBuilder setAssigneeName(String assignee) {
        return setFieldInput(new FieldInput(IssueFieldId.ASSIGNEE_FIELD, ComplexIssueInputFieldValue.with("name", assignee)));
    }

    public IssueInput build() {
        return new IssueInput(fields, properties);
    }

    @SuppressWarnings("unused")
    public IssueInputBuilder setAffectedVersions(List<Version> versions) {
        return setAffectedVersionsNames(versions.stream().map(version -> version.getName()).collect(Collectors.toList()));
    }

    public IssueInputBuilder setAffectedVersionsNames(List<String> names) {
        return setFieldInput(new FieldInput(IssueFieldId.AFFECTS_VERSIONS_FIELD, toListOfComplexIssueInputFieldValueWithSingleKey(names, "name")));
    }

    public IssueInputBuilder setComponentsNames(List<String> names) {
        return setFieldInput(new FieldInput(IssueFieldId.COMPONENTS_FIELD, toListOfComplexIssueInputFieldValueWithSingleKey(names, "name")));
    }

    public IssueInputBuilder setComponents(List<BasicComponent> basicComponents) {
        return setComponentsNames(basicComponents.stream().map(component -> component.getName()).collect(Collectors.toList()));
    }

    public IssueInputBuilder setComponents(BasicComponent... basicComponents) {
        return setComponents(Arrays.asList(basicComponents));
    }

    public IssueInputBuilder setDueDate(OffsetDateTime date) {
        return setFieldInput(new FieldInput(IssueFieldId.DUE_DATE_FIELD, date.format(JIRA_DATE_FORMATTER)));
    }

    public IssueInputBuilder setFixVersionsNames(List<String> names) {
        return setFieldInput(new FieldInput(IssueFieldId.FIX_VERSIONS_FIELD, toListOfComplexIssueInputFieldValueWithSingleKey(names, "name")));
    }

    @SuppressWarnings("unused")
    public IssueInputBuilder setFixVersions(List<Version> versions) {
        return setFixVersionsNames(versions.stream().map(version -> version.getName()).collect(Collectors.toList()));
    }

    public IssueInputBuilder setPriority(BasicPriority priority) {
        return setPriorityId(priority.getId());
    }

    public IssueInputBuilder setPriorityId(Long id) {
        return setFieldInput(new FieldInput(IssueFieldId.PRIORITY_FIELD, ComplexIssueInputFieldValue.with("id", id.toString())));
    }

    public IssueInputBuilder setReporter(BasicUser reporter) {
        return setReporterName(reporter.getName());
    }

    public IssueInputBuilder setReporterName(String reporterName) {
        return setFieldInput(new FieldInput(IssueFieldId.REPORTER_FIELD, ComplexIssueInputFieldValue.with("name", reporterName)));
    }

    public IssueInputBuilder addProperty(final String key, final String value) {
        properties.add(new PropertyInput(key, value));
        return this;
    }

    /**
     * This method returns value transformer manager used to transform values by {@link IssueInputBuilder#setFieldValue(String, Object)}.
     * You may use this manager if you want register new custom transformer.
     *
     * @return value transformer manager
     */
    @SuppressWarnings("UnusedDeclaration")
    public ValueTransformerManager getValueTransformerManager() {
        return valueTransformerManager;
    }

    private <T> List<ComplexIssueInputFieldValue> toListOfComplexIssueInputFieldValueWithSingleKey(final List<T> items, final String key) {
        return items.stream() //
                .map(item -> ComplexIssueInputFieldValue.with(key, item)) //
                .collect(Collectors.toList());
    }

}