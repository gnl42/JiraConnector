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

import java.util.Date;

import junit.framework.TestCase;

public class BambooBuildViewerComparatorTest extends TestCase {

	public void testIsSorterPropertyObjectString() {
		assertTrue(new BambooBuildViewerComparator().isSorterProperty(new Object(), ""));
		assertTrue(new BambooBuildViewerComparator().isSorterProperty(null, null));
	}

	public void testCompareViewerObjectObject() {
		//create builds with the minimal set of elements
		BambooBuild build1 = new BambooBuildInfo(null, "plan1", null, new Date(), null, false, 1, BuildStatus.SUCCESS,
				null, new Date(), null, null, 1, 1, new Date(), null, null, null, null);
		BambooBuild build2 = new BambooBuildInfo(null, "plan2", null, new Date(), null, false, 1, BuildStatus.SUCCESS,
				null, new Date(), null, null, 1, 1, new Date(), null, null, null, null);
		BambooBuild build3 = new BambooBuildInfo(null, "plan2", null, new Date(), null, false, 1, BuildStatus.SUCCESS,
				null, new Date(), null, null, 1, 1, new Date(), null, null, null, null);
		BambooBuild build4 = new BambooBuildInfo(null, "plan1", null, new Date(), null, false, 1, BuildStatus.FAILURE,
				null, new Date(), null, null, 1, 1, new Date(), null, null, null, null);
		BambooBuild build5 = new BambooBuildInfo(null, "plan2", null, new Date(), null, false, 1, BuildStatus.FAILURE,
				null, new Date(), null, null, 1, 1, new Date(), null, null, null, null);
		BambooBuild build6 = new BambooBuildInfo(null, "plan2", null, new Date(), null, false, 1, BuildStatus.FAILURE,
				null, new Date(), null, null, 1, 1, new Date(), null, null, null, null);
		BambooBuild build7 = new BambooBuildInfo(null, "plan1", null, new Date(), null, false, 1, BuildStatus.UNKNOWN,
				null, new Date(), null, null, 1, 1, new Date(), null, null, null, null);
		BambooBuild build8 = new BambooBuildInfo(null, "plan2", null, new Date(), null, false, 1, BuildStatus.UNKNOWN,
				null, new Date(), null, null, 1, 1, new Date(), null, null, null, null);

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
