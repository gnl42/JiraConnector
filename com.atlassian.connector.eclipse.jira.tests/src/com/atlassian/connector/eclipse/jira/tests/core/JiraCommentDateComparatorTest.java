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

package com.atlassian.connector.eclipse.jira.tests.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.atlassian.connector.eclipse.internal.jira.core.JiraTaskDataHandler.CommentDateComparator;
import com.atlassian.connector.eclipse.internal.jira.core.model.Comment;

/**
 * @author Shawn Minto
 */
public class JiraCommentDateComparatorTest extends TestCase {

	private Comment c1;

	private Comment c2;

	private Comment c3;

	private CommentDateComparator comparator;

	@Override
	protected void setUp() throws Exception {
		c1 = createComment("author 1", "comment 1", new Date(1));
		c2 = createComment("author 2", "comment 2", new Date(2));
		c3 = createComment("author 3", "comment 3", new Date(3));

		comparator = new CommentDateComparator();
	}

	private Comment createComment(String author, String commentValue, Date created) {
		Comment comment = new Comment();
		comment.setAuthor(author);
		comment.setComment(commentValue);
		comment.setCreated(created);
		return comment;
	}

	public void testSortAscendingCommentsByDate() {
		// test comments already in ascending order
		ArrayList<Comment> ascendingComments = new ArrayList<Comment>();
		ascendingComments.add(c1);
		ascendingComments.add(c2);
		ascendingComments.add(c3);
		Collections.sort(ascendingComments, new CommentDateComparator());
		assertEquals(c1, ascendingComments.get(0));
		assertEquals(c2, ascendingComments.get(1));
		assertEquals(c3, ascendingComments.get(2));
	}

	public void testSortDescendingCommentsByDate() {
		// test comments descending order
		List<Comment> descendingComments = new ArrayList<Comment>();
		descendingComments.add(c3);
		descendingComments.add(c2);
		descendingComments.add(c1);
		Collections.sort(descendingComments, new CommentDateComparator());
		assertEquals(c1, descendingComments.get(0));
		assertEquals(c2, descendingComments.get(1));
		assertEquals(c3, descendingComments.get(2));
	}

	public void testSortRandomCommentsByDate() {
		// test comments random order
		List<Comment> randomComments = new ArrayList<Comment>();
		randomComments.add(c2);
		randomComments.add(c3);
		randomComments.add(c1);
		Collections.sort(randomComments, new CommentDateComparator());
		assertEquals(c1, randomComments.get(0));
		assertEquals(c2, randomComments.get(1));
		assertEquals(c3, randomComments.get(2));
	}
}
