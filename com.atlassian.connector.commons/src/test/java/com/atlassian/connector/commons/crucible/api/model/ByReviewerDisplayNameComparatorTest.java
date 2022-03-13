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

package com.atlassian.connector.commons.crucible.api.model;

import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import junit.framework.TestCase;

public class ByReviewerDisplayNameComparatorTest extends TestCase {
	private final ByReviewerDisplayNameComparator comparator = new ByReviewerDisplayNameComparator();
	private final Reviewer r1 = new Reviewer("wseliga", "Wojciech Seliga", true);
	private final Reviewer r5 = new Reviewer("wseliga", "Seliga, Wojciech", true);
	private final Reviewer r2 = new Reviewer("wseliga", null, true);
	private final Reviewer r3 = new Reviewer("pniewiadomski", "Pawel Niewiadomski", false);
	private final Reviewer r6 = new Reviewer("pniewiadomski", null, false);

	public void testCompare() {
		assertEquals(0, comparator.compare(r1, r1));
		assertEquals(0, comparator.compare(r2, r2));
		assertTrue(comparator.compare(r1, r5) > 0);
		assertTrue(comparator.compare(r5, r1) < 0);
		assertTrue(comparator.compare(r2, r1) > 0);
		assertTrue(comparator.compare(r3, r1) < 0);
		assertTrue(comparator.compare(r2, r6) > 0);
	}

}
