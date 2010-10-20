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

package com.atlassian.jira.rest.restjavaclient.internal.json;

import com.atlassian.jira.rest.restjavaclient.domain.*;
import com.google.common.base.Splitter;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.*;

import static com.atlassian.jira.rest.restjavaclient.internal.json.JsonParseUtil.getNestedObject;
import static com.atlassian.jira.rest.restjavaclient.internal.json.JsonParseUtil.getNestedString;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
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

    private static Set<String> SPECIAL_FIELDS = new HashSet<String>(Arrays.asList(SUMMARY_ATTR, UPDATED_ATTR, CREATED_ATTR,
			AFFECTS_VERSIONS_ATTR, FIX_VERSIONS_ATTR, COMPONENTS_ATTR, LINKS_ATTR, ISSUE_TYPE_ATTR, VOTES_ATTR,
            WORKLOG_ATTR, WATCHER_ATTR, PROJECT_ATTR, STATUS_ATTR, COMMENT_ATTR, ATTACHMENT_ATTR, SUMMARY_ATTR,
			PRIORITY_ATTR, RESOLUTION_ATTR, ASSIGNEE_ATTR, REPORTER_ATTR));

	private final IssueLinkJsonParser issueLinkJsonParser = new IssueLinkJsonParser();
	private final BasicVotesJsonParser votesJsonParser = new BasicVotesJsonParser();
	private final BasicStatusJsonParser statusJsonParser = new BasicStatusJsonParser();
	private final WorklogJsonParser worklogJsonParser = new WorklogJsonParser();
	private final JsonParserWithJsonObjectValue<BasicWatchers> watchersJsonParser
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

	
    private <T> Collection<T> parseArray(JSONObject jsonObject, JsonWeakParser<T> jsonParser) throws JSONException {
//        String type = jsonObject.getString("type");
//        final String name = jsonObject.getString("name");
        final JSONArray valueObject = jsonObject.optJSONArray(VALUE_ATTR);
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
    private <T> Collection<T> parseOptionalArray(JSONObject json, JsonWeakParser<T> jsonParser, String ... path) throws JSONException {
        final JSONObject js = JsonParseUtil.getNestedOptionalObject(json, path);
        if (js == null) {
            return null;
        }
        return parseArray(js, jsonParser);
    }

	@Override
	public Issue parse(JSONObject s) throws JSONException {
        final JSONObject commentsJs = JsonParseUtil.getNestedObject(s, FIELDS, COMMENT_ATTR);

        Collection<Comment> comments;
        if (commentsJs != null) {
            comments = parseArray(commentsJs, new JsonWeakParserForJsonObject<Comment>(commentJsonParser));
        } else {
            comments = new ArrayList<Comment>();
        }

        final String summary = JsonParseUtil.getNestedString(s, FIELDS, SUMMARY_ATTR, VALUE_ATTR);
		final Collection<Attachment> attachments = parseOptionalArray(s, new JsonWeakParserForJsonObject<Attachment>(attachmentJsonParser), FIELDS, ATTACHMENT_ATTR);
		final Iterable<String> expandos = parseExpandos(s);
		final Collection<Field> fields = parseFields(s.getJSONObject(FIELDS));
		final BasicIssueType issueType = issueTypeJsonParser.parse(getNestedObject(s, FIELDS, ISSUE_TYPE_ATTR, VALUE_ATTR));
		final DateTime creationDate = JsonParseUtil.parseDateTime(getNestedString(s, FIELDS, CREATED_ATTR, VALUE_ATTR));
		final DateTime updateDate = JsonParseUtil.parseDateTime(getNestedString(s, FIELDS, UPDATED_ATTR, VALUE_ATTR));

		final BasicPriority priority = getOptionalField(s, PRIORITY_ATTR, priorityJsonParser);
		final BasicResolution resolution = getOptionalField(s, RESOLUTION_ATTR, resolutionJsonParser);
		final BasicUser assignee = getOptionalField(s, ASSIGNEE_ATTR, userJsonParser);
		final BasicUser reporter = getOptionalField(s, REPORTER_ATTR, userJsonParser);

		final URI transitionsUri = JsonParseUtil.parseURI(s.getString("transitions"));
		final BasicProject project = projectJsonParser.parse(getNestedObject(s, FIELDS, PROJECT_ATTR));
        final Collection<IssueLink> issueLinks = parseOptionalArray(s, new JsonWeakParserForJsonObject<IssueLink>(issueLinkJsonParser), FIELDS, LINKS_ATTR);
		final BasicVotes votes = votesJsonParser.parse(getNestedObject(s, FIELDS, VOTES_ATTR, VALUE_ATTR));
		final BasicStatus status = statusJsonParser.parse(getNestedObject(s, FIELDS, STATUS_ATTR, VALUE_ATTR));

        final Collection<Version> fixVersions = parseOptionalArray(s, new JsonWeakParserForJsonObject<Version>(versionJsonParser), FIELDS, FIX_VERSIONS_ATTR);
        final Collection<Version> affectedVersions = parseOptionalArray(s, new JsonWeakParserForJsonObject<Version>(versionJsonParser), FIELDS, AFFECTS_VERSIONS_ATTR);
        final Collection<BasicComponent> components = parseOptionalArray(s, new JsonWeakParserForJsonObject<BasicComponent>(basicComponentJsonParser), FIELDS, COMPONENTS_ATTR);

        final Collection<Worklog> worklogs = parseOptionalArray(s, new JsonWeakParserForJsonObject<Worklog>(worklogJsonParser), FIELDS, WORKLOG_ATTR);

		final BasicWatchers watchers = watchersJsonParser.parse(getNestedObject(s, FIELDS, WATCHER_ATTR));

		return new Issue(summary, JsonParseUtil.getSelfUri(s), s.getString("key"), project, issueType, status, priority, 
				resolution, attachments, reporter, assignee, creationDate, updateDate, affectedVersions, fixVersions,
				components, fields, comments, transitionsUri, issueLinks, votes, worklogs, watchers, expandos
		);
	}

	private <T> T getOptionalField(JSONObject s, final String fieldId, JsonParser<T> jsonParser) throws JSONException {
		final JSONObject fieldJson = JsonParseUtil.getNestedOptionalObject(s, FIELDS, fieldId);
		return fieldJson != null ? jsonParser.parse(fieldJson.getJSONObject(VALUE_ATTR)) : null;
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
            try {
                res.add(fieldParser.parse(json.getJSONObject(key), key));
            } catch (JSONException e) {
                final JSONException jsonException = new JSONException("Cannot parse field [" + key + "]");
                jsonException.initCause(e);
                throw jsonException;
            }
		}
		return res;
	}

}
