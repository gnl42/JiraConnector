package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.AddressableEntity;
import org.joda.time.DateTime;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Comment implements AddressableEntity {
	private User author;
	private User updateAuthor;
	private DateTime creationDate;
	private DateTime updateDate;
	private String body;
	private URI self;
	private String renderer;
	
	public boolean wasUpdated() {
		return false;
	}
	public String getBody() {
		return body;
	}

	public URI getSelf() {
		return self;
	}
}
