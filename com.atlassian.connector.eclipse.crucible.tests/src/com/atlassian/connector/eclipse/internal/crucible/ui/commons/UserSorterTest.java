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

package com.atlassian.connector.eclipse.internal.crucible.ui.commons;

import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedUser;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;

import junit.framework.TestCase;

/**
 * @author Shawn Minto
 */
public class UserSorterTest extends TestCase {

	public void testSorter() {
		CrucibleUserSorter sorter = new CrucibleUserSorter();

		String userName1 = "username";
		String userName2 = "username2";
		String displayName1 = "displayName";
		User user1 = new UserBean(userName1, displayName1);
		User user2 = new UserBean(userName2);

		assertEquals(0, sorter.compare(null, user1, user1));

		assertTrue(0 > sorter.compare(null, user1, user2));
		assertTrue(0 < sorter.compare(null, user2, user1));

		CrucibleCachedUser cachedUser1 = new CrucibleCachedUser(user1);
		CrucibleCachedUser cachedUser2 = new CrucibleCachedUser(user2);

		assertTrue(0 > sorter.compare(null, cachedUser1, cachedUser2));
		assertTrue(0 < sorter.compare(null, cachedUser2, cachedUser1));
	}

}
