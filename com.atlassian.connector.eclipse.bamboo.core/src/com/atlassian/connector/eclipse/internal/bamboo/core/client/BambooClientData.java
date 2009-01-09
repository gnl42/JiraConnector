/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
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
import java.util.Collections;
import java.util.List;

/**
 * Cached repository configuration for Bamboo server.
 * 
 * @author Shawn Minto
 */
public class BambooClientData implements Serializable {

	private static final long serialVersionUID = 5078330984585994532L;

	private List<BambooPlan> plans;

	public BambooClientData() {
	}

	public synchronized boolean hasData() {
		return plans != null;
	}

	public synchronized Collection<BambooPlan> getPlans() {
		return plans;
	}

	public synchronized void setPlans(Collection<BambooPlan> plans) {
		this.plans = Collections.unmodifiableList(new ArrayList<BambooPlan>(plans));
	}

}
