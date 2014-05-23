package com.atlassian.jira.rest.client.api.domain;

import com.google.common.base.Objects;

import javax.annotation.Nullable;
import java.net.URI;

public class OperationLink {
	private final String id;
	private final String styleClass;
	private final String label;
	private final String title;
	private final URI href;
	private final Integer weight;
	@Nullable private final String iconClass;

	public OperationLink(final String id, final String styleClass, final String label, final String title,
			final java.net.URI href, final Integer weight, @Nullable final String iconClass) {
		this.id = id;
		this.styleClass = styleClass;
		this.iconClass = iconClass;
		this.label = label;
		this.title = title;
		this.href = href;
		this.weight = weight;
	}

	public String getId() {
		return id;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public String getLabel() {
		return label;
	}

	public String getTitle() {
		return title;
	}

	public URI getHref() {
		return href;
	}

	public Integer getWeight() {
		return weight;
	}

	@Nullable
	public String getIconClass() {
		return iconClass;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof OperationLink) {
			OperationLink that = (OperationLink) o;
			return Objects.equal(id, that.id)
					&& Objects.equal(styleClass, that.styleClass)
					&& Objects.equal(label, that.label)
					&& Objects.equal(title, that.title)
					&& Objects.equal(href, that.href)
					&& Objects.equal(weight, that.weight)
					&& Objects.equal(iconClass, that.iconClass);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, styleClass, label, title, href, weight, iconClass);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", id)
				.add("styleClass", styleClass)
				.add("label", label)
				.add("title", title)
				.add("href", href)
				.add("weight", weight)
				.add("iconClass", iconClass)
				.toString();
	}
}
