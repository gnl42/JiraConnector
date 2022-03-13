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
package com.atlassian.theplugin.commons.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.remoteapi.TestHttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewTestUtil;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.spartez.util.junit3.TestUtil;
import org.easymock.EasyMock;
import java.util.ArrayList;
import junit.framework.TestCase;

public class CrucibleServerFacadeImplTest extends TestCase {

	private static final ConnectionCfg SERVER_DATA = new ConnectionCfg("crucible", "", "", "");

	public void testSetReviewers() throws RemoteApiException, ServerPasswordNotProvidedException {
		final CrucibleSession mock = EasyMock.createNiceMock(CrucibleSession.class);
		final CrucibleServerFacadeImpl crucibleServerFacade = new CrucibleServerFacadeImpl(LoggerImpl.getInstance(), null,
				new TestHttpSessionCallbackImpl()) {
			@Override
					public CrucibleSession getSession(final ConnectionCfg server)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				return mock;
			}
		};
		Review review = ReviewTestUtil.createReview(SERVER_DATA.getUrl());
		review.setPermId(new PermId("CR-123"));
		review.setReviewers(MiscUtil.<Reviewer>buildHashSet());
		final ArrayList<String> newReviewers = MiscUtil.buildArrayList("wseliga", "mwent");
		EasyMock.expect(mock.getReview(review.getPermId())).andReturn(review);
		mock.addReviewers(review.getPermId(), MiscUtil.buildHashSet(newReviewers));

		EasyMock.replay(mock);
		crucibleServerFacade.setReviewers(SERVER_DATA, review.getPermId(), newReviewers);
		EasyMock.verify(mock);

		EasyMock.reset(mock);
		review.setReviewers(MiscUtil.<Reviewer>buildHashSet(
				new Reviewer("wseliga", true), new Reviewer("jgorycki", false), new Reviewer("sginter", true)));
		final ArrayList<String> newReviewers2 = MiscUtil.buildArrayList("jgorycki", "mwent", "pmaruszak");
		EasyMock.expect(mock.getReview(review.getPermId())).andReturn(review);
		mock.addReviewers(review.getPermId(), MiscUtil.buildHashSet("mwent", "pmaruszak"));
		EasyMock.expectLastCall().once();
		mock.removeReviewer(review.getPermId(), "sginter");
		EasyMock.expectLastCall().once();
		mock.removeReviewer(review.getPermId(), "wseliga");
		EasyMock.expectLastCall().once();
		EasyMock.replay(mock);
		crucibleServerFacade.setReviewers(SERVER_DATA, review.getPermId(), newReviewers2);
		EasyMock.verify(mock);
	}

	public void testUpdateProjects() throws RemoteApiException, ServerPasswordNotProvidedException {
		// testing if there is no bad caching on facade level (as it used to be)
		final CrucibleSession mock = EasyMock.createNiceMock(CrucibleSession.class);
		final CrucibleServerFacadeImpl crucibleServerFacade = new CrucibleServerFacadeImpl(LoggerImpl.getInstance(), null,
				new TestHttpSessionCallbackImpl()) {
			@Override
					public CrucibleSession getSession(final ConnectionCfg server)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				return mock;
			}
		};
		final BasicProject PR_1 = new BasicProject("myid1", "PR1", "Project1");
		final BasicProject PR_2 = new BasicProject("myid2", "PR2", "Project2");
		final BasicProject PR_3 = new BasicProject("myid3", "PR3", "Project3");
		EasyMock.expect(mock.getProjects()).andReturn(MiscUtil.buildArrayList(PR_1, PR_2));
		EasyMock.expect(mock.getProjects()).andReturn(MiscUtil.buildArrayList(PR_1, PR_2, PR_3));
		EasyMock.expect(mock.getProjects()).andReturn(MiscUtil.buildArrayList(PR_3));
		EasyMock.replay(mock);

		TestUtil.assertHasOnlyElements(crucibleServerFacade.getProjects(SERVER_DATA), PR_1, PR_2);
		TestUtil.assertHasOnlyElements(crucibleServerFacade.getProjects(SERVER_DATA), PR_1, PR_2, PR_3);
		TestUtil.assertHasOnlyElements(crucibleServerFacade.getProjects(SERVER_DATA), PR_3);

	}
}
