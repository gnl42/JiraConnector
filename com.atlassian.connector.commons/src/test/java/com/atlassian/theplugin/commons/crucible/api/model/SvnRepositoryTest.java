package com.atlassian.theplugin.commons.crucible.api.model;

import junit.framework.TestCase;
import static junitx.framework.Assert.assertNotEquals;

/**
 * Created by IntelliJ IDEA.
 * User: pniewiadomski
 * Date: 2009-07-24
 * Time: 09:59:23
 * To change this template use File | Settings | File Templates.
 */
public class SvnRepositoryTest extends TestCase {

	public void testEquals() {
		SvnRepository s1 = new SvnRepository("n", "t", false, "url", "path");
		SvnRepository s2 = new SvnRepository("n", "t", false, "1", "path");
		SvnRepository s3 = new SvnRepository("n", "t", false, "url", "p");

		assertEquals(s1, s2);
		assertEquals(s1, s3);
		assertEquals(s2, s3);
		assertEquals(s2, s1);

		SvnRepository s4 = new SvnRepository("1", "t", false, "url", "path");
		SvnRepository s5 = new SvnRepository("n", "1", false, "url", "path");
		assertNotEquals(s1, s4);
		assertNotEquals(s1, s5);
	}
	
}
