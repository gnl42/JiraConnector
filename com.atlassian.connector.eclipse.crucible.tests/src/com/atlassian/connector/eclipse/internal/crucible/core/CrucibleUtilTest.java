/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.core;

import com.atlassian.connector.commons.misc.IntRange;
import com.atlassian.connector.commons.misc.IntRanges;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author Shawn Minto
 */
public class CrucibleUtilTest extends TestCase {

	public void testGetPredefinedFilter() {
		for (PredefinedFilter filter : PredefinedFilter.values()) {
			PredefinedFilter predefinedFilter = CrucibleUtil.getPredefinedFilter(filter.getFilterUrl());
			assertNotNull(predefinedFilter);
			assertEquals(filter, predefinedFilter);
		}

		assertNull(CrucibleUtil.getPredefinedFilter("NON-EXISTANT-FILTER-URL"));
	}

	public void testGetPermIdFromTaskId() {
		String permId = "CR-5";
		String taskId = CrucibleUtil.getPermIdFromTaskId(permId);

		// ensure decoding somtehing that is decoded works
		assertEquals(permId, taskId);

		taskId = CrucibleUtil.getTaskIdFromPermId(permId);
		assertNotSame(permId, taskId);

		String newPermId = CrucibleUtil.getPermIdFromTaskId(permId);
		assertEquals(permId, newPermId);
	}

	public void testGetTaskIdFromPermId() {
		String permId = "CR-5";
		String expectedTaskId = "CR%2D_5";

		String taskId = CrucibleUtil.getTaskIdFromPermId(permId);
		assertEquals(expectedTaskId, taskId);

		taskId = CrucibleUtil.getTaskIdFromPermId(taskId);
		assertEquals(expectedTaskId, taskId);

		taskId = CrucibleUtil.getTaskIdFromPermId(permId.replace("-", ""));
		assertEquals(permId.replace("-", ""), taskId);
	}

	public void testGetPredefinedFilterWebUrl() {
		String repositoryUrl = "http://crucible.atlassian.com";
		String filterUrl = "test";
		String expectedFilterWebUrl = repositoryUrl + "/cru/?filter=" + filterUrl;

		String filterWebUrl = CrucibleUtil.getPredefinedFilterWebUrl(repositoryUrl + "/", filterUrl);
		assertEquals(expectedFilterWebUrl, filterWebUrl);

		filterWebUrl = CrucibleUtil.getPredefinedFilterWebUrl(repositoryUrl, filterUrl);
		assertEquals(expectedFilterWebUrl, filterWebUrl);
	}

	public void testAddTrailingSlash() {
		String expectedUrl = "http://crucible.atlassian.com";

		String url = CrucibleUtil.addTrailingSlash(expectedUrl);
		assertEquals(expectedUrl + "/", url);

		url = CrucibleUtil.addTrailingSlash(expectedUrl + "/");
		assertEquals(expectedUrl + "/", url);

		url = CrucibleUtil.addTrailingSlash(expectedUrl + "//");
		assertEquals(expectedUrl + "//", url);
	}

	public void testGetReviewUrl() {
		String repositoryUrl = "http://crucible.atlassian.com";
		String permId = "CR-5";
		String expectedReviewWebUrl = repositoryUrl + "/cru/" + permId;

		String taskId = CrucibleUtil.getTaskIdFromPermId(permId);

		String reviewWebUrl = CrucibleUtil.getReviewUrl(repositoryUrl + "/", taskId);
		assertEquals(expectedReviewWebUrl, reviewWebUrl);

		reviewWebUrl = CrucibleUtil.getReviewUrl(repositoryUrl, taskId);
		assertEquals(expectedReviewWebUrl, reviewWebUrl);

		reviewWebUrl = CrucibleUtil.getReviewUrl(repositoryUrl + "/", permId);
		assertEquals(expectedReviewWebUrl, reviewWebUrl);

	}

