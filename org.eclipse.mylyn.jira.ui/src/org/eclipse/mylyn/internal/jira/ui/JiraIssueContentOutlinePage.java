/*******************************************************************************
 * Copyright (c) 2006 Jira Dashboard project.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.internal.jira.ui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylar.internal.jira.MylarJiraPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.tigris.jira.core.model.Comment;
import org.tigris.jira.core.model.Issue;

/**
 * @author Brock Janiczak
 */
public class JiraIssueContentOutlinePage extends ContentOutlinePage {

	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	private final Issue issue;

	private Image commentImage;
	private Image jiraImage;

	private final JiraTaskEditor editor;
	
	public JiraIssueContentOutlinePage(Issue issue) {
		this(null, issue);
	}
	
	public JiraIssueContentOutlinePage(JiraTaskEditor editor, Issue issue) {
		this.editor = editor;
		this.issue = issue;
		commentImage = MylarJiraPlugin.getDefault().getImageRegistry().get("icons/obj16/comment.gif");
		jiraImage = MylarJiraPlugin.getDefault().getImageRegistry().get("icons/obj16/jira.png");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.contentoutline.ContentOutlinePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		TreeViewer treeViewer = getTreeViewer();
		
		treeViewer.setContentProvider(new ITreeContentProvider() {
		
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		
			public void dispose() {
			}
		
			public Object[] getElements(Object inputElement) {
				List<Object> elements = new ArrayList<Object>();
				elements.add(issue.getSummary());
				elements.addAll(Arrays.asList(issue.getComments()));
				
				// root elements are summary, environment, description and "comments"
				
				return elements.toArray();
			}
		
			public boolean hasChildren(Object element) {
				// "comments" has children
				return false;
			}
		
			public Object getParent(Object element) {
				return null;
			}
		
			public Object[] getChildren(Object parentElement) {
				// Get all of the comments from the comment list
				return new Object[0];
			}
		
		});
		treeViewer.setLabelProvider(new LabelProvider() {
		
			public String getText(Object element) {
				if (element instanceof Comment) {
					Comment comment = (Comment)element;
					return DATE_FORMAT.format(comment.getCreated()) + " " + comment.getAuthor();
				}
				return super.getText(element);
			}
		
			public Image getImage(Object element) {
				if (element instanceof Comment) {
					return commentImage;
				} else {
					// TODO the summary should be a placeholder object
					return jiraImage;
				}
			}
		
		});
		
		treeViewer.addOpenListener(new IOpenListener() {
		
			public void open(OpenEvent event) {
				if (editor == null) {
					return;
				}
				
				Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if (selection instanceof Comment) {
					editor.revealComment((Comment)selection);
				}
			}
		
		});
		treeViewer.setInput(issue);
	}
	
}
