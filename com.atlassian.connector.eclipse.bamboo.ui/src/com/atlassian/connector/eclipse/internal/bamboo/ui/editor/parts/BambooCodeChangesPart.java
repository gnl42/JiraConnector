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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Part displaying code changes between current and last build
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooCodeChangesPart extends AbstractBambooEditorFormPart {

	public BambooCodeChangesPart() {
		super("");
	}

	public BambooCodeChangesPart(String partName) {
		super(partName);
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		Section section = createSection(parent, toolkit, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
				| ExpandableComposite.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
		Composite composite = toolkit.createComposite(section, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		Tree tree = toolkit.createTree(composite, SWT.SINGLE);
		TreeViewer changesViewer = new TreeViewer(tree);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tree);
		changesViewer.setContentProvider(new ITreeContentProvider() {

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof BambooChangeSet) {
					return (((BambooChangeSet) parentElement).getFiles()).toArray();
				}
				return new Object[0];
			}

			public Object getParent(Object element) {
				if (element instanceof BambooFileInfo) {
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
		changesViewer.expandAll();

		toolkit.paintBordersFor(composite);

		section.setClient(composite);
		setSection(toolkit, section);

		return control;
	}

//	private void fillTree(final Tree tree) {
//		for (BambooChangeSet changeSet : buildDetails.getCommitInfo()) {
//			TreeItem root = new TreeItem(tree, SWT.NONE);
//			root.setImage(CommonImages.getImage(BambooImages.CHANGESET));
//			root.setText("[" + changeSet.getAuthor() + "]   " + getCommentSnippet(changeSet.getComment()));
//			for (BambooFileInfo file : changeSet.getFiles()) {
//				TreeItem fileItem = new TreeItem(root, SWT.NONE);
//				fileItem.setImage(CommonImages.getImage(BambooImages.FILE));
//				fileItem.setText(file.getFileDescriptor().getAbsoluteUrl());
//			}
//			root.setExpanded(true);
//		}
//	}
//
	private String getCommentSnippet(String comment) {
		String[] commentLines = comment.split("[\r\n]");
		return commentLines.length == 0 ? "N/A" : commentLines[0];
	}
}