	public void testGetTaskIdFromUrl() {
		String permId = "CR-5";
		String reviewWebUrl = "http://crucible.atlassian.com/cru/" + permId;

		String expectedTaskId = CrucibleUtil.getTaskIdFromPermId(permId);

		String taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertEquals(expectedTaskId, taskId);

		reviewWebUrl = "http://crucible.atlassian.com/" + permId;
		taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertNull(taskId);

		reviewWebUrl = "http://crucible.atlassian.com/" + permId + "/test";
		taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertNull(taskId);

		reviewWebUrl = "http://crucible.atlassian.com/cru/" + permId + "/test";
		taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertNull(taskId);

		reviewWebUrl = "http://crucible.atlassian.com/cru/";
		taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertNull(taskId);

		reviewWebUrl = "http://crucible.atlassian.com/cru//";
		taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertNull(taskId);

	}

	public void testGetRepositoryUrlFromUrl() {
		String expectedRepositoryUrl = "http://crucible.atlassian.com/";
		String permId = "CR-5";
		String url = expectedRepositoryUrl + "cru/" + permId;

		String repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertEquals(expectedRepositoryUrl, repositoryUrl);

		url = expectedRepositoryUrl + permId;
		repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertNull(repositoryUrl);

		url = expectedRepositoryUrl + permId + "/test";
		repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertNull(repositoryUrl);

		url = expectedRepositoryUrl + "cru/" + permId + "/test";
		repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertEquals(expectedRepositoryUrl, repositoryUrl);

		url = expectedRepositoryUrl + "cru/";
		repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertEquals(expectedRepositoryUrl, repositoryUrl);

		url = expectedRepositoryUrl + "cru//";
		repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertEquals(expectedRepositoryUrl, repositoryUrl);
	}

	public void testIsFilterDefinition() {
		RepositoryQuery query = new RepositoryQuery(CrucibleCorePlugin.CONNECTOR_KIND, "crucible-test");
		assertTrue(CrucibleUtil.isFilterDefinition(query));

		query.setAttribute("test", "tst");
		assertTrue(CrucibleUtil.isFilterDefinition(query));

		query.setAttribute(CrucibleConstants.KEY_FILTER_ID, "");
		assertTrue(CrucibleUtil.isFilterDefinition(query));

		query.setAttribute(CrucibleConstants.KEY_FILTER_ID, "someFilter");
		assertFalse(CrucibleUtil.isFilterDefinition(query));
	}

	public void testGetStatesFromString() {
		char validSep = ',';
		char invalidSep = ';';
		StringBuilder builder = new StringBuilder();
		for (State state : State.values()) {
			builder.append(state.value());
			builder.append(validSep);
		}
		assertTrue(containEqualStates(State.values(), CrucibleUtil.getStatesFromString(builder.toString())));

		assertTrue(containEqualStates(new State[0],
				CrucibleUtil.getStatesFromString("thisisaninvalidstringnoseparator")));

		assertTrue(containEqualStates(new State[0], CrucibleUtil.getStatesFromString(State.CLOSED.value() + "invalid"
				+ State.ABANDONED.value() + "stringnoseparatorwithsomestatenamesinbetween" + State.DRAFT.value())));

		builder = new StringBuilder();
		for (State state : State.values()) {
			builder.append(state.value());
			builder.append(invalidSep);
		}
		assertTrue(containEqualStates(new State[0], CrucibleUtil.getStatesFromString(builder.toString())));

		builder = new StringBuilder();
		State[] states = State.values();
		for (int i = 0; i < states.length; i++) {
			builder.append(states[i].value());
			if (i % 2 == 0) {
				builder.append(invalidSep);
			} else {
				builder.append(validSep);
			}
		}
		assertTrue(containEqualStates(new State[0], CrucibleUtil.getStatesFromString(builder.toString())));

		builder = new StringBuilder();
		builder.append(State.ABANDONED.value());
		builder.append(validSep);
		builder.append(State.ABANDONED.value());
		builder.append(validSep);
		builder.append(State.ABANDONED.value());
		builder.append(invalidSep);
		builder.append(State.ABANDONED.value());
		builder.append(validSep);
		builder.append(State.ABANDONED.value());
		builder.append(invalidSep);
		builder.append(State.ABANDONED.value());
		builder.append(validSep);
		builder.append(State.ABANDONED.value());

		assertTrue(containEqualStates(new State[] { State.ABANDONED },
				CrucibleUtil.getStatesFromString(builder.toString())));

		builder = new StringBuilder();
		builder.append(State.ABANDONED.value());
		builder.append(validSep);
		builder.append(State.APPROVAL.value());
		builder.append(validSep);
		builder.append(State.CLOSED.value());
		builder.append(invalidSep);
		builder.append(State.DRAFT.value());
		builder.append(validSep);
		builder.append(State.REJECTED.value());
		builder.append(invalidSep);
		builder.append(State.REVIEW.value());
		builder.append(validSep);
		builder.append(State.ABANDONED.value());

		assertTrue(containEqualStates(new State[] { State.ABANDONED, State.APPROVAL },
				CrucibleUtil.getStatesFromString(builder.toString())));
	}

