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

package com.atlassian.connector.eclipse.internal.bamboo.ui.actions;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RetrieveBuildLogsJob;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.part.IPageBookViewPage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Action opening the build log
 * 
 * @author Thomas Ehrnhoefer
 */
public class ShowBuildLogAction extends AbstractBambooAction {
	private static final String BAMBOO_BUILD_LOG_CONSOLE = "Bamboo Build Log for ";

	private static Map<BambooBuild, MessageConsole> buildLogConsoles = new HashMap<BambooBuild, MessageConsole>();

	public ShowBuildLogAction(ISelectionProvider selectionProvider) {
		super(selectionProvider);
	}

	public ShowBuildLogAction(BambooBuild bambooBuild) {
		super(bambooBuild);
		buildLogConsoles = new HashMap<BambooBuild, MessageConsole>();
	}

	@Override
	public void run() {
		final BambooBuild build = getBuild();
		if (build != null) {
			final MessageConsole console = prepareConsole(build);
			final MessageConsoleStream messageStream = console.newMessageStream();

			RetrieveBuildLogsJob job = new RetrieveBuildLogsJob(build, TasksUi.getRepositoryManager().getRepository(
					BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()));
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					if (event.getResult() == Status.OK_STATUS) {
						String buildLog = ((RetrieveBuildLogsJob) event.getJob()).getBuildLog();
						if (buildLog == null) {
							//retrieval failed, remove console
							handledFailedLogRetrieval(console, messageStream, build);
						} else {
							try {
								showConsole(console);
								messageStream.print(buildLog);
							} finally {
								try {
									messageStream.close();
								} catch (IOException e) {
									StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
											"Failed to close console message stream"));
								}
							}
						}
					} else {
						//retrieval failed, remove console
						handledFailedLogRetrieval(console, messageStream, build);
					}
				}
			});
			job.schedule();

		}
	}

	private MessageConsole prepareConsole(BambooBuild build) {
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		MessageConsole buildLogConsole = buildLogConsoles.get(build);
		if (buildLogConsole == null) {
			buildLogConsole = new MessageConsole(BAMBOO_BUILD_LOG_CONSOLE + build.getPlanKey() + " - "
					+ build.getNumber(), BambooImages.CONSOLE) {
				@Override
				public IPageBookViewPage createPage(IConsoleView view) {
					final IPageBookViewPage page = super.createPage(view);
					view.getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							((IOConsolePage) page).getViewer().setSelection(new TextSelection(0, 0));
						}
					});
					return page;
				}
			};

		}
		buildLogConsoles.put(build, buildLogConsole);
		consoleManager.addConsoles(new IConsole[] { buildLogConsole });

		buildLogConsole.clearConsole();
		return buildLogConsole;
	}

	private void showConsole(MessageConsole console) {
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.showConsoleView(console);
	}

	private void handledFailedLogRetrieval(MessageConsole console, MessageConsoleStream stream, final BambooBuild build) {
		try {
			stream.close();
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
					"Failed to close console message stream"));
		}
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.removeConsoles(new IConsole[] { console });
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(null, getText(), "Retrieving build logs for " + build.getPlanKey() + "-"
						+ build.getNumber() + " failed. See error log for details.");
			}
		});
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.size() == 1) {
			if (selection.getFirstElement() instanceof BambooBuild) {
				try {
					build = (BambooBuild) selection.getFirstElement();
					build.getNumber();
					return true;
				} catch (UnsupportedOperationException e) {
					// ignore
					build = null;
				}
			} else if (selection.getFirstElement() instanceof Object) {
				System.out.println();
			}
		}
		return false;
	}
}
