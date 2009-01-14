/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.editor;

import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * The form part that displays all of the review files and their comments
 * 
 * @author Shawn Minto
 */
public class CrucibleReviewFilesPart extends AbstractCrucibleEditorFormPart {

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review) {
	}

	@Override
	public Control createControl(Composite parent, FormToolkit toolkit) {
		//CHECKSTYLE:MAGIC:OFF
		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED;

		Section section = toolkit.createSection(parent, style);
		section.setText("Review Files");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);

		Composite composite = toolkit.createComposite(section);
		composite.setLayout(new GridLayout(4, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
		section.setClient(composite);
//
//		try {
//			List<CrucibleFileInfo> files = new ArrayList<CrucibleFileInfo>(crucibleReview.getFiles());
//			Collections.sort(files, new Comparator<CrucibleFileInfo>() {
//
//				public int compare(CrucibleFileInfo o1, CrucibleFileInfo o2) {
//					if (o1 != null && o2 != null) {
//						return o1.getFileDescriptor().getUrl().compareTo(o2.getFileDescriptor().getUrl());
//					}
//					return 0;
//				}
//
//			});
//
//			for (CrucibleFileInfo file : files) {
//
//				final String repoUrl = file.getFileDescriptor().getRepoUrl();
//				final String revision = file.getFileDescriptor().getRevision();
//				final String filePath = file.getFileDescriptor().getUrl();
//
//				Hyperlink fileHyperlink = toolkit.createHyperlink(composite, filePath + " " + revision, SWT.NONE);
//				fileHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
//					@Override
//					public void linkActivated(HyperlinkEvent e) {
//						TeamUiUtils.openFile(repoUrl, filePath, revision, new NullProgressMonitor());
//					}
//
//				});
//
//				GridDataFactory.fillDefaults().grab(true, false).span(4, 1).applyTo(fileHyperlink);
//			}
//		} catch (ValueNotYetInitialized e) {
//			// TODO do something different here?
//			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
//		}
		//CHECKSTYLE:MAGIC:ON
		return section;
	}

}
