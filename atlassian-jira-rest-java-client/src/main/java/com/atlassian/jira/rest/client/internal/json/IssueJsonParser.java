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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.BasicIssueType;
import com.atlassian.jira.rest.client.domain.BasicPriority;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicResolution;
import com.atlassian.jira.rest.client.domain.BasicStatus;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.BasicVotes;
import com.atlassian.jira.rest.client.domain.BasicWatchers;
import com.atlassian.jira.rest.client.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueFieldId;
import com.atlassian.jira.rest.client.domain.IssueLink;
import com.atlassian.jira.rest.client.domain.Subtask;
import com.atlassian.jira.rest.client.domain.TimeTracking;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.rest.client.domain.IssueFieldId.AFFECTS_VERSIONS_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.ASSIGNEE_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.ATTACHMENT_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.COMMENT_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.COMPONENTS_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.CREATED_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.DESCRIPTION_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.DUE_DATE_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.FIX_VERSIONS_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.ISSUE_TYPE_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.LABELS_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.LINKS_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.LINKS_PRE_5_0_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.PRIORITY_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.PROJECT_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.REPORTER_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.RESOLUTION_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.STATUS_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.SUBTASKS_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.SUMMARY_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.TIMETRACKING_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.TRANSITIONS_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.UPDATED_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.VOTES_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.WATCHER_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.WATCHER_PRE_5_0_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.WORKLOGS_FIELD;
import static com.atlassian.jira.rest.client.domain.IssueFieldId.WORKLOG_FIELD;
import static com.atlassian.jira.rest.client.internal.json.JsonParseUtil.getStringKeys;

public class IssueJsonParser implements JsonParser<Issue> {

	private static Set<String> SPECIAL_FIELDS = Sets.newHashSet(IssueFieldId.ids());

	public static final String SCHEMA_SECTION = "schema";
	public static final String NAMES_SECTION = "names";

	private final IssueLinkJsonParser issueLinkJsonParser = new IssueLinkJsonParser();
	private final IssueLinkJsonParserV5 issueLinkJsonParserV5 = new IssueLinkJsonParserV5();
	private final BasicVotesJsonParser votesJsonParser = new BasicVotesJsonParser();
	private final BasicStatusJsonParser statusJsonParser = new BasicStatusJsonParser();
	private final WorklogJsonParser worklogJsonParser = new WorklogJsonParser();
	private final JsonParser<BasicWatchers> watchersJsonParser
			= WatchersJsonParserBuilder.createBasicWatchersParser();
	private final VersionJsonParser versionJsonParser = new VersionJsonParser();
	private final BasicComponentJsonParser basicComponentJsonParser = new BasicComponentJsonParser();
	private final AttachmentJsonParser attachmentJsonParser = new AttachmentJsonParser();
	private final JsonFieldParser fieldParser = new JsonFieldParser();
	private final CommentJsonParser commentJsonParser = new CommentJsonParser();
	private final BasicIssueTypeJsonParser issueTypeJsonParser = new BasicIssueTypeJsonParser();
	private final BasicProjectJsonParser projectJsonParser = new BasicProjectJsonParser();
	private final BasicPriorityJsonParser priorityJsonParser = new BasicPriorityJsonParser();
	private final BasicResolutionJsonParser resolutionJsonParser = new BasicResolutionJsonParser();
	private final BasicUserJsonParser userJsonParser = new BasicUserJsonParser();
	private final SubtaskJsonParser subtaskJsonParser = new SubtaskJsonParser();
	private final ChangelogJsonParser changelogJsonParser = new ChangelogJsonParser();
	private final JsonWeakParserForString jsonWeakParserForString = new JsonWeakParserForString();

	private static final String FIELDS = "fields";
	private static final String VALUE_ATTR = "value";

	static Iterable<String> parseExpandos(JSONObject json) throws JSONException {
		final String expando = json.getString("expand");
		return Splitter.on(',').split(expando);
	}


	private <T> Collection<T> parseArray(JSONObject jsonObject, JsonWeakParser<T> jsonParser, String arrayAttribute)
			throws JSONException {
//        String type = jsonObject.getString("type");
//        final String name = jsonObject.getString("name");
		final JSONArray valueObject = jsonObject.optJSONArray(arrayAttribute);
		if (valueObject == null) {
			return new ArrayList<T>();
		}
		Collection<T> res = new ArrayList<T>(valueObject.length());
		for (int i = 0; i < valueObject.length(); i++) {
			res.add(jsonParser.parse(valueObject.get(i)));
		}
		return res;
	}

