/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.reports.ui.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.monitor.core.collection.CommandUsageCollector;
import org.eclipse.mylyn.internal.monitor.core.collection.DelegatingUsageCollector;
import org.eclipse.mylyn.internal.monitor.core.collection.IUsageCollector;
import org.eclipse.mylyn.internal.monitor.core.collection.SummaryCollector;
import org.eclipse.mylyn.internal.monitor.core.collection.ViewUsageCollector;
import org.eclipse.mylyn.internal.monitor.reports.MonitorReportsPlugin;
import org.eclipse.mylyn.internal.monitor.usage.ReportGenerator;
import org.eclipse.mylyn.internal.monitor.usage.UiUsageMonitorPlugin;
import org.eclipse.mylyn.internal.monitor.usage.collectors.PerspectiveUsageCollector;
import org.eclipse.mylyn.internal.monitor.usage.editors.UsageStatsEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ViewPluginAction;

/**
 * @author Mik Kersten
 */
public class EclipseUsageSummaryAction implements IViewActionDelegate {
	ReportGenerator generator = null;

	public void init(IViewPart view) {
		// ignore
	}

	public void run(IAction action) {
		if (action instanceof ViewPluginAction) {
			ViewPluginAction objectAction = (ViewPluginAction) action;
			final List<File> files = getStatsFilesFromSelection(objectAction);
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {

					List<IUsageCollector> delegates = new ArrayList<IUsageCollector>();
					delegates.add(new ViewUsageCollector());
					delegates.add(new PerspectiveUsageCollector());
					delegates.add(new CommandUsageCollector());
					// delegates.add(new CsvOutputCollector());
					delegates.add(new SummaryCollector());

					DelegatingUsageCollector collector = new DelegatingUsageCollector();
					collector.setReportTitle("Usage Summary");
					collector.setDelegates(delegates);
					generator = new ReportGenerator(UiUsageMonitorPlugin.getDefault().getInteractionLogger(),
							collector, new JobChangeAdapter() {
								@Override
								public void done(IJobChangeEvent event) {
									try {
										IWorkbenchPage page = MonitorReportsPlugin.getDefault()
												.getWorkbench()
												.getActiveWorkbenchWindow()
												.getActivePage();
										if (page == null) {
											return;
										}
										IEditorInput input = new UsageStatsEditorInput(files, generator);
										page.openEditor(input, MonitorReportsPlugin.REPORT_SUMMARY_ID);
									} catch (PartInitException e) {
										StatusHandler.log(new Status(IStatus.ERROR, MonitorReportsPlugin.ID_PLUGIN,
												"Could not open summary editor", e));
									}
								}
							});

				}
			});
		}
	}

	/**
	 * TODO: move
	 */
	public static List<File> getStatsFilesFromSelection(ViewPluginAction objectAction) {
		final List<File> files = new ArrayList<File>();
		if (objectAction.getSelection() instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) objectAction.getSelection();
			for (Object object : structuredSelection.toList()) {
				if (object instanceof IFile) {
					IFile file = (IFile) object;
					if (file.getFileExtension().equals("zip")) {
						files.add(new File(file.getLocation().toString()));
					}
				}
			}
		}
		Collections.sort(files); // ensure that they are sorted by date

		if (files.isEmpty()) {
			files.add(UiUsageMonitorPlugin.getDefault().getMonitorLogFile());
		}
		return files;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

}
