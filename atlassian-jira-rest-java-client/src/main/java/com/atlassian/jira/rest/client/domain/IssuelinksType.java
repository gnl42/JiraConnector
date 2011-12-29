package com.atlassian.jira.rest.client.domain;

import com.google.common.base.Objects;

/**
 *
 * @since v5.1
 */
public class IssuelinksType {
	private final String name;
	private final String id;
	private final String inward;
	private final String outward;

	public IssuelinksType(String id, String name, String inward, String outward) {
		this.id = id;
		this.name = name;
		this.inward = inward;
		this.outward = outward;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public String getInward() {
		return inward;
	}

	public String getOutward() {
		return outward;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("id", id).
				add("name", name).
				add("inward", inward).
				add("outward", outward).
				toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IssueLinkType) {
			IssuelinksType that = (IssuelinksType) obj;
			return Objects.equal(this.id, that.id)
					&& Objects.equal(this.name, that.name)
					&& Objects.equal(this.inward, that.inward)
					&& Objects.equal(this.outward, that.outward);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, name, inward, outward);
	}

}