	private boolean containEqualStates(State[] expected, State[] actual) {
		if (expected == null || actual == null) {
			return false;
		}
		if (expected.length != actual.length) {
			return false;
		}
		ArrayList<State> expectedList = new ArrayList<State>();
		Collections.addAll(expectedList, expected);
		for (State state : actual) {
			if (expectedList.contains(state)) {
				expectedList.remove(state);
			} else {
				return false;
			}
		}
		return expectedList.size() == 0;
	}

	public void testCreateCustomFilterFromQuery() {
		CustomFilterBean filter = new CustomFilterBean();
		String urlPrefix = "https://sub.domain.tld/folder/?";
		Boolean allReviewersComplete = new Boolean(true);
		String author = "aut1";
		Boolean isComplete = new Boolean(true);
		String creator = "cre1";
		String moderator = "mod1";
		boolean isOrRoles = false;
		String projectKey = "pro1";
		String reviewer = "rev1";
		State[] states = new State[] { State.ABANDONED, State.CLOSED, State.SUMMARIZE };

		IRepositoryQuery query = new RepositoryQuery(CrucibleCorePlugin.CONNECTOR_KIND, "mock query");
		StringBuilder b = new StringBuilder();
		addQueryParam(CustomFilter.ALLCOMPLETE, allReviewersComplete.toString(), b, query);
		addQueryParam(CustomFilter.AUTHOR, author, b, query);
		addQueryParam(CustomFilter.COMPLETE, isComplete.toString(), b, query);
		addQueryParam(CustomFilter.CREATOR, creator, b, query);
		addQueryParam(CustomFilter.MODERATOR, moderator, b, query);
		addQueryParam(CustomFilter.ORROLES, String.valueOf(isOrRoles), b, query);
		addQueryParam(CustomFilter.PROJECT, projectKey, b, query);
		addQueryParam(CustomFilter.REVIEWER, reviewer, b, query);
		addQueryParam(CustomFilter.STATES, getStatesString(states), b, query);
		b.insert(0, urlPrefix);

		filter.setAllReviewersComplete(allReviewersComplete);
		filter.setAuthor(author);
		filter.setComplete(isComplete);
		filter.setCreator(creator);
		filter.setModerator(moderator);
		filter.setOrRoles(isOrRoles);
		filter.setProjectKey(projectKey);
		filter.setReviewer(reviewer);
		filter.setState(states);

		CustomFilter actualFilter = CrucibleUtil.createCustomFilterFromQuery(query);

		assertTrue(equalFilters(filter, actualFilter));

		filter.setAuthor(creator);

		assertFalse(equalFilters(filter, actualFilter));
	}

	private static void addQueryParam(String name, String value, StringBuilder builder, IRepositoryQuery query) {
		if (builder != null) {
			if (builder.length() > 0) {
				builder.append("&");
			}
			builder.append(name).append("=").append(value);
		}
		if (query != null) {
			query.setAttribute(name, value);
		}
	}

	private String getStatesString(State[] states) {
		StringBuilder b = new StringBuilder();
		for (State state : states) {
			b.append(state.value());
			b.append(',');
		}
		return b.toString();
	}

