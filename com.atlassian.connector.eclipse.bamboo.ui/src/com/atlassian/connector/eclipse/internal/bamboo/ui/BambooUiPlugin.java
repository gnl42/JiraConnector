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

package com.atlassian.connector.eclipse.internal.bamboo.ui;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.notifications.BambooNotificationProvider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.IRepositoryListener;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Shawn Minto
 */
public class BambooUiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.atlassian.connector.eclipse.bamboo.ui";

	// The shared instance
	private static BambooUiPlugin plugin;

	private BambooNotificationProvider bambooNotificationProvider;

	private MyRepositoryListener repositoryListener;

	/**
	 * The constructor
	 */
	public BambooUiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// trigger tasks ui initialization first
		IRepositoryManager repositoryManager = TasksUi.getRepositoryManager();
		repositoryManager.addListener(BambooCorePlugin.getRepositoryConnector().getClientManager());
		UIJob job = new UIJob("Initializing Bamboo") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				bambooNotificationProvider = new BambooNotificationProvider();
				try {
					IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					for (IViewReference view : activePage.getViewReferences()) {
						if (view.getId().equals(BambooView.ID)) {
							activePage.showView(BambooView.ID, null, IWorkbenchPage.VIEW_CREATE);
						}
					}
				} catch (PartInitException e) {
					StatusHandler.log(new Status(IStatus.ERROR, PLUGIN_ID, "Could not initialize Bamboo view."));
				}
				BambooCorePlugin.getBuildPlanManager().initializeScheduler(TasksUi.getRepositoryManager());
				return Status.OK_STATUS;
			}
		};
		repositoryListener = new MyRepositoryListener();
		repositoryManager.addListener(repositoryListener);
		job.schedule();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (bambooNotificationProvider != null) {
			bambooNotificationProvider.dispose();
		}
		if (repositoryListener != null) {
			IRepositoryManager repositoryManager = TasksUi.getRepositoryManager();
			repositoryManager.removeListener(repositoryListener);
			repositoryListener = null;
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static BambooUiPlugin getDefault() {
		return plugin;
	}

	private final class MyRepositoryListener implements IRepositoryListener {
		public void repositoryUrlChanged(TaskRepository repository, String oldUrl) {
		}

		public void repositorySettingsChanged(TaskRepository repository) {
		}

		public void repositoryRemoved(TaskRepository repository) {
		}

		public void repositoryAdded(TaskRepository repository) {
			if (repository.getConnectorKind().equals(BambooCorePlugin.CONNECTOR_KIND)) {
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow();
						if (activeWorkbenchWindow == null) {
							return;
						}
						IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
						if (activePage == null) {
							return;
						}
						for (IViewReference view : activePage.getViewReferences()) {
							if (view.getId().equals(BambooView.ID)) {
								return;
							}
						}
						// there is no Bamboo view, so we can now try open one, if user wants
						if (MessageDialog.openQuestion(activeWorkbenchWindow.getShell(), "Question",
								"Would you like to open Bamboo View in this perspective?")) {
							try {
								activePage.showView(BambooView.ID, null, IWorkbenchPage.VIEW_CREATE);
							} catch (PartInitException e) {
								StatusHandler.log(new Status(IStatus.ERROR, PLUGIN_ID,
										"Could not initialize Bamboo view."));
							}
						}
					}
				});

			}
		}
	}
}
