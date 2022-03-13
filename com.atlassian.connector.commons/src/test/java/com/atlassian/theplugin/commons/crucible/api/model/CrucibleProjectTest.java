package com.atlassian.theplugin.commons.crucible.api.model;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;

import static junitx.framework.Assert.assertNotEquals;

public class CrucibleProjectTest extends TestCase {

	public void testEquals() {
        Collection<String> usersNames = new ArrayList<String>();
        usersNames.add("Ala");
        usersNames.add("Zosia");

		BasicProject p1 = new ExtendedCrucibleProject("0", "0", "0", null);
		BasicProject p2 = new ExtendedCrucibleProject("1", "2", "3", usersNames);
		BasicProject p3 = new ExtendedCrucibleProject("2", "X", "3", usersNames);
		BasicProject p4 = new ExtendedCrucibleProject("2", "X", "3", usersNames);
		BasicProject p5 = new ExtendedCrucibleProject("1", "2", "X", usersNames);

		//noinspection ObjectEqualsNull
		assertFalse(p1.equals(null));
		assertTrue(p1.equals(p1));

		assertFalse(p1.equals(p2));
		assertNotEquals(p1.hashCode(), p2.hashCode());

		assertFalse(p2.equals(p1));
		assertNotEquals(p2.hashCode(), p1.hashCode());
		assertFalse(p1.equals(p3));
		assertNotEquals(p1.hashCode(), p2.hashCode());
		assertFalse(p5.equals(p1));
		assertNotEquals(p5.hashCode(), p1.hashCode());

		assertTrue(p4.equals(p3));
		assertEquals(p4.hashCode(), p3.hashCode());
		assertTrue(p3.equals(p4));
		assertEquals(p3.hashCode(), p4.hashCode());
	}
}
