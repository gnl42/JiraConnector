/**
 * 
 */
package com.atlassian.connector.eclipse.internal.bamboo.core;

import com.atlassian.theplugin.commons.cfg.SubscribedPlan;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

/**
 * @author Thomas Ehrnhoefers
 * 
 */
public class BambooUtilTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for
	 * {@link com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil#setSubcribedPlans(org.eclipse.mylyn.tasks.core.TaskRepository, java.util.Collection)}
	 * .
	 */
	public void testSetAndGetSubcribedPlans() {
		TaskRepository repo = new TaskRepository(BambooCorePlugin.CONNECTOR_KIND, "http://bamboo.atlassian.com");
		Collection<SubscribedPlan> plans = new ArrayList<SubscribedPlan>();
		plans.add(new SubscribedPlan("id1"));
		plans.add(new SubscribedPlan("id2"));
		BambooUtil.setSubcribedPlans(repo, plans);
		Collection<SubscribedPlan> result = BambooUtil.getSubscribedPlans(repo);
		assertEquals(plans.size(), result.size());
		assertEquals(plans.iterator().next(), result.iterator().next());
		assertEquals(plans.iterator().next(), new SubscribedPlan(repo.getProperty(
				"com.atlassian.connector.eclipse.bamboo.subscribedPlans").substring(0, 3)));
	}

	public void testIsSameBuildPlan() {
		fail("not implemented");
	}

}
