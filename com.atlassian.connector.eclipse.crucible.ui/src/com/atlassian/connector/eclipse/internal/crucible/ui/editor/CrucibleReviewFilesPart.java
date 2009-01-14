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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.CrucibleFilePart;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The form part that displays all of the review files and their comments
 * 
 * @author Shawn Minto
 */
public class CrucibleReviewFilesPart extends AbstractCrucibleEditorFormPart {

	private CrucibleReviewEditorPage crucibleEditor;

	private Review crucibleReview;

	private Section filesSection;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review) {
		this.crucibleEditor = editor;
		this.crucibleReview = review;
	}

	@Override
	public Control createControl(Composite parent, final FormToolkit toolkit) {
		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE;
		filesSection = toolkit.createSection(parent, style);
		filesSection.setText(getSectionTitle());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(filesSection);

		if (filesSection.isExpanded()) {
			Composite composite = createCommentViewers(toolkit);
			filesSection.setClient(composite);
		} else {
			filesSection.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					if (filesSection.getClient() == null) {
						try {
							crucibleEditor.setReflow(false);
							Composite composite = createCommentViewers(toolkit);
							filesSection.setClient(composite);
						} finally {
							crucibleEditor.setReflow(true);
						}
						crucibleEditor.reflow();
					}
				}
			});
		}

		return filesSection;
	}

	private String getSectionTitle() {
		String title = "Review Files";
		try {
			return title + " (" + crucibleReview.getFiles().size() + " files)";
		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return title;

	}

	private Composite createCommentViewers(FormToolkit toolkit) {
		//CHECKSTYLE:MAGIC:OFF
		Composite composite = toolkit.createComposite(filesSection);
		composite.setLayout(new GridLayout(1, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

		try {
			List<CrucibleFileInfo> files = new ArrayList<CrucibleFileInfo>(crucibleReview.getFiles());
			Collections.sort(files, new Comparator<CrucibleFileInfo>() {

				public int compare(CrucibleFileInfo o1, CrucibleFileInfo o2) {
					if (o1 != null && o2 != null) {
						return o1.getFileDescriptor().getUrl().compareTo(o2.getFileDescriptor().getUrl());
					}
					return 0;
				}

			});

			for (CrucibleFileInfo file : files) {
				CrucibleFilePart fileComposite = new CrucibleFilePart(file, crucibleEditor);
				Control fileControl = fileComposite.createControl(composite, toolkit);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(fileControl);
			}
		} catch (ValueNotYetInitialized e) {
			// TODO do something different here?
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		//CHECKSTYLE:MAGIC:ON
		return composite;
	}

}
