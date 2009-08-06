/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Meghan Allen - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.monitor.usage.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.monitor.core.collection.IUsageCollector;
import org.eclipse.mylyn.internal.monitor.core.collection.ViewUsageCollector;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.atlassian.connector.eclipse.internal.monitor.usage.MonitorFileRolloverJob;
import com.atlassian.connector.eclipse.internal.monitor.usage.collectors.PerspectiveUsageCollector;

/**
 * @author Meghan Allen
 */
public class NewUsageSummaryEditorWizard extends Wizard implements INewWizard {

	private static final String TITLE = "New Usage Summary Report";

	private UsageSummaryEditorWizardPage usageSummaryPage;

	public NewUsageSummaryEditorWizard() {
		super();
		init();
		setWindowTitle(TITLE);
	}

	private void init() {
		usageSummaryPage = new UsageSummaryEditorWizardPage();
	}

	@Override
	public boolean performFinish() {

		if (!usageSummaryPage.includePerspective() && !usageSummaryPage.includeViews()) {
			return false;
		}

		List<IUsageCollector> collectors = new ArrayList<IUsageCollector>();

		if (usageSummaryPage.includePerspective()) {
			collectors.add(new PerspectiveUsageCollector());
		}
		if (usageSummaryPage.includeViews()) {
			ViewUsageCollector mylynViewUsageCollector = new ViewUsageCollector();
			collectors.add(mylynViewUsageCollector);
		}

		MonitorFileRolloverJob job = new MonitorFileRolloverJob(collectors);
		job.setPriority(Job.LONG);
		job.schedule();

		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// ignore

	}

	@Override
	public void addPages() {
		addPage(usageSummaryPage);
	}

}
