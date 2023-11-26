/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import me.glindholm.connector.eclipse.internal.jira.core.InvalidJiraQueryException;
import me.glindholm.connector.eclipse.internal.jira.core.JiraRepositoryConnector;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ClassicFilterDataExtractor;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ComponentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.EstimateVsActualFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDataExtractor;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.JQLFilterDataExtractor;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.JiraFieldSpecialValue;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.JiraFields;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.JiraFieldsNames;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.NobodyFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.Order;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.PriorityFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.RelativeDateRangeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.StatusFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.UserFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.UserInGroupFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.VersionFilter;

/**
 * A JiraCustomQuery represents a custom query for issues from a Jira repository.
 */
public class FilterDefinitionConverter {

    /** JQL descending sort order */
    private static final String JQL_SORT_DESCENDING = "DESC"; //$NON-NLS-1$

    /** JQL ascending sort order */
    private static final String JQL_SORT_ASCENDING = "ASC"; //$NON-NLS-1$

    /** JQL ordering */
    private static final String JQL_ORDER_BY = " ORDER BY "; //$NON-NLS-1$

    /** Field name containing text to be searched for classic queries */
    private static final String QUERY_KEY = "query"; //$NON-NLS-1$

    /** Date format used for JQL queries */
    public static final DateFormat JQL_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //$NON-NLS-1$

    /** Date format used for JQL queries */
    public static final DateFormat JQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$

    private final String encoding;

    /** Date format used for classic queries */
    private final DateFormat classicQueryOffsetDateTimeFormat;

    /**
     * @param encoding
     * @param dateFormat date format which has to be used for classic queries; should match the one
     *                   configured in JIRA; in case of JQL format is hard-coded and independent from
     *                   user settings
     */
    public FilterDefinitionConverter(final String encoding, final DateFormat dateFormat) {
        Assert.isNotNull(dateFormat);
        Assert.isNotNull(encoding);
        this.encoding = encoding;
        classicQueryOffsetDateTimeFormat = dateFormat;
    }

    public String toUrl(final String repositoryUrl, final FilterDefinition filter) {
        return repositoryUrl + JiraRepositoryConnector.FILTER_URL_PREFIX + "&reset=true" + getQueryParams(filter); //$NON-NLS-1$
    }

    public String toJqlUrl(final String repositoryUrl, final FilterDefinition filter) throws UnsupportedEncodingException {
        return repositoryUrl + JiraRepositoryConnector.FILTER_URL_PREFIX_NEW + URLEncoder.encode(getJqlString(filter), encoding);
    }

