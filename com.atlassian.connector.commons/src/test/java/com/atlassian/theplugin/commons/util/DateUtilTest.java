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

import junit.framework.TestCase;
import org.joda.time.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 3, 2008
 * Time: 2:08:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class DateUtilTest extends TestCase {
	private DateTime date1;

	@Override
	protected void setUp() throws Exception {
		super.setUp();	//To change body of overridden methods use File | Settings | File Templates.
		date1 = new DateTime();
	}

	public void testTimeDiff() {
//		assertEquals("in the future", DateUtil.getRelativePastDate(date1.toDate(), date1.plus(Minutes.ONE).toDate()));
        assertEquals("< 1 second ago", DateUtil.getRelativePastDate(date1.toDate(), date1.plus(Minutes.ONE).toDate()));

		assertEquals("< 1 second ago", DateUtil.getRelativePastDate(date1.toDate(), date1.toDate()));

		assertEquals("1 minute ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Minutes.ONE).toDate()));
		assertEquals("2 minutes ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Minutes.TWO).toDate()));
		assertEquals("3 minutes ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Minutes.THREE).toDate()));

		assertEquals("1 hour ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Hours.ONE).toDate()));
		assertEquals("2 hours ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Hours.TWO).toDate()));
		assertEquals("3 hours ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Hours.THREE).toDate()));

		assertEquals("1 day ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Days.ONE).toDate()));
		assertEquals("2 days ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Days.TWO).toDate()));
		assertEquals("3 days ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Days.THREE).toDate()));

		assertEquals("1 week ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Weeks.ONE).toDate()));
		assertEquals("2 weeks ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Weeks.TWO).toDate()));
		assertEquals("3 weeks ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Weeks.THREE).toDate()));

		assertEquals("1 month ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Months.ONE).toDate()));
		assertEquals("2 months ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Months.TWO).toDate()));
		assertEquals("3 months ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Months.THREE).toDate()));

		assertEquals("1 year ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Years.ONE).toDate()));
		System.out.println("****************************************************");
		System.out.println(date1.toString());
		System.out.println(date1.minus(Years.TWO).toString());
		System.out.println("****************************************************");
		assertEquals("2 years ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Years.TWO).toDate()));
		assertEquals("3 years ago", DateUtil.getRelativePastDate(date1.toDate(), date1.minus(Years.THREE).toDate()));
	}

	public void testComplicatedTimeDiff() {
		final DateTime date2 = date1
				.minus(Months.ONE)
				.minus(Weeks.ONE);
		assertEquals("1 month ago", DateUtil.getRelativePastDate(date1.toDate(), date2.toDate()));
	}
}