package com.atlassian.jira.rest.client.domain;

import com.atlassian.jira.rest.client.IdentifiableEntity;
import com.google.common.base.Objects;

import java.net.URI;

/**
 * User: kalamon
 * Date: 16.11.12
 * Time: 11:31
 */
public class FavouriteFilter extends AddressableNamedEntity implements IdentifiableEntity<Long> {
    private final Long id;
    private final String jql;
    private final URI viewUrl;
    private final URI searchUrl;

    public FavouriteFilter(URI self, String name, Long id, String jql, URI viewUrl, URI searchUrl) {
        super(self, name);
        this.id = id;
        this.jql = jql;
        this.viewUrl = viewUrl;
        this.searchUrl = searchUrl;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getJql() {
        return jql;
    }

    public URI getViewUrl() {
        return viewUrl;
    }

    public URI getSearchUrl() {
        return searchUrl;
    }

    @Override
    public String toString() {
        return getToStringHelper().toString();
    }

    protected Objects.ToStringHelper getToStringHelper() {
        return Objects.toStringHelper(this).
                add("self", self).
                add("name", name).
                add("id", id).
                add("jql", jql).
                add("searchUrl", searchUrl).
                add("viewUrl", viewUrl);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FavouriteFilter) {
            FavouriteFilter that = (FavouriteFilter) obj;
            return Objects.equal(this.self, that.self)
                    && Objects.equal(this.name, that.name)
                    && Objects.equal(this.id, that.id)
                    && Objects.equal(this.jql, that.jql)
                    && Objects.equal(this.searchUrl, that.searchUrl)
                    && Objects.equal(this.viewUrl, that.viewUrl);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(self, name, id, jql, searchUrl, viewUrl);
    }
}
