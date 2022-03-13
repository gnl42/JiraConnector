/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.commons.util;

import static com.atlassian.theplugin.commons.util.MiscUtil.isModified;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;

import java.util.Collection;

public class MiscUtilTest extends TestCase {
    public void testIsModified() {
        assertFalse(isModified(null, null));
        assertTrue(isModified("abc", null));
        assertTrue(isModified(null, "abc"));
        assertFalse(isModified("abc", "abc"));
		// intentionally using new Integer here to check different instances equals method
        assertFalse(isModified(new Integer(1), new Integer(1)));
        assertTrue(isModified(new Integer(1), new Integer(2)));
    }

	public void testBuildArrayList() {
		final Object o1 = new Object();
		final Object o2 = new Object();
		TestUtil.assertHasOnlyElements(MiscUtil.buildArrayList(o1, o2), o2, o1);
		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				MiscUtil.buildArrayList((Collection<Object>) null);
			}
		});
	}
}
