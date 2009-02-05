/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.bamboo.core.client.model;

import java.io.Serializable;

/**
 * Cached Bamboo build plan
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooCachedPlan implements Serializable {

	private static final char SEPARATOR = '-';

	private static final long serialVersionUID = 4962438242498880319L;

	private final String name;

	private final String key;

	private final boolean favourite;

	private final boolean enabled;

	private boolean subscribed;

	public BambooCachedPlan(String name, String key, boolean favourite, boolean enabled, boolean subscribed) {
		super();
		this.name = name;
		this.key = key;
		this.favourite = favourite;
		this.enabled = enabled;
		this.subscribed = subscribed;
	}

	public BambooCachedPlan(String name, String key, boolean favourite, boolean enabled) {
		super();
		this.name = name;
		this.key = key;
		this.favourite = favourite;
		this.enabled = enabled;
		this.subscribed = false;
	}

	public boolean isSubscribed() {
		return subscribed;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}

	public boolean isFavourite() {
		return favourite;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}

	public String getProjectKeyFromPlanKey() {
		return key.substring(0, key.indexOf(SEPARATOR));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	/**
	 * Equal if the key is equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BambooCachedPlan other = (BambooCachedPlan) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		return true;
	}

}