	private boolean equalFilters(CustomFilter expected, CustomFilter actual) {
		return expected.isAllReviewersComplete().equals(actual.isAllReviewersComplete())
				&& expected.getAuthor().equals(actual.getAuthor()) && expected.isComplete().equals(actual.isComplete())
				&& expected.getCreator().equals(actual.getCreator())
				&& expected.getModerator().equals(actual.getModerator()) && expected.isOrRoles() == actual.isOrRoles()
				&& expected.getProjectKey().equals(actual.getProjectKey())
				&& expected.getReviewer().equals(actual.getReviewer())
				&& containEqualStates(expected.getState(), actual.getState());
	}

	public void testCreateFilterWebUrl() {
		String urlPrefix = "https://sub.domain.tld/folder";
		Boolean allReviewersComplete = new Boolean(true);
		String author = "aut1";
		Boolean isComplete = new Boolean(true);
		String creator = "cre1";
		String moderator = "mod1";
		boolean isOrRoles = false;
		String projectKey = "pro1";
		String reviewer = "rev1";
		State[] states = new State[] { State.SUMMARIZE };

		IRepositoryQuery query = new RepositoryQuery(CrucibleCorePlugin.CONNECTOR_KIND, "mock query");
		StringBuilder b = new StringBuilder();
		addQueryParam(CustomFilter.AUTHOR, author, b, query);
		addQueryParam(CustomFilter.CREATOR, creator, b, query);
		addQueryParam(CustomFilter.MODERATOR, moderator, b, query);
		addQueryParam(CustomFilter.REVIEWER, reviewer, b, query);
		addQueryParam(CustomFilter.PROJECT, projectKey, b, query);
		addQueryParam(CustomFilter.STATES, getStatesString(states), null, query);
		for (State state : states) {
			addQueryParam("state", state.value(), b, null);
		}
		addQueryParam(CustomFilter.COMPLETE, isComplete.toString(), b, query);
		addQueryParam(CustomFilter.ORROLES, String.valueOf(isOrRoles), b, query);
		addQueryParam(CustomFilter.ALLCOMPLETE, allReviewersComplete.toString(), b, query);

		String actual = CrucibleUtil.createFilterWebUrl(urlPrefix, query);
		String expected = urlPrefix + "/" + CrucibleConstants.CUSTOM_FILER_START + "&" + b.toString();

		assertEquals(expected, actual);
	}

	public void testGetTaskIdFromReview() {
		Review review = new Review("http://crucible.atlassian.com/cru/");
		String permId = "CR-5";
		String expected = "CR%2D_5";
		PermId id = new PermId(permId);
		review.setPermId(id);
		assertEquals(expected, CrucibleUtil.getTaskIdFromReview(review));
	}

	public void testIsPartialReview() {
		Review review = new Review("http://crucible.atlassian.com/cru/");
		assertTrue(CrucibleUtil.isPartialReview(review));
		Set<CrucibleFileInfo> files = new LinkedHashSet<CrucibleFileInfo>();
		review.setFiles(files);
		assertFalse(CrucibleUtil.isPartialReview(review));
	}

