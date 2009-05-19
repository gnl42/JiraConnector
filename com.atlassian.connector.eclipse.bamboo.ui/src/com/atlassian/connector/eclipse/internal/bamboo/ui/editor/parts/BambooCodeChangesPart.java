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

package com.atlassian.connector.eclipse.internal.bamboo.ui.editor.parts;

import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiUtil;
import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.commons.VersionedFileInfo;
import com.atlassian.theplugin.commons.bamboo.BambooChangeSet;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Part displaying code changes between current and last build
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooCodeChangesPart extends AbstractBambooEditorFormPart {

	private TreeViewer changesViewer;

	private Link link;

	public BambooCodeChangesPart() {
		super("");
	}

	public BambooCodeChangesPart(String partName) {
		super(partName);
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		super.toolkit = toolkit;
		createSectionAndComposite(parent, toolkit, 2, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED
				| ExpandableComposite.TWISTIE);

		createLink(mainComposite, toolkit, "Retrieving build details from server...", null, null, null);

		toolkit.paintBordersFor(mainComposite);

		setSection(toolkit, section);

		return control;
	}

	private void createTreeViewer() {
		Tree tree = toolkit.createTree(mainComposite, SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).hint(FULL_WIDTH, 100).applyTo(tree);
		changesViewer = new TreeViewer(tree);
		changesViewer.setContentProvider(new ITreeContentProvider() {

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof BambooChangeSet) {
					return (((BambooChangeSet) parentElement).getFiles()).toArray();
				}
				return new Object[0];
			}

			public Object getParent(Object element) {
				if (element instanceof BambooFileInfo) {
					if (buildDetails == null) {
						return null;
					}
					for (BambooChangeSet changeSet : buildDetails.getCommitInfo()) {
						if (changeSet.getFiles().contains(element)) {
							return changeSet;
						}
					}
				}
				return null;
			}

			public boolean hasChildren(Object element) {
				if (element instanceof BambooChangeSet) {
					return !(((BambooChangeSet) element).getFiles()).isEmpty();
				}
				return false;
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof BuildDetails) {
					return (((BuildDetails) inputElement).getCommitInfo()).toArray();
				}
				return new Object[0];
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		});
		changesViewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooChangeSet) {
					return "[" + ((BambooChangeSet) element).getAuthor() + "]   "
							+ BambooUiUtil.getCommentSnippet(((BambooChangeSet) element).getComment());
				} else if (element instanceof BambooFileInfo) {
					return ((VersionedFileInfo) element).getFileDescriptor().getAbsoluteUrl();
				} else {
					return super.getText(element);
				}
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof BambooChangeSet) {
					return ((BambooChangeSet) element).getComment();
				}
				return null;
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof BambooChangeSet) {
					return CommonImages.getImage(BambooImages.CHANGESET);
				} else if (element instanceof BambooFileInfo) {
					return CommonImages.getImage(BambooImages.FILE);
				}
				return null;
			}
		});
		changesViewer.setInput(buildDetails);
	}

	@Override
	public void buildInfoRetrievalDone() {
		reinitMainComposite();

		if (buildDetails != null) {
			if (buildDetails.getCommitInfo().size() > 0) {
				createTreeViewer();
			} else {
				createLink(mainComposite, toolkit, "No code changes triggered this build.", null, null, null);
			}
		} else {
			link = createLink(mainComposite, toolkit, "Retrieving build details from server failed. Click to",
					"try again", ".", new Listener() {
						public void handleEvent(Event event) {
							link.removeListener(SWT.Selection, this);
							getBuildEditor().retrieveBuildInfo();
						}
					});
		}
		getBuildEditor().reflow();
	}

	@Override
	protected void fillToolBar(ToolBarManager toolBarManager) {
		Action collapseAllAction = new Action("") { //$NON-NLS-1$
			@Override
			public void run() {
				changesViewer.collapseAll();
			}
		};
		collapseAllAction.setImageDescriptor(CommonImages.COLLAPSE_ALL);
		collapseAllAction.setToolTipText("Collapse All");
		toolBarManager.add(collapseAllAction);

		Action expandAllAction = new Action("") { //$NON-NLS-1$
			@Override
			public void run() {
				changesViewer.expandAll();
			}
		};
		expandAllAction.setImageDescriptor(CommonImages.EXPAND_ALL);
		expandAllAction.setToolTipText("Expand All");
		toolBarManager.add(expandAllAction);
	}
}
