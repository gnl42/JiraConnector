package com.atlassian.jira.restjavaclient.json;

import com.atlassian.jira.restjavaclient.ExpandableProperty;
import com.atlassian.jira.restjavaclient.IssueArgs;
import com.atlassian.jira.restjavaclient.domain.Attachment;
import com.atlassian.jira.restjavaclient.domain.Comment;
import com.atlassian.jira.restjavaclient.domain.Field;
import com.atlassian.jira.restjavaclient.domain.Issue;
import com.atlassian.jira.restjavaclient.domain.IssueLink;
import com.atlassian.jira.restjavaclient.domain.IssueType;
import com.atlassian.jira.restjavaclient.domain.Project;
import com.atlassian.jira.restjavaclient.domain.User;
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

	private static Set<String> SPECIAL_FIELDS = new HashSet<String>(Arrays.asList("summary", UPDATED_ATTR, CREATED_ATTR));

	private final IssueLinkJsonParser issueLinkJsonParser = new IssueLinkJsonParser();


	interface ExpandablePropertyBuilder<T> {
		T parse(JSONObject json) throws JSONException;
	}

	static Iterable<String> parseExpandos(JSONObject json) throws JSONException {
		final String expando = json.getString("expand");
		return Splitter.on(',').split(expando);
	}

	
	private <T> ExpandableProperty<T> parseExpandableProperty(JSONObject json, ExpandablePropertyBuilder<T> expandablePropertyBuilder)
			throws JSONException {
		final int numItems = json.getInt("size");
		final Collection<T> items;
		JSONArray itemsJa = json.getJSONArray("items");

		if (itemsJa.length() > 0) {
			items = new ArrayList<T>(numItems);
			for (int i = 0; i < itemsJa.length(); i++) {
				final T item = expandablePropertyBuilder.parse(itemsJa.getJSONObject(i));
				items.add(item);
			}
		} else {
			items = null;
		}

		return new ExpandableProperty<T>(numItems, items);
	}

	private Collection<IssueLink> parseIssueLinks(JSONArray jsonArray) throws JSONException {
		final Collection<IssueLink> issueLinks = new ArrayList<IssueLink>(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			issueLinks.add(issueLinkJsonParser.parseIssueLink(jsonArray.getJSONObject(i)));
		}
		return issueLinks;
	}
	
	public Issue parseIssue(IssueArgs args, JSONObject s) throws JSONException {
		final ExpandableProperty<Comment> expandableComment = parseExpandableProperty(s.getJSONObject("comments"),
				new CommentExpandablePropertyBuilder(args));

		final ExpandableProperty<Attachment> attachments = parseExpandableProperty(s.getJSONObject("attachments"), new ExpandablePropertyBuilder<Attachment>() {
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
		Collection<IssueLink> issueLinks = linksJsonArray != null ? parseIssueLinks(linksJsonArray) : null;

		return new Issue(JsonParseUtil.getSelfUri(s), s.getString("key"), project, issueType, expandos, expandableComment, attachments, fields, creationDate, updateDate, transitionsUri, issueLinks);
	}

	private static Comment parseComment(JSONObject json, @Nullable String renderer) throws JSONException {
		final URI selfUri = JsonParseUtil.getSelfUri(json);
		final String body = json.getString("body");
		final User author = JsonParseUtil.parseAuthor(json.getJSONObject("author"));
		final User updateAuthor = JsonParseUtil.parseAuthor(json.getJSONObject("updateAuthor"));
		return new Comment(selfUri, body, author, updateAuthor, JsonParseUtil.parseDateTime(json.getString("created")),
				JsonParseUtil.parseDateTime(json.getString("updated")), renderer);
	}

	private Attachment parseAttachment(JSONObject json) throws JSONException {
		final URI selfUri = JsonParseUtil.getSelfUri(json);
		final String filename = json.getString("filename");
		final User author = JsonParseUtil.parseAuthor(json.getJSONObject("author"));
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


	private static class CommentExpandablePropertyBuilder implements ExpandablePropertyBuilder<Comment> {
		private final IssueArgs args;

		public CommentExpandablePropertyBuilder(IssueArgs args) {
			this.args = args;
		}

		public Comment parse(JSONObject json) throws JSONException {
			return parseComment(json, args.getRenderer());
		}
	}

	
}
