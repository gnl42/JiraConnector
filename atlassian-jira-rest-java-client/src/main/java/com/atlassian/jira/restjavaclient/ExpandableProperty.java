package com.atlassian.jira.restjavaclient;

import java.util.Collection;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class ExpandableProperty<T> {
    private final int size;

	public ExpandableProperty(int size) {
		this.size = size;
        items = null;
	}

    public ExpandableProperty(int size, Collection<T> items) {
        this.size = size;
        this.items = items;
    }

    public int getSize() {
        return size;
    }

    final private Collection<T> items;

    Iterable<T> getItems() {
        return items;
    }
}
