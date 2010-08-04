package com.atlassian.jira.restjavaclient.domain;

import com.atlassian.jira.restjavaclient.AddressableEntity;
import com.google.common.base.Objects;
import org.joda.time.DateTime;

import java.net.URI;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Comment implements AddressableEntity {
	private final User author;
	private final User updateAuthor;
	private final DateTime creationDate;
	private final DateTime updateDate;
	private final String body;
	private final URI self;

    public Comment(URI self, String body, User author, User updateAuthor, DateTime creationDate, DateTime updateDate, String renderer) {
        this.author = author;
        this.updateAuthor = updateAuthor;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.body = body;
        this.self = self;
        this.renderer = renderer;
    }

    private final String renderer;
	
	public boolean wasUpdated() {
		return false;
	}
	public String getBody() {
		return body;
	}

	public URI getSelf() {
		return self;
	}

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("self", self)
                .add("body", body)
                .add("author", author)
                .add("updateAuthor", updateAuthor)
                .add("creationDate", creationDate)
                .add("updateDate", updateDate).toString();
    }
}
