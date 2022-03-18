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
package com.atlassian.connector.commons.jira.beans;

import java.util.HashMap;

/**
 * @author Jacek Jaroczynski
 */
public class JIRASecurityLevelBean extends AbstractJIRAConstantBean {

	public JIRASecurityLevelBean() {
	}

	public JIRASecurityLevelBean(final Long id, final String name) {
		super(id, name, null);
	}

	public JIRASecurityLevelBean(final JIRASecurityLevelBean other) {
		this(other.getMap());
	}

	public JIRASecurityLevelBean(final HashMap<String, String> map) {
		super(map);
	}

	public HashMap<String, String> getMap() {
		return super.getMap();
//		map.put("value", getValue());
	}

	public String getQueryStringFragment() {
		// todo create query string fragment
		return null;
	}

	public JIRAQueryFragment getClone() {
		return new JIRASecurityLevelBean(this);
	}    
}
