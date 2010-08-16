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

package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.ExpandableProperty;
import com.atlassian.jira.restjavaclient.IssueArgs;
import com.atlassian.jira.restjavaclient.domain.Attachment;
import com.atlassian.jira.restjavaclient.domain.BasicStatus;
import com.atlassian.jira.restjavaclient.domain.Comment;
import com.atlassian.jira.restjavaclient.domain.Field;
import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.IssueLink;
import com.atlassian.jira.restjavaclient.domain.IssueType;
import com.atlassian.jira.restjavaclient.domain.Project;
import com.atlassian.jira.restjavaclient.domain.User;
import com.atlassian.jira.restjavaclient.domain.Version;
import com.atlassian.jira.restjavaclient.domain.Votes;
import com.atlassian.jira.restjavaclient.domain.Watchers;
import com.atlassian.jira.restjavaclient.domain.Worklog;
import com.google.common.base.Splitter;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.atlassian.jira.restjavaclient.json.JsonParseUtil.getNestedObject;
import static com.atlassian.jira.restjavaclient.json.JsonParseUtil.getNestedString;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class IssueJsonParser {
	private static final String THUMBNAIL = "thumbnail";
	private static final String UPDATED_ATTR = "updated";
	private static final String CREATED_ATTR = "created";
	private static final String AFFECTS_VERSIONS_ATTR = "versions";
	private static final String FIX_VERSIONS_ATTR = "fixVersions";

	private static Set<String> SPECIAL_FIELDS = new HashSet<String>(Arrays.asList("summary", UPDATED_ATTR, CREATED_ATTR,
			AFFECTS_VERSIONS_ATTR, FIX_VERSIONS_ATTR));

	private final IssueLinkJsonParser issueLinkJsonParser = new IssueLinkJsonParser();
	private final VotesJsonParser votesJsonParser = new VotesJsonParser();
	private final StatusJsonParser statusJsonParser = new StatusJsonParser();
	private final WorklogJsonParser worklogJsonParser = new WorklogJsonParser();
	private final WatchersJsonParser watchersJsonParser = new WatchersJsonParser();
	private final VersionJsonParser versionJsonParser = new VersionJsonParser();


	static Iterable<String> parseExpandos(JSONObject json) throws JSONException {
		final String expando = json.getString("expand");
		return Splitter.on(',').split(expando);
	}

	
	private Collection<IssueLink> parseIssueLinks(JSONArray jsonArray) throws JSONException {
		final Collection<IssueLink> issueLinks = new ArrayList<IssueLink>(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			issueLinks.add(issueLinkJsonParser.parseIssueLink(jsonArray.getJSONObject(i)));
		}
		return issueLinks;
	}
	
	public Issue parseIssue(IssueArgs args, JSONObject s) throws JSONException {
		final ExpandableProperty<Comment> expandableComment = JsonParseUtil.parseExpandableProperty(s.getJSONObject("comments"),
				new CommentExpandablePropertyBuilder(args));

		final ExpandableProperty<Attachment> attachments = JsonParseUtil.parseExpandableProperty(s.getJSONObject("attachments"),
				new JsonParseUtil.ExpandablePropertyBuilder<Attachment>() {
			public Attachment parse(JSONObject json) throws JSONException {
				return parseAttachment(json);
			}
		});
		final Iterable<String> expandos = parseExpandos(s);
		final Collection<Field> fields = parseFields(s.getJSONObject("fields"));
		final IssueType issueType = parseIssueType(getNestedObject(s, "fields", "issuetype"));
		final DateTime creationDate = JsonParseUtil.parseDateTime(getNestedString(s, "fields", "created"));
		final DateTime updateDate = JsonParseUtil.parseDateTime(getNestedString(s, "fields", "updated"));
		final URI transitionsUri = JsonParseUtil.parseURI(s.getString("transitions"));
		final Project project = parseProject(getNestedObject(s, "fields", "project"));
		final JSONArray linksJsonArray = s.optJSONArray("links");
		final Collection<IssueLink> issueLinks = linksJsonArray != null ? parseIssueLinks(linksJsonArray) : null;
		final Votes votes = votesJsonParser.parseVotes(getNestedObject(s, "fields", "votes"));
		final BasicStatus status = statusJsonParser.parseBasicStatus(getNestedObject(s, "fields", "status"));
		final JSONArray fixVersionsJsonArray = JsonParseUtil.getNestedArray(s, "fields", FIX_VERSIONS_ATTR);
		final Collection<Version> fixVersions = fixVersionsJsonArray != null ? parseVersions(fixVersionsJsonArray) : null;

		final JSONArray affectedVersionsJsonArray = JsonParseUtil.getNestedArray(s, "fields", AFFECTS_VERSIONS_ATTR);
		final Collection<Version> affectedVersions = affectedVersionsJsonArray != null ? parseVersions(affectedVersionsJsonArray) : null;

		final ExpandableProperty<Worklog> worklogs = JsonParseUtil.parseExpandableProperty(s.getJSONObject("worklogs"),
				new JsonParseUtil.ExpandablePropertyBuilder<Worklog>() {
			public Worklog parse(JSONObject json) throws JSONException {
				return worklogJsonParser.parseWorklog(json);
			}
		});

		final Watchers watchers = watchersJsonParser.parseWatchers(s.getJSONObject("watchers"));

		return new Issue(JsonParseUtil.getSelfUri(s), s.getString("key"), project, issueType, status, expandos, expandableComment,
				attachments, fields, creationDate, updateDate, transitionsUri, issueLinks, votes, worklogs, watchers, affectedVersions, fixVersions);
	}

	private <T> Collection<T> parseJsonArray(JSONArray jsonArray, JsonParser<T> jsonParser) throws JSONException {
		final Collection<T> res = new ArrayList<T>(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			res.add(jsonParser.parse(jsonArray.getJSONObject(i)));
		}
		return res;
	}


	private Collection<Version> parseVersions(JSONArray jsonArray) throws JSONException {
		return parseJsonArray(jsonArray, versionJsonParser);
	}

	private static Comment parseComment(JSONObject json, @Nullable String renderer) throws JSONException {
		final URI selfUri = JsonParseUtil.getSelfUri(json);
		final String body = json.getString("body");
		final User author = JsonParseUtil.parseUser(json.getJSONObject("author"));
		final User updateAuthor = JsonParseUtil.parseUser(json.getJSONObject("updateAuthor"));
		return new Comment(selfUri, body, author, updateAuthor, JsonParseUtil.parseDateTime(json.getString("created")),
				JsonParseUtil.parseDateTime(json.getString("updated")), renderer);
	}

	private Attachment parseAttachment(JSONObject json) throws JSONException {
		final URI selfUri = JsonParseUtil.getSelfUri(json);
		final String filename = json.getString("filename");
		final User author = JsonParseUtil.parseUser(json.getJSONObject("author"));
		final DateTime creationDate = JsonParseUtil.parseDateTime(json.getString("created"));
		final int size = json.getInt("size");
		final String mimeType = json.getString("mimeType");
		final URI contentURI = JsonParseUtil.parseURI(json.getString("content"));
		final URI thumbnailURI = json.has(THUMBNAIL) ? JsonParseUtil.parseURI(THUMBNAIL) : null;
		return new Attachment(selfUri, filename, author, creationDate, size, mimeType, contentURI, thumbnailURI);
	}

	IssueType parseIssueType(JSONObject json) throws JSONException {
		final URI selfUri = JsonParseUtil.getSelfUri(json);
		final String name = json.getString("name");
		final boolean isSubtask = json.getBoolean("subtask");
		return new IssueType(selfUri, name, isSubtask);
	}

	Project parseProject(JSONObject json) throws JSONException {
		final URI selfUri = JsonParseUtil.getSelfUri(json);
		final String key = json.getString("key");
		return new Project(selfUri, key);
	}

	static Collection<Field> parseFields(JSONObject json) throws JSONException {
		ArrayList<Field> res = new ArrayList<Field>(json.length());
		for (Iterator<String> it = json.keys(); it.hasNext();) {
			final String key = it.next();
			if (SPECIAL_FIELDS.contains(key)) {
				continue;
			}
			final Object value = json.get(key);
			if (value instanceof JSONObject) {

			} else {
				res.add(new Field(key, value != JSONObject.NULL ? value.toString() : null));
			}
		}
		return res;
	}


	private static class CommentExpandablePropertyBuilder implements JsonParseUtil.ExpandablePropertyBuilder<Comment> {
		private final IssueArgs args;

		public CommentExpandablePropertyBuilder(IssueArgs args) {
			this.args = args;
		}

		public Comment parse(JSONObject json) throws JSONException {
			return parseComment(json, args.getRenderer());
		}
	}


	
}
