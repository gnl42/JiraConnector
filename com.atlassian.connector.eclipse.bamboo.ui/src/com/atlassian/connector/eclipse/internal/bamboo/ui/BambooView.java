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
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildPlanManager;
import com.atlassian.connector.eclipse.internal.bamboo.core.RefreshBuildsForAllRepositoriesJob;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.AddCommentToBuildJob;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.AddLabelToBuildJob;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RetrieveBuildLogsJob;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RetrieveTestResultsJob;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.util.DateUtil;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.internal.junit.model.JUnitModel;
import org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.ViewPart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class BambooView extends ViewPart {

	private class OpenInBrowserAction extends Action {

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				for (Iterator<BambooBuild> it = selection.iterator(); it.hasNext();) {
					String url = BambooUtil.getUrlFromBuild(it.next());
					TasksUiUtil.openUrl(url);
				}
			}
		}
	}

	private class ShowBuildLogsAction extends BaseSelectionListenerAction {

		public ShowBuildLogsAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				BambooBuild build = (BambooBuild) selection.iterator().next();
				if (build != null) {
					RetrieveBuildLogsJob job = new RetrieveBuildLogsJob(build, TasksUi.getRepositoryManager()
							.getRepository(BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()));
					job.addJobChangeListener(new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							if (event.getResult() == Status.OK_STATUS) {
								byte[] buildLog = ((RetrieveBuildLogsJob) event.getJob()).getBuildLog();
								prepareConsole();
								MessageConsoleStream messageStream = buildLogConsole.newMessageStream();
								messageStream.print(new String(buildLog));
								try {
									messageStream.close();
								} catch (IOException e) {
									StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
											"Failed to close console message stream"));
								}
							}
						}
					});
					job.schedule();

				}
			}
		}

		private void prepareConsole() {
			IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
			IConsole[] existing = consoleManager.getConsoles();
			for (IConsole element : existing) {
				if (BAMBOO_BUILD_LOG_CONSOLE.equals(element.getName())) {
					buildLogConsole = (MessageConsole) element;
				}
			}
			if (buildLogConsole == null) {
				buildLogConsole = new MessageConsole(BAMBOO_BUILD_LOG_CONSOLE, BambooImages.CONSOLE);
				consoleManager.addConsoles(new IConsole[] { buildLogConsole });
			}
			buildLogConsole.clearConsole();
			consoleManager.showConsoleView(buildLogConsole);

		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			return selection.size() == 1;
		}
	}

	private class AddLabelToBuildAction extends BaseSelectionListenerAction {
		public AddLabelToBuildAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				BambooBuild build = (BambooBuild) selection.iterator().next();
				if (build != null) {
					InputDialog inputdialog = new InputDialog(getSite().getShell(), "Add Label to Build", NLS.bind(
							"Please type the lable to add to build {0}-{1}", build.getBuildKey(),
							build.getBuildNumber()), "", new IInputValidator() {
						public String isValid(String newText) {
							if (newText == null || newText.length() < 1) {
								return "Please enter a label.";
							}
							return null;
						}
					});
					if (inputdialog.open() == Window.OK) {
						String label = inputdialog.getValue();
						AddLabelToBuildJob job = new AddLabelToBuildJob(build, TasksUi.getRepositoryManager()
								.getRepository(BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()), label);
						job.schedule();

					}
				}
			}
		}
	}

	private class AddCommentToBuildAction extends BaseSelectionListenerAction {
		public AddCommentToBuildAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				BambooBuild build = (BambooBuild) selection.iterator().next();
				if (build != null) {
					InputDialog inputdialog = new InputDialog(getSite().getShell(), "Add Comment to Build", NLS.bind(
							"Please type the comment to add to build {0}-{1}", build.getBuildKey(),
							build.getBuildNumber()), "", new IInputValidator() {
						public String isValid(String newText) {
							if (newText == null || newText.length() < 1) {
								return "Please enter a comment.";
							}
							return null;
						}
					});
					if (inputdialog.open() == Window.OK) {
						String comment = inputdialog.getValue();
						AddCommentToBuildJob job = new AddCommentToBuildJob(build, TasksUi.getRepositoryManager()
								.getRepository(BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()), comment);
						job.schedule();

					}
				}
			}
		}
	}

	private class ShowTestResultsAction extends BaseSelectionListenerAction {

		public ShowTestResultsAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				BambooBuild build = (BambooBuild) selection.iterator().next();
				if (build != null) {
					RetrieveTestResultsJob job = new RetrieveTestResultsJob(build, TasksUi.getRepositoryManager()
							.getRepository(BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()));
					job.addJobChangeListener(new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							if (event.getResult() == Status.OK_STATUS) {
								File testResults = ((RetrieveTestResultsJob) event.getJob()).getTestResultsFile();
								if (testResults != null) {
									showJUnitView(testResults);
								}
							}
						}
					});
					job.schedule();

				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			return selection.size() == 1;
		}

		private void showJUnitView(final File testResults) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@SuppressWarnings("restriction")
				public void run() {
					if (!getSite().getShell().isDisposed()) {
						try {
							JUnitModel.importTestRunSession(testResults);
							getViewSite().getPage().showView(TestRunnerViewPart.NAME);
						} catch (Exception e) {
							StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
									"Error opening JUnit View"));
						}
					}
				}
			});
		}
	}

	private class BuildContentProvider implements ITreeContentProvider {

		private List<BambooBuild> allBuilds;

		public void dispose() {
		}

		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		public Object[] getElements(Object inputElement) {
			return allBuilds.toArray();
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			allBuilds = new ArrayList<BambooBuild>();
			if (newInput != null) {
				boolean hasFailed = false;
				for (Collection<BambooBuild> collection : ((Map<TaskRepository, Collection<BambooBuild>>) newInput).values()) {
					allBuilds.addAll(collection);
					for (BambooBuild build : collection) {
						if (build.getStatus() == BuildStatus.FAILURE) {
							hasFailed = true;
						}
					}
				}
				updateViewIcon(hasFailed);
			}
		}
	}

	private static final String BAMBOO_BUILD_LOG_CONSOLE = "Bamboo Build Log";

	public static final String ID = "com.atlassian.connector.eclipse.bamboo.ui.plans";

	private TreeViewer buildViewer;

	private BambooViewDataProvider bambooDataprovider;

	private final Image buildFailedImage = CommonImages.getImage(BambooImages.STATUS_FAILED);

	private final Image buildPassedImage = CommonImages.getImage(BambooImages.STATUS_PASSED);

	private final Image buildDisabledImage = CommonImages.getImage(BambooImages.STATUS_DISABLED);

	private final Image bambooImage = CommonImages.getImage(BambooImages.BAMBOO);

	private Image currentTitleImage = bambooImage;

	private MessageConsole buildLogConsole;

	@Override
	public void createPartControl(Composite parent) {
		buildViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		buildViewer.setContentProvider(new BuildContentProvider());
		buildViewer.setUseHashlookup(true);

		TreeViewerColumn column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Build");
		column.getColumn().setWidth(300);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					return ((BambooBuild) element).getBuildName() + " - " + ((BambooBuild) element).getBuildKey();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof BambooBuild) {
					switch (((BambooBuild) element).getStatus()) {
					case FAILURE:
						return buildFailedImage;
					case SUCCESS:
						return buildPassedImage;
					default:
						return buildDisabledImage;
					}
				}
				return super.getImage(element);
			}
		});

		column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Status");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					BambooBuild build = ((BambooBuild) element);
					int totalTests = build.getTestsFailed() + build.getTestsPassed();
					if (totalTests == 0) {
						return "Tests: Testless build";
					} else {
						return NLS.bind("Tests: {0} out of {1} failed", new Object[] { build.getTestsFailed(),
								totalTests });
					}
				}
				return super.getText(element);
			}
		});

		column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Build Reason");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					BambooBuild build = ((BambooBuild) element);
					return build.getBuildReason();
				}
				return super.getText(element);
			}
		});

		column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Last Built");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					BambooBuild build = ((BambooBuild) element);
					return DateUtil.getRelativeBuildTime(build.getBuildCompletedDate());
				}
				return super.getText(element);
			}
		});

		buildViewer.getTree().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				new OpenInBrowserAction().run();
			}
		});

		fillTreeContextMenu();

		//GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(planViewer.getControl());

		contributeToActionBars();

		bambooDataprovider = BambooViewDataProvider.getInstance();
		bambooDataprovider.setView(this);
	}

	private void fillTreeContextMenu() {
		MenuManager contextMenuManager = new MenuManager("BAMBOO");
		Action refreshAction = new Action() {
			@Override
			public void run() {
				refreshBuilds();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setImageDescriptor(CommonImages.REFRESH);

		Action openInBrowserAction = new OpenInBrowserAction();
		openInBrowserAction.setText("Open in Browser");
		openInBrowserAction.setImageDescriptor(CommonImages.BROWSER_SMALL);

		BaseSelectionListenerAction showBuildLogAction = new ShowBuildLogsAction();
		showBuildLogAction.setText("Show build log");
		showBuildLogAction.setImageDescriptor(BambooImages.CONSOLE);
		showBuildLogAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(showBuildLogAction);

		BaseSelectionListenerAction showTestResultsAction = new ShowTestResultsAction();
		showTestResultsAction.setText("Show test results");
		showTestResultsAction.setImageDescriptor(BambooImages.JUNIT);
		showTestResultsAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(showTestResultsAction);

		BaseSelectionListenerAction addLabelToBuildAction = new AddLabelToBuildAction();
		addLabelToBuildAction.setText("Add label to build");
		addLabelToBuildAction.setImageDescriptor(BambooImages.LABEL);
		addLabelToBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(addLabelToBuildAction);

		BaseSelectionListenerAction addCommentToBuildAction = new AddCommentToBuildAction();
		addCommentToBuildAction.setText("Add comment to build");
		addCommentToBuildAction.setImageDescriptor(BambooImages.COMMENT);
		addCommentToBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(addCommentToBuildAction);

		contextMenuManager.add(refreshAction);
		contextMenuManager.add(openInBrowserAction);
		contextMenuManager.add(showBuildLogAction);
		contextMenuManager.add(showTestResultsAction);
		contextMenuManager.add(addLabelToBuildAction);
		contextMenuManager.add(addCommentToBuildAction);
		Menu contextMenu = contextMenuManager.createContextMenu(buildViewer.getControl());
		buildViewer.getControl().setMenu(contextMenu);
		getSite().registerContextMenu(contextMenuManager, buildViewer);
	}

	@Override
	public void setFocus() {
		// ignore

	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillPopupMenu(bars.getMenuManager());
		fillToolBar(bars.getToolBarManager());
	}

	private void fillToolBar(IToolBarManager toolBarManager) {
		Action refreshAction = new Action() {
			@Override
			public void run() {
				refreshBuilds();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setImageDescriptor(CommonImages.REFRESH);

		Action openInBrowserAction = new OpenInBrowserAction();
		openInBrowserAction.setText("Open in Browser");
		openInBrowserAction.setImageDescriptor(CommonImages.BROWSER_SMALL);

		BaseSelectionListenerAction showBuildLogAction = new ShowBuildLogsAction();
		showBuildLogAction.setText("Show build log");
		showBuildLogAction.setImageDescriptor(BambooImages.CONSOLE);
		showBuildLogAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(showBuildLogAction);

		BaseSelectionListenerAction showTestResultsAction = new ShowTestResultsAction();
		showTestResultsAction.setText("Show test results");
		showTestResultsAction.setImageDescriptor(BambooImages.JUNIT);
		showTestResultsAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(showTestResultsAction);

		BaseSelectionListenerAction addLabelToBuildAction = new AddLabelToBuildAction();
		addLabelToBuildAction.setText("Add label to build");
		addLabelToBuildAction.setImageDescriptor(BambooImages.LABEL);
		addLabelToBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(addLabelToBuildAction);

		BaseSelectionListenerAction addCommentToBuildAction = new AddCommentToBuildAction();
		addCommentToBuildAction.setText("Add comment to build");
		addCommentToBuildAction.setImageDescriptor(BambooImages.COMMENT);
		addCommentToBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(addCommentToBuildAction);

		toolBarManager.add(refreshAction);
		toolBarManager.add(openInBrowserAction);
		toolBarManager.add(showBuildLogAction);
		toolBarManager.add(showTestResultsAction);
		toolBarManager.add(addLabelToBuildAction);
		toolBarManager.add(addCommentToBuildAction);
	}

	private void refresh(Map<TaskRepository, Collection<BambooBuild>> map) {
		buildViewer.setInput(map);
	}

	private void fillPopupMenu(IMenuManager menuManager) {
	}

	public void buildsChanged() {
		if (bambooDataprovider.getBuilds() != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					refresh(bambooDataprovider.getBuilds());
				}
			});
		}
	}

	private void updateViewIcon(boolean buildsFailed) {
		if (buildsFailed) {
			currentTitleImage = buildFailedImage;
		} else {
			currentTitleImage = bambooImage;
		}
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

	/*
	 * @see IWorkbenchPart#getTitleImage()
	 */
	@Override
	public Image getTitleImage() {
		if (currentTitleImage == null) {
			return super.getTitleImage();
		}
		return currentTitleImage;
	}

	private void refreshBuilds() {
		RefreshBuildsForAllRepositoriesJob job = new RefreshBuildsForAllRepositoriesJob("Refreshing builds",
				TasksUi.getRepositoryManager());
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				if (((RefreshBuildsForAllRepositoriesJob) event.getJob()).getStatus().isOK()) {
					BuildPlanManager.getInstance().handleFinishedRefreshAllBuildsJob(event);
				}
			}
		});
		job.schedule();
	}
}
