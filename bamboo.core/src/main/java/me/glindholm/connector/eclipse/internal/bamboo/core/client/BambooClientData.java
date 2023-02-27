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

package me.glindholm.connector.eclipse.internal.bamboo.core.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import me.glindholm.theplugin.commons.bamboo.BambooPlan;

/**
 * Cached repository configuration for Bamboo server.
 *
 * @author Shawn Minto
 * @author Thomas Ehrnhoefer
 */
public class BambooClientData implements Serializable {
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BambooClientData [cachedPlans=").append(cachedPlans).append("]");
        return builder.toString();
    }

    private static final long serialVersionUID = -1230017044346413273L;

    private Collection<BambooPlan> cachedPlans;

    public synchronized boolean hasData() {
        return cachedPlans != null;
    }

    public synchronized Collection<BambooPlan> getPlans() {
        return cachedPlans;
    }

    public synchronized void setPlans(final Collection<BambooPlan> plans) {
        cachedPlans = new ArrayList<>(plans);
    }

}
