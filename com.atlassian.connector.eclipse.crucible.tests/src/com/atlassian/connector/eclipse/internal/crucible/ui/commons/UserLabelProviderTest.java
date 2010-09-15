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

import com.atlassian.theplugin.commons.crucible.api.model.User;

import junit.framework.TestCase;

/**
 * @author Shawn Minto
 */
public class UserLabelProviderTest extends TestCase {

	public void testUserLabelProvider() {
		CrucibleUserLabelProvider labelProvider = new CrucibleUserLabelProvider();

		String userName1 = "username";
		String userName2 = "username2";
		String displayName1 = "displayName";
		User user1 = new User(userName1, displayName1);
		User user2 = new User(userName2);

		assertNull(labelProvider.getImage(user1));

		assertEquals(displayName1, labelProvider.getText(user1));

		assertEquals(userName2, labelProvider.getText(user2));

		assertNull(labelProvider.getImage(user1));

		assertEquals(displayName1, labelProvider.getText(user1));

		assertEquals(userName2, labelProvider.getText(user2));
	}

}