	public void testCreateHash() {
		Review review1 = new Review("http://crucible.atlassian.com/cru/");
		Set<CrucibleAction> actions = new LinkedHashSet<CrucibleAction>();
		actions.add(CrucibleAction.ABANDON);
		actions.add(CrucibleAction.APPROVE);
		review1.setActions(actions);
		review1.setAllowReviewerToJoin(true);
		review1.setAuthor(new User("aut"));
		review1.setCloseDate(new Date(1L));
		review1.setCreateDate(new Date(1L));
		review1.setCreator(new User("cre"));
		review1.setProjectKey("pro");
		review1.setDescription("des");
		Set<CrucibleFileInfo> files = new LinkedHashSet<CrucibleFileInfo>();
		review1.setFiles(files);
		review1.setMetricsVersion(5);
		review1.setModerator(new User("mod"));
		review1.setName("nam");
		review1.setPermId(new PermId("per"));
		review1.setProjectKey("prj");
		review1.setRepoName("rep");
		Set<Reviewer> reviewers = new LinkedHashSet<Reviewer>();
		Reviewer reviewer = new Reviewer("use", false);
		reviewers.add(reviewer);
		review1.setReviewers(reviewers);
		review1.setState(State.CLOSED);

		Review review = new Review("http://crucible.atlassian.com/cru/");
		actions = new LinkedHashSet<CrucibleAction>();
		actions.add(CrucibleAction.ABANDON);
		actions.add(CrucibleAction.APPROVE);
		review.setActions(actions);
		review.setAllowReviewerToJoin(true);
		review.setAuthor(new User("aut"));
		review.setCloseDate(new Date(1L));
		review.setCreateDate(new Date(1L));
		review.setCreator(new User("cre"));
		review.setProjectKey("pro");
		review.setDescription("des");
		files = new LinkedHashSet<CrucibleFileInfo>();
		review.setFiles(files);
		review.setMetricsVersion(5);
		review.setModerator(new User("mod"));
		review.setName("nam");
		review.setPermId(new PermId("per"));
		review.setProjectKey("prj");
		review.setRepoName("rep");
		reviewers = new LinkedHashSet<Reviewer>();
		reviewer = new Reviewer("use", false);
		reviewers.add(reviewer);
		review.setReviewers(reviewers);
		review.setState(State.CLOSED);

		List<CrucibleAction> transitions = new ArrayList<CrucibleAction>();
		transitions.add(CrucibleAction.CLOSE);
		review1.setTransitions(transitions);

		transitions = new ArrayList<CrucibleAction>();
		transitions.add(CrucibleAction.CLOSE);
		review.setTransitions(transitions);

		//test for incomplete reviews
		assertTrue(-1 == CrucibleUtil.createHash(review));
		assertTrue(CrucibleUtil.createHash(review) == CrucibleUtil.createHash(review1));

		List<Comment> genC = MiscUtil.buildArrayList();
		GeneralComment genCBean = new GeneralComment(review);
		genCBean.setCreateDate(new Date(2L));
		genC.add(genCBean);
		review1.setGeneralComments(genC);

		genC = MiscUtil.buildArrayList();
		genCBean = new GeneralComment(review);
		genCBean.setCreateDate(new Date(2L));
		genC.add(genCBean);
		review.setGeneralComments(genC);

		//test for same object
		assertTrue(CrucibleUtil.createHash(review) == CrucibleUtil.createHash(review));

		//test for equal reviews
		assertTrue(CrucibleUtil.createHash(review) == CrucibleUtil.createHash(review1));

		// test for changed actions
		actions.add(CrucibleAction.CLOSE);
		review1.setActions(actions);
		assertTrue(CrucibleUtil.createHash(review) == CrucibleUtil.createHash(review1));

		//test for unequal reviews
		review1.setAuthor(new User("new"));
		assertTrue(CrucibleUtil.createHash(review) != CrucibleUtil.createHash(review1));
	}

	public void testCanAddCommentToReview() {

		Review review = new Review("http://crucible.atlassian.com/cru/");
		assertFalse(CrucibleUtil.canAddCommentToReview(review));

		Set<CrucibleAction> actions = new LinkedHashSet<CrucibleAction>();
		actions.add(CrucibleAction.ABANDON);
		actions.add(CrucibleAction.APPROVE);
		review.setActions(actions);

		assertFalse(CrucibleUtil.canAddCommentToReview(review));

		actions.add(CrucibleAction.COMMENT);
		review.setActions(actions);
		assertTrue(CrucibleUtil.canAddCommentToReview(review));
	}

	public void testIsReviewComplete() {
		Review review = new Review("http://crucible.atlassian.com/cru/");
		review.setState(State.ABANDONED);
		assertTrue(CrucibleUtil.isCompleted(review));

		review.setState(State.APPROVAL);
		assertFalse(CrucibleUtil.isCompleted(review));

		review.setState(State.CLOSED);
		assertTrue(CrucibleUtil.isCompleted(review));

		review.setState(State.DEAD);
		assertTrue(CrucibleUtil.isCompleted(review));

		review.setState(State.DRAFT);
		assertFalse(CrucibleUtil.isCompleted(review));

		review.setState(State.REJECTED);
		assertTrue(CrucibleUtil.isCompleted(review));

		review.setState(State.REVIEW);
		assertFalse(CrucibleUtil.isCompleted(review));

		review.setState(State.SUMMARIZE);
		assertFalse(CrucibleUtil.isCompleted(review));

		review.setState(State.UNKNOWN);
		assertFalse(CrucibleUtil.isCompleted(review));
	}

