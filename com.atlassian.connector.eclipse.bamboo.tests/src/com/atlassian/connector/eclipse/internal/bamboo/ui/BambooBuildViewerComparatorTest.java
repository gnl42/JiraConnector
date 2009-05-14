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

package com.atlassian.connector.eclipse.internal.bamboo.ui;

import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooBuildViewerComparator.SortOrder;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.remoteapi.ServerData;

import java.util.Date;

import junit.framework.TestCase;

public class BambooBuildViewerComparatorTest extends TestCase {

	public void testIsSorterPropertyObjectString() {
		assertTrue(new BambooBuildViewerComparator().isSorterProperty(new Object(), ""));
		assertTrue(new BambooBuildViewerComparator().isSorterProperty(null, null));
	}

	private BambooBuild createBambooBuild(String planName, ServerData serverData, BuildStatus buildStatus) {
		return new BambooBuildInfo.Builder("pkey", planName, serverData, null, 1, buildStatus).pollingTime(new Date())
				.build();

	}

	public void testCompareViewerObjectObject() {
		//create builds with the minimal set of elements
		ServerData sd = new ServerData("myserver", "myid", "user", "pass", "http://atlassian.com");
		BambooBuild build1 = createBambooBuild("plan1", sd, BuildStatus.SUCCESS);
		BambooBuild build2 = createBambooBuild("plan2", sd, BuildStatus.SUCCESS);
		BambooBuild build3 = createBambooBuild("plan2", sd, BuildStatus.SUCCESS);
		BambooBuild build4 = createBambooBuild("plan1", sd, BuildStatus.FAILURE);
		BambooBuild build5 = createBambooBuild("plan2", sd, BuildStatus.FAILURE);
		BambooBuild build6 = createBambooBuild("plan2", sd, BuildStatus.FAILURE);
		BambooBuild build7 = createBambooBuild("plan1", sd, BuildStatus.UNKNOWN);
		BambooBuild build8 = createBambooBuild("plan2", sd, BuildStatus.UNKNOWN);

		BambooBuildViewerComparator comparator = new BambooBuildViewerComparator();
		while (comparator.getSortOrder() != SortOrder.STATE_PASSED_FAILED) {
			comparator.toggleSortOrder();
		}
		//sorting of successful builds
		assertTrue(-1 == comparator.compare(null, build1, build2));
		assertTrue(1 == comparator.compare(null, build2, build1));
		assertTrue(0 == comparator.compare(null, build2, build3));
		//sorting of mixed state builds (successful always ahead
		assertTrue(-1 == comparator.compare(null, build1, build4));
		assertTrue(-1 == comparator.compare(null, build2, build4));
		assertTrue(-1 == comparator.compare(null, build1, build5));
		assertTrue(-1 == comparator.compare(null, build1, build7));
		assertTrue(-1 == comparator.compare(null, build2, build7));
		assertTrue(-1 == comparator.compare(null, build1, build8));

		assertTrue(0 == comparator.compare(null, build1, null));
		assertTrue(0 == comparator.compare(null, null, build1));
		assertTrue(0 == comparator.compare(null, null, null));

		while (comparator.getSortOrder() != SortOrder.STATE_FAILED_PASSED) {
			comparator.toggleSortOrder();
		}
		//sorting of failed builds
		assertTrue(-1 == comparator.compare(null, build4, build5));
		assertTrue(1 == comparator.compare(null, build5, build4));
		assertTrue(0 == comparator.compare(null, build5, build6));
		//sorting of mixed state builds (successful always ahead
		assertTrue(-1 == comparator.compare(null, build4, build1));
		assertTrue(-1 == comparator.compare(null, build4, build2));
		assertTrue(-1 == comparator.compare(null, build5, build1));
		assertTrue(-1 == comparator.compare(null, build4, build7));
		assertTrue(-1 == comparator.compare(null, build5, build7));
		assertTrue(-1 == comparator.compare(null, build4, build8));

		while (comparator.getSortOrder() != SortOrder.UNSORTED) {
			comparator.toggleSortOrder();
		}
		//sorting of failed builds
		assertTrue(-1 == comparator.compare(null, build4, build5));
		assertTrue(1 == comparator.compare(null, build5, build4));
		assertTrue(0 == comparator.compare(null, build5, build6));
		//sorting of mixed state builds (successful always ahead
		assertTrue(0 == comparator.compare(null, build4, build1));
		assertTrue(-1 == comparator.compare(null, build4, build2));
		assertTrue(1 == comparator.compare(null, build5, build1));
		assertTrue(0 == comparator.compare(null, build4, build7));
		assertTrue(1 == comparator.compare(null, build5, build7));
		assertTrue(-1 == comparator.compare(null, build4, build8));
	}
}