	private <T> Collection<T> parseOptionalArrayNotNullable(boolean shouldUseNestedValueJson, JSONObject json, JsonWeakParser<T> jsonParser, String... path)
			throws JSONException {
		Collection<T> res = parseOptionalArray(shouldUseNestedValueJson, json, jsonParser, path);
		return res == null ? Collections.<T>emptyList() : res;
	}

	@Nullable
	private <T> Collection<T> parseOptionalArray(boolean shouldUseNestedValueJson, JSONObject json, JsonWeakParser<T> jsonParser, String... path)
			throws JSONException {
		if (shouldUseNestedValueJson) {
			final JSONObject js = JsonParseUtil.getNestedOptionalObject(json, path);
			if (js == null) {
				return null;
			}
			return parseArray(js, jsonParser, VALUE_ATTR);
		} else {
			final JSONArray jsonArray = JsonParseUtil.getNestedOptionalArray(json, path);
			if (jsonArray == null) {
				return null;
			}
			final Collection<T> res = new ArrayList<T>(jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				res.add(jsonParser.parse(jsonArray.get(i)));
			}
			return res;
		}
	}

	private String getFieldStringValue(JSONObject json, String attributeName) throws JSONException {
		final JSONObject fieldsJson = json.getJSONObject(FIELDS);

		final Object summaryObject = fieldsJson.get(attributeName);
		if (summaryObject instanceof JSONObject) { // pre JIRA 5.0 way
			return ((JSONObject) summaryObject).getString(VALUE_ATTR);
		}
		if (summaryObject instanceof String) { // JIRA 5.0 way
			return (String) summaryObject;
		}
		throw new JSONException("Cannot parse [" + attributeName + "] from available fields");
	}

	private JSONObject getFieldUnisex(JSONObject json, String attributeName) throws JSONException {
		final JSONObject fieldsJson = json.getJSONObject(FIELDS);
		final JSONObject fieldJson = fieldsJson.getJSONObject(attributeName);
		if (fieldJson.has(VALUE_ATTR)) {
			return fieldJson.getJSONObject(VALUE_ATTR); // pre 5.0 way
		} else {
			return fieldJson; // JIRA 5.0 way
		}
	}

	@Nullable
	private String getOptionalFieldStringUnisex(boolean shouldUseNestedValueJson, JSONObject json, String attributeName)
			throws JSONException {
		final JSONObject fieldsJson = json.getJSONObject(FIELDS);
		if (shouldUseNestedValueJson) {
			final JSONObject fieldJson = fieldsJson.optJSONObject(attributeName);
			if (fieldJson != null) {
				return JsonParseUtil.getOptionalString(fieldJson, VALUE_ATTR); // pre 5.0 way
			} else {
				return null;
			}
		}
		return JsonParseUtil.getOptionalString(fieldsJson, attributeName);
	}

	private String getFieldStringUnisex(JSONObject json, String attributeName) throws JSONException {
		final JSONObject fieldsJson = json.getJSONObject(FIELDS);
		final Object fieldJson = fieldsJson.get(attributeName);
		if (fieldJson instanceof JSONObject) {
			return ((JSONObject) fieldJson).getString(VALUE_ATTR); // pre 5.0 way
		}
		return fieldJson.toString(); // JIRA 5.0 way
	}

	@Override
	public Issue parse(JSONObject s) throws JSONException {
		final Iterable<String> expandos = parseExpandos(s);
		final boolean isJira5x0OrNewer = Iterables.contains(expandos, SCHEMA_SECTION);
		final boolean shouldUseNestedValueAttribute = !isJira5x0OrNewer;
		final Collection<Comment> comments;
		if (isJira5x0OrNewer) {
			final JSONObject commentsJson = s.getJSONObject(FIELDS).getJSONObject(COMMENT_FIELD.id);
			comments = parseArray(commentsJson, new JsonWeakParserForJsonObject<Comment>(commentJsonParser), "comments");

		} else {
			final Collection<Comment> commentsTmp = parseOptionalArray(
					shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<Comment>(commentJsonParser), FIELDS, COMMENT_FIELD.id);
			comments = commentsTmp != null ? commentsTmp : Lists.<Comment>newArrayList();
		}

		final String summary = getFieldStringValue(s, SUMMARY_FIELD.id);
		final String description = getOptionalFieldStringUnisex(shouldUseNestedValueAttribute, s, DESCRIPTION_FIELD.id);

		final Collection<Attachment> attachments = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<Attachment>(attachmentJsonParser), FIELDS, ATTACHMENT_FIELD.id);
		final Collection<Field> fields = isJira5x0OrNewer ? parseFieldsJira5x0(s) : parseFields(s.getJSONObject(FIELDS));