	public void testIsReviewerComplete() {
		String repositoryUrl = "http://crucible.atlassian.com/cru/";
		String username = "username";
		String username2 = "username2";

		Review review = new Review(repositoryUrl);

		Set<Reviewer> reviewers = new HashSet<Reviewer>();
		review.setReviewers(reviewers);

		assertFalse(CrucibleUtil.isUserCompleted(username, review));

		Reviewer reviewer = new Reviewer(username, false);
		reviewers.add(reviewer);
		review.setReviewers(reviewers);

		assertFalse(CrucibleUtil.isUserCompleted(username, review));

		reviewer = new Reviewer(username, true);
		reviewers.clear();
		reviewers.add(reviewer);
		review.setReviewers(reviewers);

		assertTrue(CrucibleUtil.isUserCompleted(username, review));

		assertFalse(CrucibleUtil.isUserCompleted(username2, review));

		Reviewer reviewer2 = new Reviewer(username2, false);
		reviewers.add(reviewer);
		reviewers.add(reviewer2);
		review.setReviewers(reviewers);

		assertTrue(CrucibleUtil.isUserCompleted(username, review));

		assertFalse(CrucibleUtil.isUserCompleted(username2, review));
	}

	public void testVersionedCommentDeepEquals() {
		Review review = new Review("http://crucible.atlassian.com/cru/");
		VersionedComment c1 = new VersionedComment(review);
		c1.setAuthor(new User("sminto"));
		c1.setCreateDate(new Date(2L));
		c1.setDraft(true);
		c1.setMessage("testing message");
		c1.setToStartLine(1);
		c1.setToEndLine(12);

		VersionedComment c2 = new VersionedComment(review);
		c2.setAuthor(new User("sminto"));
		c2.setCreateDate(new Date(2L));
		c2.setDraft(true);
		c2.setMessage("testing message");
		c2.setToStartLine(1);
		c2.setToEndLine(12);

		assertTrue(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));

		VersionedComment r1 = new VersionedComment(review);
		r1.setAuthor(new User("sminto"));
		r1.setCreateDate(new Date(2L));
		r1.setDraft(true);
		r1.setMessage("testing message");
		r1.setToStartLine(1);
		r1.setToEndLine(12);
		r1.setReply(true);

		VersionedComment r2 = new VersionedComment(review);
		r2.setAuthor(new User("sminto"));
		r2.setCreateDate(new Date(2L));
		r2.setDraft(true);
		r2.setMessage("testing message");
		r2.setToStartLine(1);
		r2.setToEndLine(12);
		r2.setReply(true);

