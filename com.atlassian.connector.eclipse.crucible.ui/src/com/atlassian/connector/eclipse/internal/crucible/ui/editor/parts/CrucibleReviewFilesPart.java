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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.CrucibleReviewEditorPage;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import java.util.ArrayList;
import java.util.Collection;
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

	private List<CrucibleFilePart> parts;

	@Override
	public void initialize(CrucibleReviewEditorPage editor, Review review) {
		this.crucibleEditor = editor;
		this.crucibleReview = review;
		parts = new ArrayList<CrucibleFilePart>();
	}

	@Override
	public Collection<? extends ExpandablePart> getExpandableParts() {
		return parts;
	}

	@Override
	public CrucibleReviewEditorPage getReviewEditor() {
		return crucibleEditor;
	}

	@Override
	public Control createControl(Composite parent, final FormToolkit toolkit) {
		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED;
		filesSection = toolkit.createSection(parent, style);
		filesSection.setText(getSectionTitle());
		filesSection.clientVerticalSpacing = 0;
		GridDataFactory.fillDefaults().grab(true, false).applyTo(filesSection);

		setSection(toolkit, filesSection);

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
			return NLS.bind("{0}   ({1} files, {2} comments)", new Object[] { title, crucibleReview.getFiles().size(),
					crucibleReview.getNumberOfVersionedComments() });

		} catch (ValueNotYetInitialized e) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return title;

	}

	private Composite createCommentViewers(FormToolkit toolkit) {
		//CHECKSTYLE:MAGIC:OFF
		Composite composite = toolkit.createComposite(filesSection);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());

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
				CrucibleFilePart part = new CrucibleFilePart(file, crucibleReview, crucibleEditor);
				parts.add(part);
				Control fileControl = part.createControl(composite, toolkit);
				//GridDataFactory.fillDefaults().grab(true, false).applyTo(fileControl);
				GridData gd = GridDataFactory.fillDefaults().grab(true, false).create();
				if (!part.canExpand()) {
					gd.horizontalIndent = 15;
				}
				fileControl.setLayoutData(gd);
			}
		} catch (ValueNotYetInitialized e) {
			// TODO do something different here?
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		//CHECKSTYLE:MAGIC:ON
		return composite;
	}

	@Override
	protected void fillToolBar(ToolBarManager barManager) {
		super.fillToolBar(barManager);
	}

	public void selectAndReveal(CrucibleFileInfo crucibleFile, VersionedComment comment) {
		if (!filesSection.isExpanded()) {
			EditorUtil.toggleExpandableComposite(true, filesSection);
		}

		for (CrucibleFilePart part : parts) {
			if (part.isCrucibleFile(crucibleFile)) {
				part.selectAndReveal(comment);
			}
		}
	}

}
