/*******************************************************************************
 * Copyright (c) 2012, 2024 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *     Tasktop Technologies - initial API and implementation
 *     ArSysOp - ongoing support
 *******************************************************************************/

package org.eclipse.mylyn.commons.sdk.util;

import java.util.List;
import java.util.Map;

/**
 * @author Steffen Pingel
 */
@SuppressWarnings("nls")
public class FixtureConfiguration {

	String type;

	String url;

	String version;

	String info;

	Map<String, String> properties;

	List<String> tags;

	public FixtureConfiguration() {
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(final String info) {
		this.info = info;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public List<String> getTags() {
		return tags;
	}

	public boolean isDefault() {
		return properties != null
				&& ("1".equals(properties.get("default")) || "true".equals(properties.get("default")));
	}

}