		c1.addReply(r1);
		assertFalse(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));

		c2.addReply(r2);
		assertTrue(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));

		r2.setMessage("test");
		assertFalse(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));

		c2.setMessage("test");
		assertFalse(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));
	}

	public void testVersionedCommentWithLineRangesDeepEquals() {
		Review review = new Review("http://crucible.atlassian.com/cru/");
		VersionedComment c1 = new VersionedComment(review);
		c1.setAuthor(new User("sminto"));
		c1.setCreateDate(new Date(2L));
		c1.setDraft(true);
		c1.setMessage("testing message");

		VersionedComment c2 = new VersionedComment(review);
		c2.setAuthor(new User("sminto"));
		c2.setCreateDate(new Date(2L));
		c2.setDraft(true);
		c2.setMessage("testing message");

		assertTrue(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));
		Map<String, IntRanges> lr1 = MiscUtil.buildHashMap();
		Map<String, IntRanges> lr2 = MiscUtil.buildHashMap();

		c1.setLineRanges(lr1);
		assertFalse(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));

		c2.setLineRanges(lr2);
		assertTrue(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));

		lr1.put("123", new IntRanges(new IntRange(10), new IntRange(20, 30)));
		assertFalse(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));

		lr2.put("123", new IntRanges(new IntRange(10), new IntRange(20, 30)));
		assertTrue(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));

		lr2.put("123", new IntRanges(new IntRange(10), new IntRange(21, 30)));
		assertFalse(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));

		lr1.put("123", new IntRanges(new IntRange(10), new IntRange(21, 30)));
		assertTrue(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));

		lr2.remove("123");
		lr2.put("124", new IntRanges(new IntRange(10), new IntRange(21, 30)));
		assertFalse(CrucibleUtil.areVersionedCommentsDeepEquals(c1, c2));

	}

	public void testGeneralCommentDeepEquals() {
		Review review = new Review("http://crucible.atlassian.com/cru/");

		GeneralComment c1 = new GeneralComment(review);
		c1.setAuthor(new User("sminto"));
		c1.setCreateDate(new Date(2L));
		c1.setDraft(true);
		c1.setMessage("testing message");

		GeneralComment c2 = new GeneralComment(review);
		c2.setAuthor(new User("sminto"));
		c2.setCreateDate(new Date(2L));
		c2.setDraft(true);
		c2.setMessage("testing message");

		assertTrue(CrucibleUtil.areGeneralCommentsDeepEquals(c1, c2));

		GeneralComment r1 = new GeneralComment(review);
		r1.setAuthor(new User("sminto"));
		r1.setCreateDate(new Date(2L));
		r1.setDraft(true);
		r1.setMessage("testing message");
		r1.setReply(true);

		GeneralComment r2 = new GeneralComment(review);
		r2.setAuthor(new User("sminto"));
		r2.setCreateDate(new Date(2L));
		r2.setDraft(true);
		r2.setMessage("testing message");
		r2.setReply(true);

		c1.addReply(r1);
		assertFalse(CrucibleUtil.areGeneralCommentsDeepEquals(c1, c2));

		c2.addReply(r2);
		assertTrue(CrucibleUtil.areGeneralCommentsDeepEquals(c1, c2));

		r2.setMessage("test");
		assertFalse(CrucibleUtil.areGeneralCommentsDeepEquals(c1, c2));

		c2.setMessage("test");
		assertFalse(CrucibleUtil.areGeneralCommentsDeepEquals(c1, c2));
	}

	public void testCrucibleFileDeepEquals() {
		Review review = new Review("http://crucible.atlassian.com/cru/");

		CrucibleFileInfo f1 = new CrucibleFileInfo(null, null, null);
		CrucibleFileInfo f2 = new CrucibleFileInfo(null, null, null);

		assertTrue(CrucibleUtil.areCrucibleFilesDeepEqual(f1, f2));

		VersionedComment c1 = new VersionedComment(review);
		c1.setAuthor(new User("sminto"));
		c1.setCreateDate(new Date(2L));
		c1.setDraft(true);
		c1.setMessage("testing message");
		c1.setToStartLine(1);
		c1.setToEndLine(12);

		VersionedComment c2 = new VersionedComment(review);
		c2.setAuthor(new User("sminto"));
		c2.setCreateDate(new Date(2L));
		c2.setDraft(true);
		c2.setMessage("testing message");
		c2.setToStartLine(1);
		c2.setToEndLine(12);

		f1.addComment(c1);
		f2.addComment(c2);

		assertTrue(CrucibleUtil.areCrucibleFilesDeepEqual(f1, f2));

		VersionedComment r1 = new VersionedComment(review);
		r1.setAuthor(new User("sminto"));
		r1.setCreateDate(new Date(2L));
		r1.setDraft(true);
		r1.setMessage("testing message");
		r1.setToStartLine(1);
		r1.setToEndLine(12);
		r1.setReply(true);

		VersionedComment r2 = new VersionedComment(review);
		r2.setAuthor(new User("sminto"));
		r2.setCreateDate(new Date(2L));
		r2.setDraft(true);
		r2.setMessage("testing message");
		r2.setToStartLine(1);
		r2.setToEndLine(12);
		r2.setReply(true);

		c1.addReply(r1);
		assertFalse(CrucibleUtil.areCrucibleFilesDeepEqual(f1, f2));

		c2.addReply(r2);
		assertTrue(CrucibleUtil.areCrucibleFilesDeepEqual(f1, f2));

		r2.setMessage("test");
		assertFalse(CrucibleUtil.areCrucibleFilesDeepEqual(f1, f2));

		c2.setMessage("test");
		assertFalse(CrucibleUtil.areCrucibleFilesDeepEqual(f1, f2));
	}
}
