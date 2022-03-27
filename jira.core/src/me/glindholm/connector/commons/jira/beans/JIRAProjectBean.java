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

import java.util.HashMap;
import java.util.Map;

public class JIRAProjectBean implements JIRAProject {
	private String name;
	private String key;
	private String url;
	private long id;
	private String description;
	private String lead;

	public JIRAProjectBean() {
	}

	public JIRAProjectBean(Map<String, String> projMap) {
		name = projMap.get("name");
		key = projMap.get("key");
		description = projMap.get("description");
		url = projMap.get("url");
		lead = projMap.get("lead");
		id = Long.valueOf(projMap.get("id"));
	}

	public JIRAProjectBean(long id, String name) {
		this.id = id;
		this.name = name;
	}
    public JIRAProjectBean(long id, String key, String name) {
        this.id = id;
        this.key = key;
        this.name = name;
    }

    public JIRAProjectBean(JIRAProjectBean other) {
		this(other.getMap());
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
		map.put("key", getKey());
		map.put("description", getDescription());
		map.put("url", getUrl());
		map.put("lead", getLead());
		map.put("filterTypeClass", this.getClass().getName());
		return map;
	}

	@Override
    public JIRAProjectBean getClone() {
		return new JIRAProjectBean(this);
	}

	@Override
    public String getKey() {
		return key;
	}

	@Override
    public String getUrl() {
		return url;
	}

	@Override
    public long getId() {
		return id;
	}

	@Override
    public String getDescription() {
		return description;
	}

	@Override
    public String getLead() {
		return lead;
	}

	//@Transient
	@Override
    public String getQueryStringFragment() {
		return "pid=" + id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLead(String lead) {
		this.lead = lead;
	}
}
