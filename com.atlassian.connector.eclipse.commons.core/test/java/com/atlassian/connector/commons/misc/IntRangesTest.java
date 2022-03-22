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
package com.atlassian.connector.commons.misc;

import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;

import java.util.Collections;

public class IntRangesTest extends TestCase {
	public static final IntRange IR_123 = new IntRange(123);
	public static final IntRange IR_M123 = new IntRange(-123);
	public static final IntRange IR_M123_M1 = new IntRange(-123, -1);
	public static final IntRange IR_M25_56 = new IntRange(-25, 56);
	public static final IntRange IR_3_89 = new IntRange(3, 89);
	public static final IntRange IR_1_2 = new IntRange(1, 2);

	public void testRanges() {
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Throwable {
				new IntRanges(Collections.<IntRange>emptyList());
			}
		});
		assertEquals(-123, new IntRanges(IR_M123).getTotalMin());
		assertEquals(-123, new IntRanges(IR_M123).getTotalMax());
		assertEquals(-123, new IntRanges(IR_M123, IR_3_89, IR_123).getTotalMin());
		assertEquals(123, new IntRanges(IR_M123, IR_3_89, IR_123).getTotalMax());
	}

	public void testEquals() {
		TestUtil.assertEqualsSymmetrical(new IntRanges(IR_M123), new IntRanges(IR_M123));
		TestUtil.assertNotEquals(new IntRanges(IR_M123), new IntRanges(IR_123));
		TestUtil.assertEqualsSymmetrical(new IntRanges(IR_M123, IR_1_2), new IntRanges(IR_1_2, IR_M123));
	}

	public void testToNiceString() {
		assertEquals("123", IR_123.toNiceString());
		assertEquals("3 - 89", IR_3_89.toNiceString());
		assertEquals("-123, 3 - 89, 123", new IntRanges(IR_M123, IR_3_89, IR_123).toNiceString());
	}
}
