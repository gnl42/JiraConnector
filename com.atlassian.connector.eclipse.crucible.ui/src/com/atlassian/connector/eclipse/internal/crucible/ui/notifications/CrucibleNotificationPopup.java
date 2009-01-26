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
package com.atlassian.connector.eclipse.internal.crucible.ui.notifications;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotificationPopup;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.TaskHyperlink;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

/**
 * A custom popup for change notifications for crucible reviews
 * 
 * @author Shawn Minto
 */
public class CrucibleNotificationPopup extends AbstractNotificationPopup {

//	private static final int NUM_NOTIFICATIONS_TO_DISPLAY = 4;

	private CrucibleNotificationPopupInput input;

	public CrucibleNotificationPopup(Shell parent) {
		super(parent.getDisplay());
	}

	public void setContents(CrucibleNotificationPopupInput newInput) {
		this.input = newInput;
	}

	@Override
	protected void createTitleArea(Composite parent) {
		super.createTitleArea(parent);
	}

	@Override
	protected void createContentArea(Composite parent) {
		Composite notificationComposite = new Composite(parent, SWT.NO_FOCUS);
		notificationComposite.setLayout(new GridLayout(2, false));
		notificationComposite.setBackground(parent.getBackground());

		final Label notificationLabelIcon = new Label(notificationComposite, SWT.NO_FOCUS);
		notificationLabelIcon.setBackground(parent.getBackground());
		notificationLabelIcon.setImage(CrucibleImages.getImage(TasksUiImages.TASK));

		final TaskHyperlink itemLink = new TaskHyperlink(notificationComposite, SWT.BEGINNING | SWT.WRAP | SWT.NO_FOCUS);
		itemLink.setText(input.getLabel());
		itemLink.setImage(CrucibleImages.getImage(CommonImages.OVERLAY_SYNC_INCOMMING));
		itemLink.setBackground(parent.getBackground());
		itemLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				input.open();
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null) {
					Shell windowShell = window.getShell();
					if (windowShell != null) {
						if (windowShell.getMinimized()) {
							windowShell.setMinimized(false);
						}

						windowShell.open();
						windowShell.forceActive();
					}
				}
			}
		});

		String descriptionText = null;
		if (input.getDescription() != null) {
			descriptionText = input.getDescription();
		}
		if (descriptionText != null && !descriptionText.trim().equals("")) { //$NON-NLS-1$
			Label descriptionLabel = new Label(notificationComposite, SWT.NO_FOCUS);
			descriptionLabel.setText(descriptionText);
			descriptionLabel.setBackground(parent.getBackground());
			GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(descriptionLabel);
		}
	}

}
