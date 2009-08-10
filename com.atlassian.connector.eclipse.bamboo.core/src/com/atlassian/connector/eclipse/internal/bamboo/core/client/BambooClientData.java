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

package com.atlassian.connector.eclipse.internal.bamboo.core.client;

import com.atlassian.theplugin.commons.bamboo.BambooPlan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Cached repository configuration for Bamboo server.
 * 
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 */
public class BambooClientData implements Serializable {

	private static final long serialVersionUID = 5078330984585994532L;

	private Collection<BambooPlan> cachedPlans;

	public BambooClientData() {
		cachedPlans = new ArrayList<BambooPlan>();
	}

	public synchronized boolean hasData() {
		return cachedPlans != null;
	}

	public synchronized Collection<BambooPlan> getPlans() {
		return cachedPlans;
	}

	public synchronized void setPlans(Collection<BambooPlan> plans) {
		this.cachedPlans = new ArrayList<BambooPlan>();
		this.cachedPlans.addAll(plans);
	}
}