		final BasicIssueType issueType = issueTypeJsonParser.parse(getFieldUnisex(s, ISSUE_TYPE_FIELD.id));
		final DateTime creationDate = JsonParseUtil.parseDateTime(getFieldStringUnisex(s, CREATED_FIELD.id));
		final DateTime updateDate = JsonParseUtil.parseDateTime(getFieldStringUnisex(s, UPDATED_FIELD.id));

		final String dueDateString = getOptionalFieldStringUnisex(shouldUseNestedValueAttribute, s, DUE_DATE_FIELD.id);
		final DateTime dueDate = dueDateString == null ? null : JsonParseUtil.parseDateTimeOrDate(dueDateString);

		final BasicPriority priority = getOptionalField(shouldUseNestedValueAttribute, s, PRIORITY_FIELD.id, priorityJsonParser);
		final BasicResolution resolution = getOptionalField(shouldUseNestedValueAttribute, s, RESOLUTION_FIELD.id, resolutionJsonParser);
		final BasicUser assignee = getOptionalField(shouldUseNestedValueAttribute, s, ASSIGNEE_FIELD.id, userJsonParser);
		final BasicUser reporter = getOptionalField(shouldUseNestedValueAttribute, s, REPORTER_FIELD.id, userJsonParser);

		final String transitionsUri = getOptionalFieldStringUnisex(shouldUseNestedValueAttribute, s, TRANSITIONS_FIELD.id);
		final BasicProject project = projectJsonParser.parse(getFieldUnisex(s, PROJECT_FIELD.id));
		final Collection<IssueLink> issueLinks;
		if (isJira5x0OrNewer) {
			issueLinks = parseOptionalArray(shouldUseNestedValueAttribute, s,
					new JsonWeakParserForJsonObject<IssueLink>(issueLinkJsonParserV5), FIELDS, LINKS_FIELD.id);
		} else {
			issueLinks = parseOptionalArray(shouldUseNestedValueAttribute, s,
					new JsonWeakParserForJsonObject<IssueLink>(issueLinkJsonParser), FIELDS, LINKS_PRE_5_0_FIELD.id);
		}

