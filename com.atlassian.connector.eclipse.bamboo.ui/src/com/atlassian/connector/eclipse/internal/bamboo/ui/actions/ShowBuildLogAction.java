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

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooConstants;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.EclipseBambooBuild;
import com.atlassian.connector.eclipse.internal.bamboo.ui.console.JavaCompilationErrorTracker;
import com.atlassian.connector.eclipse.internal.bamboo.ui.console.JavaExceptionTracker;
import com.atlassian.connector.eclipse.internal.bamboo.ui.console.JavaNativeTracker;
import com.atlassian.connector.eclipse.internal.bamboo.ui.console.JavaStackTraceTracker;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RetrieveBuildLogsJob;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.IPageBookViewPage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Action opening the build log
 * 
 * @author Thomas Ehrnhoefer
 * @author Wojciech Seliga
 */
public class ShowBuildLogAction extends EclipseBambooBuildSelectionListenerAction {
	private static boolean isConsoleAvailable = false;

	static {
		try {
			if (ConsolePlugin.getDefault() != null) {
				isConsoleAvailable = true;
			}
		} catch (Throwable e) {
			//ignore - swallow exception
		}
	}

	private static Map<BambooBuild, Object> buildLogConsoles = new HashMap<BambooBuild, Object>();

	public ShowBuildLogAction() {
		super(null);
		initialize();
		buildLogConsoles = new HashMap<BambooBuild, Object>();
	}

	private void initialize() {
		setText(BambooConstants.SHOW_BUILD_LOG_ACTION_LABEL);
		setImageDescriptor(BambooImages.CONSOLE);
	}

	@Override
	void onRun(EclipseBambooBuild eclipseBambooBuild) {
		new ShowBuildLogExecute().downloadAndShowBuildLog(eclipseBambooBuild);
	}

	@Override
	boolean onUpdateSelection(EclipseBambooBuild eclipseBambooBuild) {
		return isConsoleAvailable;
	}

	private class ShowBuildLogExecute {

		public void downloadAndShowBuildLog(final EclipseBambooBuild build) {
			final MessageConsole console = prepareConsole(build.getBuild());
			final MessageConsoleStream messageStream = console.newMessageStream();

			RetrieveBuildLogsJob job = new RetrieveBuildLogsJob(build.getBuild(), build.getTaskRepository());
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					if (event.getResult() == Status.OK_STATUS) {
						String buildLog = ((RetrieveBuildLogsJob) event.getJob()).getBuildLog();
						if (buildLog == null) {
							//retrieval failed, remove console
							handledFailedLogRetrieval(console, messageStream, build.getBuild());
						} else {
							try {
								showConsole(console);
								if (!messageStream.isClosed()) {
									messageStream.print(buildLog.length() > 0 ? buildLog : "Build log is empty.");
								} else {
									StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
											"Cannot print to console message stream."));
								}
							} finally {
								try {
									if (!messageStream.isClosed()) {
										messageStream.close();
									}
								} catch (IOException e) {
									StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
											"Failed to close console message stream.", e));
								}
							}
						}
					} else {
						//retrieval failed, remove console
						handledFailedLogRetrieval(console, messageStream, build.getBuild());
					}
				}
			});
			job.schedule();
		}

		public MessageConsole prepareConsole(BambooBuild build) {
			IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
			MessageConsole buildLogConsole = (MessageConsole) buildLogConsoles.get(build);
			if (buildLogConsole == null) {
				buildLogConsole = new MessageConsole(NLS.bind("Bamboo Build Log for {0}", build.getPlanKey() + "-"
						+ build.getNumber()), BambooImages.CONSOLE) {
					@Override
					public IPageBookViewPage createPage(IConsoleView view) {
						final IPageBookViewPage page = super.createPage(view);
						view.getSite().getShell().getDisplay().asyncExec(new Runnable() {
							@SuppressWarnings("restriction")
							public void run() {
								//needed due to a delay in the console display and thus a problem (race condition) with the action enablement
								((org.eclipse.ui.internal.console.IOConsolePage) page).getViewer().setSelection(
										new TextSelection(0, 0));
							}
						});
						return page;
					}
				};

				try {
					Class.forName("org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink");
					buildLogConsole.addPatternMatchListener(new JavaStackTraceTracker());
					buildLogConsole.addPatternMatchListener(new JavaExceptionTracker());
					buildLogConsole.addPatternMatchListener(new JavaNativeTracker());
					buildLogConsole.addPatternMatchListener(new JavaCompilationErrorTracker());
				} catch (ClassNotFoundException e) {
					// JDT is missing, that's ok
				}
			}
			buildLogConsoles.put(build, buildLogConsole);
			consoleManager.addConsoles(new IConsole[] { buildLogConsole });

			buildLogConsole.clearConsole();
			return buildLogConsole;
		}

		public void showConsole(MessageConsole console) {
			IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
			consoleManager.showConsoleView(console);
		}

		public void handledFailedLogRetrieval(MessageConsole console, MessageConsoleStream stream,
				final BambooBuild build) {
			try {
				stream.close();
			} catch (IOException e) {
				StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
						"Failed to close console message stream"));
			}
			IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
			consoleManager.removeConsoles(new IConsole[] { console });
			buildLogConsoles.remove(build);
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null, getText(), "Retrieving build logs for " + build.getPlanKey() + "-"
							+ build.getNumber() + " failed. See Error Log for details.");
				}
			});
		}
	}
}
