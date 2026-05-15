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

package me.glindholm.connector.eclipse.jira.tests.util;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.runtime.IProgressMonitor;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraAction;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssue;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueField;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraNamedFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraServerInfo;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClientCache;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.service.rest.JiraRestClientAdapter;

public class MockJiraRestClientAdapter extends JiraRestClientAdapter {

	public MockJiraRestClientAdapter(final String url, final JiraClientCache cache) {
		super(url, cache, true);
	}

	@Override
	public void addComment(final String issueKey, final String comment) throws JiraException {
		// ignore
	}

	@Override
	public void addAttachment(final String issueKey, final byte[] content, final String filename) throws JiraException {
		// ignore
	}

	@Override
	public InputStream getAttachment(final URI attachmentUri) {
		return null;
	}

	@Override
	public JiraProject[] getProjects() {
		return new JiraProject[0];
	}

	@Override
	public JiraNamedFilter[] getFavouriteFilters() {
		return new JiraNamedFilter[0];
	}

	@Override
	public JiraResolution[] getResolutions() {
		return new JiraResolution[0];
	}

	@Override
	public JiraPriority[] getPriorities() {
		return new JiraPriority[0];
	}

	@Override
	public JiraIssue getIssueByKeyOrId(final String issueKeyOrId, final IProgressMonitor monitor) throws JiraException {
		return new JiraIssue();
	}

	@Override
	public JiraStatus[] getStatuses() {
		return new JiraStatus[0];
	}

	@Override
	public JiraIssueType[] getIssueTypes() {
		return new JiraIssueType[0];
	}

	@Override
	public JiraIssueType[] getIssueTypes(final String projectKey) {
		return new JiraIssueType[0];
	}

	@Override
	public List<JiraIssue> getIssues(final String jql, final int maxResult, final IProgressMonitor monitor) throws JiraException {
		return new ArrayList<>();
	}

	@Override
	public void getProjectDetails(final JiraProject project) {
		// ignore
	}

	@Override
	public void addWorklog(final String issueKey, final JiraWorkLog jiraWorklog) throws JiraException {
		// ignore
	}

	@Override
	public JiraServerInfo getServerInfo() throws JiraException {
		return new JiraServerInfo();
	}

	@Override
	public List<JiraAction> getTransitions(final String issueKey) throws JiraException {
		return new List<>() {
			@Override
			public Iterator<JiraAction> iterator() {
				return null;
			}

			@Override
			public int size() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean isEmpty() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean contains(final Object o) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Object[] toArray() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> T[] toArray(final T[] a) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean add(final JiraAction e) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean remove(final Object o) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean containsAll(final Collection<?> c) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean addAll(final Collection<? extends JiraAction> c) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean addAll(final int index, final Collection<? extends JiraAction> c) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean removeAll(final Collection<?> c) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean retainAll(final Collection<?> c) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void clear() {
				// TODO Auto-generated method stub

			}

			@Override
			public JiraAction get(final int index) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public JiraAction set(final int index, final JiraAction element) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void add(final int index, final JiraAction element) {
				// TODO Auto-generated method stub

			}

			@Override
			public JiraAction remove(final int index) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int indexOf(final Object o) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int lastIndexOf(final Object o) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public ListIterator<JiraAction> listIterator() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ListIterator<JiraAction> listIterator(final int index) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<JiraAction> subList(final int fromIndex, final int toIndex) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public void transitionIssue(final JiraIssue issue, final String transitionKey, final String comment,
			final List<JiraIssueField> transitionFields) throws JiraException {
		// ignore
	}

	@Override
	public void assignIssue(final String issueKey, final String user, final String comment) throws JiraException {
		// ignore
	}

	@Override
	public String createIssue(final JiraIssue issue) throws JiraException {
		return "KEY-1";
	}

}
