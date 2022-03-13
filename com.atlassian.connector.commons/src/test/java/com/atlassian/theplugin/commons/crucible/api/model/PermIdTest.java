package com.atlassian.theplugin.commons.crucible.api.model;

import junit.framework.TestCase;
import static junitx.framework.Assert.assertNotEquals;

/**
 * Created by IntelliJ IDEA.
 * User: pniewiadomski
 * Date: 2009-07-23
 * Time: 16:45:33
 * To change this template use File | Settings | File Templates.
 */
public class PermIdTest extends TestCase {

	public void testEquals() {
		PermId p1 = new PermId("1");
		PermId p2 = new PermId(null);
		PermId p3 = new PermId("4");
		PermId p4 = new PermId("4");

		assertNotEquals(p1, p2);
		assertNotEquals(p1.hashCode(), p2.hashCode());

		assertNotEquals(p1, null);

		assertNotEquals(p1, p3);
		assertNotEquals(p1.hashCode(), p3.hashCode());
		assertNotEquals(p3, p1);

		assertEquals(p3, p4);
		assertEquals(p3.hashCode(), p4.hashCode());
	}
}
