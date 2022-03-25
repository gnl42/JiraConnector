/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.glindholm.connector.commons.jira.beans;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

public class JIRASavedFilterBean implements JIRASavedFilter {
	private final String name;
    private String author;
    private String project;
	private final long id;
    private String jql;
    private URI searchUrl;
    private URI viewUrl;

    public JIRASavedFilterBean(Map<String, String> projMap) {
        name = projMap.get("name");
        author = projMap.get("author");
        project = projMap.get("project");
        id = Long.valueOf(projMap.get("id"));
    }

	public JIRASavedFilterBean(String n, long id) {
		name = n;
		this.id = id;
	}

    public JIRASavedFilterBean(String name, long id, String jql, URI searchUrl, URI viewUrl) {
        this.name = name;
        this.id = id;
        this.jql = jql;
        this.searchUrl = searchUrl;
        this.viewUrl = viewUrl;
    }

    public JIRASavedFilterBean(JIRASavedFilterBean other) {
        this.name = other.name;
        this.author = other.author;
        this.project = other.project;
        this.id = other.id;
        this.jql = other.jql;
        this.searchUrl = other.searchUrl;
        this.viewUrl = other.viewUrl;
    }

	@Override
    public String getName() {
        return name;
    }

	@Override
    public HashMap<String, String> getMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("name", getName());
		map.put("id", Long.toString(id));
		map.put("author", getAuthor());
		map.put("project", getProject());
		map.put("filterTypeClass", this.getClass().getName());
		return map;
	}

	@Override
    public JIRASavedFilterBean getClone() {
		return new JIRASavedFilterBean(this);
	}

	@Override
    public long getId() {
        return id;
    }

	@Override
    public String getAuthor() {
		return author;
	}

	@Override
    public String getProject() {
		return project;
	}

	@Override
    public String getQueryStringFragment() {
        return Long.toString(id);
    }

    @Override
    public List<JIRAQueryFragment> getQueryFragments() {
        return ImmutableList.of((JIRAQueryFragment) this);
    }

    @Override
    public String getOldStyleQueryString() {
        return getQueryStringFragment();
    }


    @Override
    public String getJql() {
        return jql;
    }

    @Override
    public URI getSearchUrl() {
        return searchUrl;
    }

    @Override
    public URI getViewUrl() {
        return viewUrl;
    }
}