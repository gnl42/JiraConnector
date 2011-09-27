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
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueLink;
import com.atlassian.jira.rest.client.domain.TimeTracking;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class IssueJsonParser implements JsonParser<Issue> {
	private static final String UPDATED_ATTR = "updated";
	private static final String CREATED_ATTR = "created";
	private static final String AFFECTS_VERSIONS_ATTR = "versions";
	private static final String FIX_VERSIONS_ATTR = "fixVersions";
	private static final String COMPONENTS_ATTR = "components";
	private static final String LINKS_ATTR = "links";
	private static final String ISSUE_TYPE_ATTR = "issuetype";
	private static final String VOTES_ATTR = "votes";
	private static final String WORKLOG_ATTR = "worklog";
	private static final String WATCHER_ATTR = "watcher";
	private static final String PROJECT_ATTR = "project";
	private static final String STATUS_ATTR = "status";
	private static final String COMMENT_ATTR = "comment";
	private static final String PRIORITY_ATTR = "priority";
	private static final String ATTACHMENT_ATTR = "attachment";
	private static final String RESOLUTION_ATTR = "resolution";
	private static final String ASSIGNEE_ATTR = "assignee";
	private static final String REPORTER_ATTR = "reporter";
	private static final String SUMMARY_ATTR = "summary";
	private static final String DESCRIPTION_ATTR = "description";
	private static final String TIMETRACKING_ATTR = "timetracking";

	private static Set<String> SPECIAL_FIELDS = new HashSet<String>(Arrays.asList(SUMMARY_ATTR, UPDATED_ATTR, CREATED_ATTR,
			AFFECTS_VERSIONS_ATTR, FIX_VERSIONS_ATTR, COMPONENTS_ATTR, LINKS_ATTR, ISSUE_TYPE_ATTR, VOTES_ATTR,
			WORKLOG_ATTR, WATCHER_ATTR, PROJECT_ATTR, STATUS_ATTR, COMMENT_ATTR, ATTACHMENT_ATTR, SUMMARY_ATTR, DESCRIPTION_ATTR,
			PRIORITY_ATTR, RESOLUTION_ATTR, ASSIGNEE_ATTR, REPORTER_ATTR, TIMETRACKING_ATTR));
	public static final String SCHEMA_SECTION = "schema";
	public static final String NAMES_SECTION = "names";

	private final IssueLinkJsonParser issueLinkJsonParser = new IssueLinkJsonParser();
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

	private static final String FIELDS = "fields";
	private static final String VALUE_ATTR = "value";

	static Iterable<String> parseExpandos(JSONObject json) throws JSONException {
		final String expando = json.getString("expand");
		return Splitter.on(',').split(expando);
	}


	private <T> Collection<T> parseArray(JSONObject jsonObject, JsonWeakParser<T> jsonParser, String arrayAttribute) throws JSONException {
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


	@Nullable
	private <T> Collection<T> parseOptionalArray(boolean shouldUseNestedValueJson, JSONObject json, JsonWeakParser<T> jsonParser, String... path) throws JSONException {
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
	private String getOptionalFieldStringUnisex(boolean shouldUseNestedValueJson, JSONObject json, String attributeName) throws JSONException {
		final JSONObject fieldsJson = json.getJSONObject(FIELDS);
		if (shouldUseNestedValueJson) {
			final JSONObject fieldJson = fieldsJson.optJSONObject(attributeName);
			if (fieldJson != null) {
				return JsonParseUtil.getOptionalString(((JSONObject) fieldJson), VALUE_ATTR); // pre 5.0 way
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
			return ((JSONObject)fieldJson).getString(VALUE_ATTR); // pre 5.0 way
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
			final JSONObject commentsJson = s.getJSONObject(FIELDS).getJSONObject(COMMENT_ATTR);
			comments = parseArray(commentsJson, new JsonWeakParserForJsonObject<Comment>(commentJsonParser), "comments");

		} else {
			final Collection<Comment> commentsTmp = parseOptionalArray(
					shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<Comment>(commentJsonParser), FIELDS, COMMENT_ATTR);
			comments = commentsTmp != null ? commentsTmp : Lists.<Comment>newArrayList();
		}



		final String summary = getFieldStringValue(s, SUMMARY_ATTR);
		final String description = getOptionalFieldStringUnisex(shouldUseNestedValueAttribute, s, DESCRIPTION_ATTR);

		final Collection<Attachment> attachments = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<Attachment>(attachmentJsonParser), FIELDS, ATTACHMENT_ATTR);
		final Collection<Field> fields = isJira5x0OrNewer ? parseFieldsJira5x0(s) : parseFields(s.getJSONObject(FIELDS));

		final BasicIssueType issueType = issueTypeJsonParser.parse(getFieldUnisex(s, ISSUE_TYPE_ATTR));
		final DateTime creationDate = JsonParseUtil.parseDateTime(getFieldStringUnisex(s, CREATED_ATTR));
		final DateTime updateDate = JsonParseUtil.parseDateTime(getFieldStringUnisex(s, UPDATED_ATTR));

		final BasicPriority priority = getOptionalField(shouldUseNestedValueAttribute, s, PRIORITY_ATTR, priorityJsonParser);
		final BasicResolution resolution = getOptionalField(shouldUseNestedValueAttribute, s, RESOLUTION_ATTR, resolutionJsonParser);
		final BasicUser assignee = getOptionalField(shouldUseNestedValueAttribute, s, ASSIGNEE_ATTR, userJsonParser);
		final BasicUser reporter = getOptionalField(shouldUseNestedValueAttribute, s, REPORTER_ATTR, userJsonParser);

		final URI transitionsUri = JsonParseUtil.parseURI(s.getString("transitions"));
		final BasicProject project = projectJsonParser.parse(getFieldUnisex(s, PROJECT_ATTR));
		final Collection<IssueLink> issueLinks = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<IssueLink>(issueLinkJsonParser), FIELDS, LINKS_ATTR);
		final BasicVotes votes = getOptionalField(shouldUseNestedValueAttribute, s, VOTES_ATTR, votesJsonParser);
		final BasicStatus status = statusJsonParser.parse(getFieldUnisex(s, STATUS_ATTR));

		final Collection<Version> fixVersions = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<Version>(versionJsonParser), FIELDS, FIX_VERSIONS_ATTR);
		final Collection<Version> affectedVersions = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<Version>(versionJsonParser), FIELDS, AFFECTS_VERSIONS_ATTR);
		final Collection<BasicComponent> components = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<BasicComponent>(basicComponentJsonParser), FIELDS, COMPONENTS_ATTR);

		final Collection<Worklog> worklogs = parseOptionalArray(shouldUseNestedValueAttribute, s, new JsonWeakParserForJsonObject<Worklog>(worklogJsonParser), FIELDS, WORKLOG_ATTR);

		final BasicWatchers watchers = getOptionalField(shouldUseNestedValueAttribute, s, WATCHER_ATTR, watchersJsonParser);
		final TimeTracking timeTracking = getOptionalField(shouldUseNestedValueAttribute, s, TIMETRACKING_ATTR, new TimeTrackingJsonParser());

		return new Issue(summary, JsonParseUtil.getSelfUri(s), s.getString("key"), project, issueType, status, description, priority,
				resolution, attachments, reporter, assignee, creationDate, updateDate, affectedVersions, fixVersions,
				components, timeTracking, fields, comments, transitionsUri, issueLinks, votes, worklogs, watchers, expandos
		);
	}

	@Nullable
	private <T> T getOptionalField(boolean shouldUseNestedValue, JSONObject s, final String fieldId, JsonParser<T> jsonParser) throws JSONException {
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
//		final JSONObject schemaJson = json.getJSONObject("schema");
		@SuppressWarnings("unchecked")
		final Iterator<String> it = json.keys();
		while (it.hasNext()) {
			final String fieldId = it.next();
			JSONObject fieldDefinition = json.getJSONObject(fieldId);
			res.put(fieldId, fieldDefinition.getString("type"));

		}
		return res;
	}

	private Map<String, String> parseNames(JSONObject json) throws JSONException {
		final HashMap<String, String> res = Maps.newHashMap();
		for (Iterator<String> it = json.keys(); it.hasNext(); ) {
			final String key = it.next();
			res.put(key, json.getString(key));
		}
		return res;
	}


	private Collection<Field> parseFields(JSONObject json) throws JSONException {
		ArrayList<Field> res = new ArrayList<Field>(json.length());
		@SuppressWarnings("unchecked")
		final Iterator<String> iterator = json.keys();
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