		Collection<Subtask> subtasks = null;
		if (isJira5x0OrNewer) {
			subtasks = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<Subtask>(subtaskJsonParser), FIELDS, SUBTASKS_FIELD.id);
		}

		final BasicVotes votes = getOptionalField(shouldUseNestedValueAttribute, s, VOTES_FIELD.id, votesJsonParser);
		final BasicStatus status = statusJsonParser.parse(getFieldUnisex(s, STATUS_FIELD.id));

		final Collection<Version> fixVersions = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<Version>(versionJsonParser), FIELDS, FIX_VERSIONS_FIELD.id);
		final Collection<Version> affectedVersions = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<Version>(versionJsonParser), FIELDS, AFFECTS_VERSIONS_FIELD.id);
		final Collection<BasicComponent> components = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<BasicComponent>(basicComponentJsonParser), FIELDS, COMPONENTS_FIELD.id);

		final Collection<Worklog> worklogs;
		if (isJira5x0OrNewer) {
			if (JsonParseUtil.getNestedOptionalObject(s, FIELDS, WORKLOG_FIELD.id) != null) {
				worklogs = parseOptionalArray(shouldUseNestedValueAttribute, s,
						new JsonWeakParserForJsonObject<Worklog>(new WorklogJsonParserV5(JsonParseUtil.getSelfUri(s))),
						FIELDS, WORKLOG_FIELD.id, WORKLOGS_FIELD.id);
			} else {
				worklogs = Collections.emptyList();
			}
		} else {
			worklogs = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<Worklog>(worklogJsonParser), FIELDS, WORKLOG_FIELD.id);
		}

		final BasicWatchers watchers = getOptionalField(shouldUseNestedValueAttribute, s,
				isJira5x0OrNewer ? WATCHER_FIELD.id : WATCHER_PRE_5_0_FIELD.id, watchersJsonParser);
		final TimeTracking timeTracking = getOptionalField(shouldUseNestedValueAttribute, s, TIMETRACKING_FIELD.id,
				isJira5x0OrNewer ? new TimeTrackingJsonParserV5() : new TimeTrackingJsonParser());

		final Set<String> labels = Sets.newHashSet(parseOptionalArrayNotNullable(shouldUseNestedValueAttribute, s,
				jsonWeakParserForString, FIELDS, LABELS_FIELD.id));

		final Collection<ChangelogGroup> changelog = parseOptionalArray(false, s, new JsonWeakParserForJsonObject<ChangelogGroup>(changelogJsonParser), "changelog", "histories");
		return new Issue(summary, JsonParseUtil.getSelfUri(s), s.getString("key"), project, issueType, status,
				description, priority, resolution, attachments, reporter, assignee, creationDate, updateDate,
				dueDate, affectedVersions, fixVersions, components, timeTracking, fields, comments,
				transitionsUri != null ? JsonParseUtil.parseURI(transitionsUri) : null, issueLinks,
				votes, worklogs, watchers, expandos, subtasks, changelog, labels);
	}

	@Nullable
	private <T> T getOptionalField(boolean shouldUseNestedValue, JSONObject s, final String fieldId, JsonParser<T> jsonParser)
			throws JSONException {
		final JSONObject fieldJson = JsonParseUtil.getNestedOptionalObject(s, FIELDS, fieldId);
		// for fields like assignee (when unassigned) value attribute may be missing completely
		if (fieldJson != null) {
			if (shouldUseNestedValue) {
				final JSONObject valueJsonObject = fieldJson.optJSONObject(VALUE_ATTR);
				if (valueJsonObject != null) {
					return jsonParser.parse(valueJsonObject);
				}

			} else {
				return jsonParser.parse(fieldJson);
			}

		}
		return null;
	}

	private Collection<Field> parseFieldsJira5x0(JSONObject issueJson) throws JSONException {
		final JSONObject names = issueJson.optJSONObject(NAMES_SECTION);
		final Map<String, String> namesMap = parseNames(names);
		final JSONObject types = issueJson.optJSONObject(SCHEMA_SECTION);
		final Map<String, String> typesMap = parseSchema(types);

		final JSONObject json = issueJson.getJSONObject(FIELDS);
		final ArrayList<Field> res = new ArrayList<Field>(json.length());
		@SuppressWarnings("unchecked")
		final Iterator<String> iterator = json.keys();
		while (iterator.hasNext()) {
			final String key = iterator.next();
			try {
				if (SPECIAL_FIELDS.contains(key)) {
					continue;
				}
				final Object value = json.opt(key);
				res.add(new Field(key, namesMap.get(key), typesMap.get("key"), value != JSONObject.NULL ? value : null));
			} catch (final Exception e) {
				throw new JSONException("Error while parsing [" + key + "] field: " + e.getMessage()) {
					@Override
					public Throwable getCause() {
						return e;
					}
				};
			}
		}
		return res;
	}

	private Map<String, String> parseSchema(JSONObject json) throws JSONException {
		final HashMap<String, String> res = Maps.newHashMap();
		final Iterator<String> it = getStringKeys(json);
		while (it.hasNext()) {
			final String fieldId = it.next();
			JSONObject fieldDefinition = json.getJSONObject(fieldId);
			res.put(fieldId, fieldDefinition.getString("type"));

		}
		return res;
	}

	private Map<String, String> parseNames(JSONObject json) throws JSONException {
		final HashMap<String, String> res = Maps.newHashMap();
		final Iterator<String> iterator = getStringKeys(json);
		while (iterator.hasNext()) {
			final String key = iterator.next();
			res.put(key, json.getString(key));
		}
		return res;
	}


	private Collection<Field> parseFields(JSONObject json) throws JSONException {
		ArrayList<Field> res = new ArrayList<Field>(json.length());
		final Iterator<String> iterator = getStringKeys(json);
		while (iterator.hasNext()) {
			final String key = iterator.next();
			if (SPECIAL_FIELDS.contains(key)) {
				continue;
			}
			res.add(fieldParser.parse(json.getJSONObject(key), key));
		}
		return res;
	}

}
