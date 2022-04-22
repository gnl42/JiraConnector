package com.atlassian.jira.rest.client.api.domain;

import java.util.Objects;

/**
 * Represents the structure used for paging.
 *
 * @since v5.2
 */
public class Page<T> {
    private final long startAt;
    private final int maxResults;
    private final long total;
    private final Iterable<T> values;
    private final boolean isLast;

    public Page(long startAt, int maxResults, long total, Iterable<T> values, boolean isLast) {
        this.startAt = startAt;
        this.maxResults = maxResults;
        this.total = total;
        this.values = values;
        this.isLast = isLast;
    }

    public long getStartAt() {
        return startAt;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public long getTotal() {
        return total;
    }

    public Iterable<T> getValues() {
        return values;
    }

    public boolean isLast() {
        return isLast;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page<?> page = (Page<?>) o;
        return startAt == page.startAt &&
                maxResults == page.maxResults &&
                total == page.total &&
                isLast == page.isLast &&
                Objects.equals(values, page.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startAt, maxResults, total, values, isLast);
    }

    @Override
    public String toString() {
        return "Page{" +
                "startAt=" + startAt +
                ", maxResults=" + maxResults +
                ", total=" + total +
                ", values=" + values +
                ", isLast=" + isLast +
                '}';
    }
}
