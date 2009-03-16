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
import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.commons.VersionedFileInfo;
import com.atlassian.theplugin.commons.bamboo.BambooChangeSet;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;

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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * Part displaying code changes between current and last build
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooCodeChangesPart extends AbstractBambooEditorFormPart {

	private TreeViewer changesViewer;

	private Hyperlink link;

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

		createLinks(mainComposite, toolkit, "Retrieving build details from server...", "", "", null);

		toolkit.paintBordersFor(mainComposite);

		section.setClient(mainComposite);
		setSection(toolkit, section);

		return control;
	}

	private String getCommentSnippet(String comment) {
		String[] commentLines = comment.split("[\r\n]");
		return commentLines.length == 0 ? "N/A" : commentLines[0];
	}

	private void createTreeViewer() {
		Tree tree = toolkit.createTree(mainComposite, SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 100).applyTo(tree);
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
							+ getCommentSnippet(((BambooChangeSet) element).getComment());
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
	public void buildInfoRetrievalDone(boolean success) {
		reinitMainComposite();

		if (success) {
			if (buildDetails.getCommitInfo().size() > 0) {
				createTreeViewer();
			} else {
				createLinks(mainComposite, toolkit, "No code changes triggered this build.", null, null, null);
			}
		} else {
			link = createLinks(mainComposite, toolkit, "Retrieving build details from server failed. Click to",
					"try again", ".", new HyperlinkAdapter() {
						@Override
						public void linkActivated(HyperlinkEvent e) {
							link.removeHyperlinkListener(this);
							getBuildEditor().retrieveBuildInfo();
						}
					});
		}
		getBuildEditor().reflow();
	}
}
