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

import static com.atlassian.connector.commons.misc.IntRangesTest.*;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;

public class IntRangesParserTest extends TestCase {
	public void testParse() {
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Throwable {
				//noinspection ConstantConditions
				IntRangesParser.parse(null);
			}
		});

		TestUtil.assertHasOnlyElements(IntRangesParser.parse("123").getRanges(), IR_123);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("-123").getRanges(), IR_M123);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("-25-56").getRanges(), IR_M25_56);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("3-89").getRanges(), IR_3_89);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("1-2").getRanges(), IR_1_2);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("-123, 1-2, 3-89, 123").getRanges(),
				IR_M123, IR_1_2, IR_3_89, IR_123);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("-123,1-2,  3 - 89, 123  ").getRanges(),
				IR_M123, IR_1_2, IR_3_89, IR_123);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("-123, 123").getRanges(),
				IR_M123, IR_123);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("1-2, 3-89").getRanges(), 
				IR_1_2, IR_3_89);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("-25 -56  ").getRanges(), IR_M25_56);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("-25 -56,  ").getRanges(), IR_M25_56);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("-25 -56,").getRanges(), IR_M25_56);
		TestUtil.assertHasOnlyElements(IntRangesParser.parse("-123--1").getRanges(), IR_M123_M1);
	}

	private static final String[] BAD_INPUTS = {
			"",
			"    ",
			"\t",
		    "fds",
			"12b",
			",12",
			"2-1",
			",-25 -56,",
			", -1234",
			"-12 23-54,",
			"-12--23",
			"1,5,b",
			"-",
			"--",
			"-43-",
			"---",
	};

	public void testParseInvalid() {
		for (String input : BAD_INPUTS) {
			try {
				IntRangesParser.parse(input);
				fail(NumberFormatException.class.getName() + " expected while parsing [" + input + "]");
			} catch (NumberFormatException e) {
				// OK - this is expected
			}
		}

	}
}
