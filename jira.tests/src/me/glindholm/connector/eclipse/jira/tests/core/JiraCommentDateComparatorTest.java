/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package me.glindholm.connector.eclipse.jira.tests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.JiraTaskDataHandler.CommentDateComparator;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComment;
import me.glindholm.jira.rest.client.api.domain.BasicUser;

/**
 * @author Shawn Minto
 */
public class JiraCommentDateComparatorTest  {

	private JiraComment c1;

	private JiraComment c2;

	private JiraComment c3;

	private CommentDateComparator comparator;

	@BeforeEach
	protected void setUp() throws Exception {
		c1 = createJiraComment("author 1", "comment 1", new Date(1).toInstant());
		c2 = createJiraComment("author 2", "comment 2", new Date(2).toInstant());
		c3 = createJiraComment("author 3", "comment 3", new Date(3).toInstant());

		comparator = new CommentDateComparator();
	}

	private JiraComment createJiraComment(final String author, final String commentValue, final Instant created) {
		final var comment = new JiraComment();
		comment.setAuthor( new BasicUser(null, author, author));
		comment.setComment(commentValue);
		comment.setCreated(created);
		return comment;
	}

	@Test
	public void testSortAscendingCommentsByDate() {
		// test comments already in ascending order
		final List<JiraComment> ascendingComments = new ArrayList<>();
		ascendingComments.add(c1);
		ascendingComments.add(c2);
		ascendingComments.add(c3);
		Collections.sort(ascendingComments, new CommentDateComparator());
		assertEquals(c1, ascendingComments.get(0));
		assertEquals(c2, ascendingComments.get(1));
		assertEquals(c3, ascendingComments.get(2));
	}

	@Test
	public void testSortDescendingCommentsByDate() {
		// test comments descending order
		final List<JiraComment> descendingComments = new ArrayList<>();
		descendingComments.add(c3);
		descendingComments.add(c2);
		descendingComments.add(c1);
		Collections.sort(descendingComments, new CommentDateComparator());
		assertEquals(c1, descendingComments.get(0));
		assertEquals(c2, descendingComments.get(1));
		assertEquals(c3, descendingComments.get(2));
	}

	@Test
	public void testSortRandomCommentsByDate() {
		// test comments random order
		final List<JiraComment> randomComments = new ArrayList<>();
		randomComments.add(c2);
		randomComments.add(c3);
		randomComments.add(c1);
		Collections.sort(randomComments, new CommentDateComparator());
		assertEquals(c1, randomComments.get(0));
		assertEquals(c2, randomComments.get(1));
		assertEquals(c3, randomComments.get(2));
	}
}
