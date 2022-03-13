package com.atlassian.theplugin.commons.crucible.api.model;

import junit.framework.TestCase;
import static junitx.framework.Assert.assertNotEquals;

/**
 * Created by IntelliJ IDEA.
 * User: pniewiadomski
 * Date: 2009-07-23
 * Time: 16:50:58
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryTest extends TestCase {

	public void testEquals() {
		Repository r1 = new Repository("n", "t", true);
		Repository r2 = new Repository("n", "t", false);
		Repository r3 = new Repository("a", "t", false);
		Repository r4 = new Repository("n", "t", false);

		assertNotEquals(r1, null);
		assertNotEquals(r1, r2);
		assertNotEquals(r1.hashCode(), r2.hashCode());
		assertNotEquals(r1, r3);
		assertNotEquals(r1.hashCode(), r3.hashCode());

		assertNotEquals(r2, r3);
		assertNotEquals(r2.hashCode(), r3.hashCode());
		
		assertEquals(r2, r2);

		assertEquals(r2, r4);
		assertEquals(r2.hashCode(), r4.hashCode());
	}
	
}