    public FilterDefinition toFilter(final JiraClient client, final String classicUrl, final boolean validate) {
        try {
            return toFilter(client, classicUrl, validate, false, null);
        } catch (JiraException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public FilterDefinition toFilter(final JiraClient client, final String classicUrl, final boolean validate, final boolean update,
            final IProgressMonitor monitor) throws JiraException, UnsupportedEncodingException {
        final FilterDefinition filter = new FilterDefinition();

        final int n = classicUrl.indexOf('?');
        if (n == -1) {
            return filter;
        }

        final HashMap<String, List<String>> params = new HashMap<>();
        for (final String pair : classicUrl.substring(n + 1).split("&")) { //$NON-NLS-1$
            final String[] tokens = pair.split("="); //$NON-NLS-1$
            if (tokens.length > 1) {
                final String key = tokens[0];
                final String value = tokens.length == 1 ? "" : URLDecoder.decode(tokens[1], encoding); //$NON-NLS-1$
                List<String> values = params.get(key);
                if (values == null) {
                    values = new ArrayList<>();
                    params.put(key, values);
                }
                values.add(value);
            }
        }

        final JiraFieldsNames jiraField = JiraFieldsNames.createClassic();
        final List<String> projectIds = getIds(params, jiraField.PROJECT());
        final List<JiraProject> projects = new ArrayList<>();
        for (final String projectId : projectIds) {
            JiraProject project = client.getCache().getProjectById(projectId);
            if (update && (project == null || !project.hasDetails())) {
                project = client.getCache().refreshProjectDetails(projectId, monitor);
            }
            if (project == null) {
                if (validate) {
                    // safeguard
                    throw new InvalidJiraQueryException("Unknown project " + projectId); //$NON-NLS-1$
                } else {
                    continue;
                }
            }
            projects.add(project);
        }

        if (projects.size() > 0) {
            filter.setProjectFilter(new ProjectFilter(projects.toArray(new JiraProject[projects.size()])));

            final List<String> componentIds = getIds(params, jiraField.COMPONENT());
            final Set<JiraComponent> components = new LinkedHashSet<>();
            final Set<JiraVersion> versions = new LinkedHashSet<>();

            boolean hasNoComponent = false;

            for (final JiraProject project : projects) {
                if (!project.hasDetails()) {
                    continue;
                }
                for (final String componentId : componentIds) {
                    final JiraComponent[] projectComponents = project.getComponents();
                    for (final JiraComponent component : projectComponents) {
                        if (component.getId().equals(componentId)) {
                            components.add(component);
                        }
                    }

                    if (componentId.equals(JiraFieldSpecialValue.COMPONENT_NONE.getClassic())) {
                        hasNoComponent = true;
                    }
                }
                versions.addAll(Arrays.asList(project.getVersions()));
            }
            if (!componentIds.isEmpty()) {
                filter.setComponentFilter(new ComponentFilter(components.toArray(new JiraComponent[components.size()]), hasNoComponent));
            }

            final JiraVersion[] projectVersions = versions.toArray(new JiraVersion[versions.size()]);
            filter.setFixForVersionFilter(createVersionFilter(getIds(params, jiraField.FIX_VERSION()), projectVersions));
            filter.setReportedInVersionFilter(createVersionFilter(getIds(params, jiraField.AFFECTED_VERSION()), projectVersions));
        }

        final List<String> typeIds = getIds(params, JiraFields.ISSUE_TYPE.getClassic());
        final List<JiraIssueType> issueTypes = new ArrayList<>();
        for (final String typeId : typeIds) {
            JiraIssueType issueType = null;
            if (projects.size() > 0) {
                for (final JiraProject project : projects) {
                    issueType = project.getIssueTypeById(typeId);
                    if (issueType != null) {
                        break;
                    }
                }
            }

            // fallback - if there are no projects or project issue types are not supported
            if (issueType == null) {
                issueType = client.getCache().getIssueTypeById(typeId);
            }

            if (issueType != null) {
                issueTypes.add(issueType);
            } else if (validate) {
                throw new InvalidJiraQueryException("Unknown type " + typeId); //$NON-NLS-1$
            }
        }
        if (!issueTypes.isEmpty()) {
            filter.setIssueTypeFilter(new IssueTypeFilter(issueTypes.toArray(new JiraIssueType[issueTypes.size()])));
        }

        final List<String> statusIds = getIds(params, JiraFields.STATUS.getClassic());
        final List<JiraStatus> statuses = new ArrayList<>();
        for (final String statusId : statusIds) {
            final JiraStatus status = client.getCache().getStatusById(statusId);
            if (status != null) {
                statuses.add(status);
            } else if (validate) {
                throw new InvalidJiraQueryException("Unknown status " + statusId); //$NON-NLS-1$
            }
        }
        if (!statuses.isEmpty()) {
            filter.setStatusFilter(new StatusFilter(statuses.toArray(new JiraStatus[statuses.size()])));
        }

        final List<String> resolutionIds = getIds(params, JiraFields.RESOLUTION.getClassic());
        final List<JiraResolution> resolutions = new ArrayList<>();
        boolean unresolved = false;
        for (final String resolutionId : resolutionIds) {
            if (!JiraFieldSpecialValue.UNRESOLVED.getClassic().equals(resolutionId)) {
                final JiraResolution resolution = client.getCache().getResolutionById(resolutionId);
                if (resolution != null) {
                    resolutions.add(resolution);
                } else if (validate) {
                    throw new InvalidJiraQueryException("Unknown resolution " + resolutionId); //$NON-NLS-1$
                }
            } else {
                unresolved = true;
            }
        }
        if (!resolutionIds.isEmpty()) {
            filter.setResolutionFilter(new ResolutionFilter(resolutions.toArray(new JiraResolution[resolutions.size()])));
        } else if (unresolved) {
            filter.setResolutionFilter(new ResolutionFilter(new JiraResolution[0]));
        }

        final List<String> priorityIds = getIds(params, JiraFields.PRIORITY.getClassic());
        final List<JiraPriority> priorities = new ArrayList<>();
        for (final String priorityId : priorityIds) {
            final JiraPriority priority = client.getCache().getPriorityById(priorityId);
            if (priority != null) {
                priorities.add(priority);
            } else if (validate) {
                throw new InvalidJiraQueryException("Unknown priority " + priorityId); //$NON-NLS-1$
            }
        }
        if (!priorities.isEmpty()) {
            filter.setPriorityFilter(new PriorityFilter(priorities.toArray(new JiraPriority[priorities.size()])));
        }

        final List<String> queries = getIds(params, QUERY_KEY);
        for (final String query : queries) {
            final boolean searchSummary = getIds(params, jiraField.SUMMARY()).contains("true"); //$NON-NLS-1$
            final boolean searchDescription = getIds(params, jiraField.DESCRIPTION()).contains("true"); //$NON-NLS-1$
            final boolean searchEnvironment = getIds(params, jiraField.ENVIRONMENT()).contains("true"); //$NON-NLS-1$
            final boolean searchComments = getIds(params, jiraField.COMMENT()).contains("true"); //$NON-NLS-1$
            filter.setContentFilter(new ContentFilter(query, searchSummary, searchDescription, searchEnvironment, searchComments));
        }

        filter.setReportedByFilter(createUserFilter(params, JiraFields.REPORTER.getClassic()));
        filter.setAssignedToFilter(createUserFilter(params, JiraFields.ASSIGNEE.getClassic()));

        filter.setCreatedDateFilter(createDateFilter(params, JiraFields.CREATED.getClassic()));
        filter.setUpdatedDateFilter(createDateFilter(params, JiraFields.UPDATED.getClassic()));
        filter.setDueDateFilter(createDateFilter(params, JiraFields.DUE_DATE.getClassic()));

        return filter;
    }

    public String getQueryParams(final FilterDefinition filter) {
        final StringBuilder sb = new StringBuilder();
        final JiraFieldsNames jiraField = JiraFieldsNames.createClassic();
        final FilterDataExtractor classicFilter = new ClassicFilterDataExtractor();

        // project
        addParameters(sb, jiraField.PROJECT(), classicFilter.extractProjects(filter.getProjectFilter()));

        // component
        addParameters(sb, jiraField.COMPONENT(), classicFilter.extractComponents(filter.getComponentFilter()));

        // fix version
        addParameters(sb, jiraField.FIX_VERSION(), classicFilter.extractVersions(filter.getFixForVersionFilter()));

        // affects version
        addParameters(sb, jiraField.AFFECTED_VERSION(), classicFilter.extractVersions(filter.getReportedInVersionFilter()));

        // issue type
        addParameters(sb, jiraField.ISSUE_TYPE(), classicFilter.extractIssueTypes(filter.getIssueTypeFilter()));

        // status
        addParameters(sb, jiraField.STATUS(), classicFilter.extractStatuses(filter.getStatusFilter()));

        // resolution
        addParameters(sb, jiraField.RESOLUTION(), classicFilter.extractResolutions(filter.getResolutionFilter()));

        // priority
        addParameters(sb, jiraField.PRIORITY(), classicFilter.extractPriorities(filter.getPriorityFilter()));

        // content (summary, description, comments, environment)
        final ContentFilter contentFilter = filter.getContentFilter();
        if (contentFilter != null) {
            final String queryString = contentFilter.getQueryString();
            if (queryString != null) {
                addParameter(sb, QUERY_KEY, queryString);
            }
            if (contentFilter.isSearchingSummary()) {
                addParameter(sb, jiraField.SUMMARY(), "true"); //$NON-NLS-1$
            }
            if (contentFilter.isSearchingDescription()) {
                addParameter(sb, jiraField.DESCRIPTION(), "true"); //$NON-NLS-1$
            }
            if (contentFilter.isSearchingComments()) {
                addParameter(sb, jiraField.COMMENT(), "true"); //$NON-NLS-1$
            }
            if (contentFilter.isSearchingEnvironment()) {
                addParameter(sb, jiraField.ENVIRONMENT(), "true"); //$NON-NLS-1$
            }
        }

        // reporter and assignee
        addUserFilter(sb, filter.getReportedByFilter(), JiraFields.REPORTER.getClassic(), JiraFieldSpecialValue.ISSUE_NO_REPORTER.getClassic());
        addUserFilter(sb, filter.getAssignedToFilter(), JiraFields.ASSIGNEE.getClassic(), JiraFieldSpecialValue.UNASSIGNED.getClassic());

        // created, updated, due dates
        addDateFilter(sb, filter.getCreatedDateFilter(), JiraFields.CREATED.getClassic());
        addDateFilter(sb, filter.getUpdatedDateFilter(), JiraFields.UPDATED.getClassic());
        addDateFilter(sb, filter.getDueDateFilter(), JiraFields.DUE_DATE.getClassic());

        // column sorting
        addOrdering(sb, filter.getOrdering());

        // estimation
        final EstimateVsActualFilter estimateFilter = filter.getEstimateVsActualFilter();
        if (estimateFilter != null) {
            final long min = estimateFilter.getMinVariation();
            if (min != 0L) {
                addParameter(sb, "minRatioLimit", Long.toString(min)); //$NON-NLS-1$
            }
            final long max = estimateFilter.getMaxVariation();
            if (max != 0L) {
                addParameter(sb, "maxRatioLimit", Long.toString(max)); //$NON-NLS-1$
            }
        }

        return sb.toString();
    }

    public String getJqlString(final FilterDefinition filter) {
        final List<String> searchParams = new ArrayList<>();
        final JiraFieldsNames jiraField = JiraFieldsNames.createJql();
        final FilterDataExtractor jqlFilter = new JQLFilterDataExtractor();

        // project
        addJqlInExpression(searchParams, jiraField.PROJECT(),
                jqlFilter.extractProjects(filter.getProjectFilter()).stream().map(element -> "\"" + element + "\"").collect(Collectors.toList()));

        // component
        addJqlInExpression(searchParams, jiraField.COMPONENT(), jqlFilter.extractComponents(filter.getComponentFilter()));

        // fix version
        addJqlInExpression(searchParams, jiraField.FIX_VERSION(), jqlFilter.extractVersions(filter.getFixForVersionFilter()));

        // affects version
        addJqlInExpression(searchParams, jiraField.AFFECTED_VERSION(), jqlFilter.extractVersions(filter.getReportedInVersionFilter()));

        // issue type
        addJqlInExpression(searchParams, jiraField.ISSUE_TYPE(), jqlFilter.extractIssueTypes(filter.getIssueTypeFilter()));

        // status
        addJqlInExpression(searchParams, jiraField.STATUS(), jqlFilter.extractStatuses(filter.getStatusFilter()));

        // resolution
        addJqlInExpression(searchParams, jiraField.RESOLUTION(), jqlFilter.extractResolutions(filter.getResolutionFilter()));

        // priority
        addJqlInExpression(searchParams, jiraField.PRIORITY(), jqlFilter.extractPriorities(filter.getPriorityFilter()));

        // content (summary, description, environment, comments)
        final ContentFilter contentFilter = filter.getContentFilter();
        if (contentFilter != null && contentFilter.getQueryString() != null && contentFilter.getQueryString().length() > 0) {
            final String searchedString = " ~ \"" + contentFilter.getQueryString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
            final List<String> jqlOrQueryParts = new ArrayList<>(4);
            if (contentFilter.isSearchingSummary()) {
                jqlOrQueryParts.add(jiraField.SUMMARY() + searchedString);
            }
            if (contentFilter.isSearchingDescription()) {
                jqlOrQueryParts.add(jiraField.DESCRIPTION() + searchedString);
            }
            if (contentFilter.isSearchingComments()) {
                jqlOrQueryParts.add(jiraField.COMMENT() + searchedString);
            }
            if (contentFilter.isSearchingEnvironment()) {
                jqlOrQueryParts.add(jiraField.ENVIRONMENT() + searchedString);
            }

            // query like: (summary ~ "boo" OR comment ~ "boo")
            addJqlOrExpression(searchParams, jqlOrQueryParts);
        }

        // reporter
        addJqlInExpression(searchParams, jiraField.REPORTER(), jqlFilter.extractReportedBy(filter.getReportedByFilter()));

        // assignee
        addJqlInExpression(searchParams, jiraField.ASSIGNEE(), jqlFilter.extractAssignedTo(filter.getAssignedToFilter()));

        // created, updated, due dates
        addJqlAndExpression(searchParams, jiraField.CREATED(), jqlFilter.extractDates(filter.getCreatedDateFilter(), JQL_DATE_TIME_FORMAT));
        addJqlAndExpression(searchParams, jiraField.UPDATED(), jqlFilter.extractDates(filter.getUpdatedDateFilter(), JQL_DATE_TIME_FORMAT));
        addJqlAndExpression(searchParams, jiraField.DUE_DATE(), jqlFilter.extractDates(filter.getDueDateFilter(), JQL_DATE_FORMAT));

        // estimations
        addJqlAndExpression(searchParams, jiraField.WORK_RATIO(), jqlFilter.extractWorkRatios(filter.getEstimateVsActualFilter()));

        final String whereClause = StringUtils.join(searchParams, " AND "); //$NON-NLS-1$
        final String orderByClause = getJqlOrdering(filter.getOrdering());

        return whereClause + " " + orderByClause; //$NON-NLS-1$
    }

    private VersionFilter createVersionFilter(final List<String> fixForIds, final JiraVersion[] projectVersions) {
        if (fixForIds.isEmpty()) {
            return null;
        }

        boolean hasNoVersions = false;
        boolean hasReleasedVersions = false;
        boolean hasUnreleasedVersions = false;
        final List<JiraVersion> fixForversions = new ArrayList<>();
        for (final String fixForId : fixForIds) {
            if (fixForId.equals(JiraFieldSpecialValue.VERSION_NONE.getClassic())) {
                hasNoVersions = true;
            }
            if (fixForId.equals(JiraFieldSpecialValue.VERSION_RELEASED.getClassic())) {
                hasReleasedVersions = true;
            }
            if (fixForId.equals(JiraFieldSpecialValue.VERSION_UNRELEASED.getClassic())) {
                hasUnreleasedVersions = true;
            }

            for (final JiraVersion projectVersion : projectVersions) {
                if (projectVersion.getId().equals(fixForId)) {
                    fixForversions.add(projectVersion);
                }
            }
        }

        return new VersionFilter(fixForversions.toArray(new JiraVersion[fixForversions.size()]), hasNoVersions, hasReleasedVersions, hasUnreleasedVersions);

    }

    private DateFilter createDateFilter(final Map<String, List<String>> params, final String key) {
        final String after = getId(params, key + ":after"); //$NON-NLS-1$
        final String before = getId(params, key + ":before"); //$NON-NLS-1$

        Instant afterDate = null;
        try {
            if (after != null) {
                afterDate = classicQueryOffsetDateTimeFormat.parse(after).toInstant();
            }
        } catch (final ParseException ex) {
            // swallow
        }
        Instant beforeDate = null;
        try {
            if (before != null) {
                beforeDate = classicQueryOffsetDateTimeFormat.parse(before).toInstant();
            }
        } catch (final ParseException ex) {
            // swallow
        }

        final String previous = getId(params, key + ":previous"); //$NON-NLS-1$
        final String next = getId(params, key + ":next"); //$NON-NLS-1$

        return afterDate == null && beforeDate == null && previous == null && next == null ? null : new DateRangeFilter(afterDate, beforeDate, previous, next);
    }

    private UserFilter createUserFilter(final Map<String, List<String>> params, final String key) {
        final String type = getId(params, key + "Select"); //$NON-NLS-1$
        if (JiraFieldSpecialValue.ISSUE_NO_REPORTER.getClassic().equals(type) || JiraFieldSpecialValue.UNASSIGNED.getClassic().equals(type)) {
            return new NobodyFilter();
        } else if (JiraFieldSpecialValue.ISSUE_CURRENT_USER.getClassic().equals(type)) {
            return new CurrentUserFilter();
        } else {
            final String reporter = getId(params, key);
            if (reporter != null) {
                if (JiraFieldSpecialValue.ISSUE_SPECIFIC_USER.getClassic().equals(type)) {
                    return new SpecificUserFilter(reporter);
                } else if (JiraFieldSpecialValue.ISSUE_SPECIFIC_GROUP.getClassic().equals(type)) {
                    return new UserInGroupFilter(reporter);
                }
            }
        }
        return null;
    }

    private String getId(final Map<String, List<String>> params, final String key) {
        final List<String> ids = getIds(params, key);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private List<String> getIds(final Map<String, List<String>> params, final String key) {
        final List<String> ids = params.get(key);
        if (ids == null) {
            return Collections.emptyList();
        }
        return ids;
    }

    private String getJqlOrdering(final Order[] ordering) {
        if (ordering.length == 0) {
            return ""; //$NON-NLS-1$
        } else {
            final StringBuilder sb = new StringBuilder(JQL_ORDER_BY);
            for (int i = 0; i < ordering.length; i++) {
                sb.append(ordering[i].getField().getJql());
                sb.append(" "); //$NON-NLS-1$
                sb.append(ordering[i].isAscending() ? JQL_SORT_ASCENDING : JQL_SORT_DESCENDING);
                if (i < ordering.length - 1) {
                    sb.append(", "); //$NON-NLS-1$
                }
            }

            return sb.toString();
        }
    }

    private String getOrdering(final Order[] ordering) {
        final StringBuilder sb = new StringBuilder();
        for (final Order order : ordering) {
            final String fieldName = order.getField().getClassic();
            if (fieldName == null) {
                continue;
            }
            addParameter(sb, "sorter/field", fieldName); //$NON-NLS-1$
            addParameter(sb, "sorter/order", order.isAscending() ? "ASC" : "DESC"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        return sb.toString();
    }

    private void addOrdering(final StringBuilder sb, final Order[] ordering) {
        sb.append(getOrdering(ordering));
    }

    private void addDateFilter(final StringBuilder sb, final DateFilter filter, final String type) {
        if (filter instanceof final DateRangeFilter rangeFilter) {
            if (rangeFilter.getFromDate() != null) {
                addParameter(sb, type + ":after", classicQueryOffsetDateTimeFormat.format(Date.from(rangeFilter.getFromDate()))); //$NON-NLS-1$
            }
            if (rangeFilter.getToDate() != null) {
                addParameter(sb, type + ":before", classicQueryOffsetDateTimeFormat.format(Date.from(rangeFilter.getToDate()))); //$NON-NLS-1$
            }
            if (rangeFilter.getFrom() != null && rangeFilter.getFrom().length() > 0) {
                addParameter(sb, type + ":previous", rangeFilter.getFrom()); //$NON-NLS-1$
            }
            if (rangeFilter.getTo() != null && rangeFilter.getTo().length() > 0) {
                addParameter(sb, type + ":next", rangeFilter.getTo()); //$NON-NLS-1$
            }
        } else if (filter instanceof final RelativeDateRangeFilter rangeFilter) {
            if (rangeFilter.previousMilliseconds() != 0L) {
                addParameter(sb, type + ":previous", FilterDataExtractor.createRelativeDateString(rangeFilter.getPreviousRangeType(), //$NON-NLS-1$
                        rangeFilter.getPreviousCount()));
            }
            if (rangeFilter.nextMilliseconds() != 0L) {
                addParameter(sb, type + ":next", FilterDataExtractor.createRelativeDateString(rangeFilter.getNextRangeType(), //$NON-NLS-1$
                        rangeFilter.getNextCount()));
            }
        }
    }

    private void addUserFilter(final StringBuilder sb, final UserFilter filter, final String type, final String nobodyText) {
        if (filter instanceof NobodyFilter) {
            addParameter(sb, type + "Select", nobodyText); //$NON-NLS-1$
        } else if (filter instanceof CurrentUserFilter) {
            addParameter(sb, type + "Select", JiraFieldSpecialValue.ISSUE_CURRENT_USER.getClassic()); //$NON-NLS-1$
        } else if (filter instanceof SpecificUserFilter) {
            addParameter(sb, type + "Select", JiraFieldSpecialValue.ISSUE_SPECIFIC_USER.getClassic()); //$NON-NLS-1$
            addParameter(sb, type, ((SpecificUserFilter) filter).getUser());
        } else if (filter instanceof UserInGroupFilter) {
            addParameter(sb, type + "Select", JiraFieldSpecialValue.ISSUE_SPECIFIC_GROUP.getClassic()); //$NON-NLS-1$
            addParameter(sb, type, ((UserInGroupFilter) filter).getGroup());
        }
    }

    /**
     * Adds list of parameters to classic query, using the same <code>name</code> key for all
     * <code>values</code>. Something like: <code>&name=values[0]&name=values[1]...</code>. It applies
     * URL encoding on values.
     *
     * @param sb     output string
     * @param name
     * @param values
     */
    private void addParameters(final StringBuilder sb, final String name, final Collection<String> values) {
        // ignore
        for (final String value : values) {
            addParameter(sb, name, value);
        }
    }

    /**
     * Adds a single parameter to classic query, like <code>&name=value</code>. It applies URL encoding
     * on the value.
     *
     * @param sb    output string
     * @param name
     * @param value
     */
    private void addParameter(final StringBuilder sb, final String name, final String value) {
        try {
            sb.append('&').append(name).append('=').append(URLEncoder.encode(value, encoding));
        } catch (final UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Appends new part of JQL query with a "key in (value1, value2, ... )" expression. Example:
     *
     * <pre>
     *    priority in ( Blocker, Critical, Major )
     * </pre>
     *
     * @param searchParams output list where new expression will be added
     * @param field        name of the jira field
     * @param values       list of key values we search for, list can be empty
     */
    private void addJqlInExpression(final Collection<String> searchParams, final String key, final Collection<String> values) {
        // don't append expression if there are no values
        if (values.size() == 0) {
            return;
        }

        // create "key in (value1, value2, ... )" query
        final StringBuilder param = new StringBuilder();
        param.append(key);
        param.append(" in ("); //$NON-NLS-1$
        param.append(StringUtils.join(values, ",")); //$NON-NLS-1$
        param.append(")"); //$NON-NLS-1$
        // add to output list
        searchParams.add(param.toString());
    }

    /**
     * Appends new part of JQL query with expressions concatenated with OR operator. Example:
     *
     * <pre>
     *    ( expression1 OR expression2 OR expression3 )
     * </pre>
     *
     * @param searchParams
     * @param expressions
     */
    private void addJqlOrExpression(final Collection<String> searchParams, final Collection<String> expressions) {
        if (expressions.size() > 0) {
            searchParams.add("(" + StringUtils.join(expressions, " OR ") + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    /**
     * Appends new part of JQL query with expressions concatenated with AND operator. Example:
     *
     * <pre>
     *    ( field operatorAndValue1 AND field operatorAndValue2 AND field operatorAndValue3 )
     * </pre>
     *
     * @param searchParams
     * @param expressions
     */
    private void addJqlAndExpression(final Collection<String> searchParams, final String field, final Collection<String> operatorAndValue) {
        if (operatorAndValue.size() > 0) {
            final StringBuilder buffer = new StringBuilder("("); //$NON-NLS-1$
            for (final Iterator<String> valueIter = operatorAndValue.iterator(); valueIter.hasNext();) {
                buffer.append(field);
                buffer.append(" "); //$NON-NLS-1$
                buffer.append(valueIter.next());
                if (valueIter.hasNext()) {
                    buffer.append(" AND "); //$NON-NLS-1$
                }
            }
            buffer.append(")"); //$NON-NLS-1$

            searchParams.add(buffer.toString());
        }
    }

}
