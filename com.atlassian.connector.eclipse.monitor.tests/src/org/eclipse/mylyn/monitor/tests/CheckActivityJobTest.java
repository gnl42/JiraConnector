/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.tests;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.monitor.ui.CheckActivityJob;
import org.eclipse.mylyn.internal.monitor.ui.IActivityManagerCallback;

/**
 * @author Steffen Pingel
 */
public class CheckActivityJobTest extends TestCase {

	private StubCallback callback;

	private TestableCheckActivityJob job;

	@Override
	protected void setUp() throws Exception {
		callback = new StubCallback();
		job = new TestableCheckActivityJob(callback);
	}

	public void testInactivityTimeout() throws Exception {
		callback.lastEventTime = System.currentTimeMillis() - 41;
		job.setInactivityTimeout(40);
		job.run();
		assertFalse(job.isActive());
		job.run();
		assertFalse(job.isActive());
		callback.lastEventTime = System.currentTimeMillis();
		job.run();
		assertTrue(job.isActive());
		assertEquals(0, callback.activeTime);
		Thread.sleep(6);
		job.run();
		assertTrue(job.isActive());
		assertTrue("expected less than 5 < activeTime < 20, got " + callback.activeTime, callback.activeTime > 5
				&& callback.activeTime < 20);
	}

	public void testResumeFromSleepNoTimeout() throws Exception {
		job.setInactivityTimeout(0);
		job.run();
		assertTrue(job.isActive());
		job.run();
		assertTrue(job.isActive());
		assertEquals(1, callback.eventCount);
		job.run();
		assertEquals(2, callback.eventCount);
		assertTrue(job.isActive());
		Thread.sleep(11);
		job.run();
		assertTrue(job.isActive());
		assertTrue("expected more than 10 ms, got " + callback.activeTime, callback.activeTime > 10);
		assertEquals(3, callback.eventCount);
	}

	public void testResumeFromSleepTimeoutNoEvent() throws Exception {
		callback.lastEventTime = System.currentTimeMillis();
		job.setInactivityTimeout(20);
		job.setTick(20);
		job.run();
		assertTrue(job.isActive());
		job.run();
		assertTrue(job.isActive());
		assertEquals(1, callback.eventCount);
		Thread.sleep(61);
		// resume from sleep past timeout
		job.run();
		assertFalse(job.isActive());
		job.run();
		assertFalse(job.isActive());
		assertTrue("expected less than 10 ms, got " + callback.activeTime, callback.activeTime < 10);
		assertEquals(1, callback.eventCount);
		assertEquals(callback.lastEventTime, callback.startTime);
	}

	public void testResumeFromSleepTimeoutEvent() throws Exception {
		callback.lastEventTime = System.currentTimeMillis();
		job.setInactivityTimeout(20);
		job.setTick(20);
		job.run();
		assertTrue(job.isActive());
		job.run();
		assertTrue(job.isActive());
		assertEquals(1, callback.eventCount);
		Thread.sleep(41);
		// resume from sleep past timeout
		job.run();
		assertTrue(callback.inactive);
		assertFalse(job.isActive());
		Thread.sleep(11);
		// should still discard events
		job.run();
		assertFalse(job.isActive());
		// start activity
		callback.lastEventTime = System.currentTimeMillis();
		job.run();
		assertTrue(job.isActive());
		assertEquals(1, callback.eventCount);
		Thread.sleep(11);
		job.run();
		// check if time sleeping was logged
		assertTrue("expected less than 10 < activeTime < 20, got " + callback.activeTime, callback.activeTime > 10
				&& callback.activeTime < 20);
		assertEquals(2, callback.eventCount);
	}

	public void testResumeFromSleepTimeoutEventDiscarded() throws Exception {
		callback.lastEventTime = System.currentTimeMillis();
		job.setInactivityTimeout(20);
		job.setTick(20);
		job.run();
		assertTrue(job.isActive());
		job.run();
		assertTrue(job.isActive());
		assertEquals(1, callback.eventCount);
		Thread.sleep(61);
		// resume from sleep past timeout
		callback.lastEventTime = System.currentTimeMillis();
		job.run();
		assertFalse(callback.inactive);
		assertTrue(job.isActive());
		Thread.sleep(6);
		job.run();
		assertTrue(job.isActive());
		// check if time sleeping was logged
		assertTrue("expected less than 5 < activeTime < 10, got " + callback.activeTime, callback.activeTime > 5
				&& callback.activeTime < 10);
		assertEquals(2, callback.eventCount);
	}

	private class TestableCheckActivityJob extends CheckActivityJob {

		public TestableCheckActivityJob(IActivityManagerCallback callback) {
			super(callback);
		}

		public IStatus run() {
			return super.run(new NullProgressMonitor());
		}

		@Override
		protected boolean isEnabled() {
			return true;
		}

		public void setPreviousEventTime(long previousEventTime) {
			this.previousEventTime = previousEventTime;
		}

		public void setTick(long tick) {
			this.tick = tick;
		}

		@Override
		public void reschedule() {
			// ignore, job is called explicitly from test
		}

	}

	private class StubCallback implements IActivityManagerCallback {

		private boolean inactive;

		private long lastEventTime;

		private long activeTime;

		private long eventCount;

		private long startTime;

		public void addMonitoredActivityTime(long startTime, long endTime) {
			this.startTime = startTime;
			this.activeTime += endTime - startTime;
			this.eventCount++;
		}

		public void inactive() {
			this.inactive = true;
		}

		public long getLastEventTime() {
			return this.lastEventTime;
		}

		public void active() {
		}

	}

}
