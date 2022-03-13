package com.atlassian.theplugin.commons.crucible.api.model;

import static junitx.framework.Assert.assertNotEquals;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;

/**
 * @author Pawel Niewiadomski
 * @author Wojciech Seliga
 */
public class ReviewerTest extends TestCase {

	public void testEquals() {
		Reviewer r1 = new Reviewer("u", "d", false);
		Reviewer r2 = new Reviewer("u", "d", true);
		Reviewer r3 = new Reviewer("d", "d", true);
		Reviewer r4 = new Reviewer("u", null, false);
		Reviewer r5 = new Reviewer("u", null, false);

		assertNotEquals(r1, r3);
		assertNotEquals(r1.hashCode(), r3.hashCode());

		assertNotEquals(r2, r3);
		assertNotEquals(r2.hashCode(), r3.hashCode());

		TestUtil.assertNotEquals(r1, r2);
		assertNotEquals(r1.hashCode(), r2.hashCode());

		assertNotEquals(r1, r4);

		assertEquals(r4, r5);
		assertEquals(r4.hashCode(), r5.hashCode());
	}

}
