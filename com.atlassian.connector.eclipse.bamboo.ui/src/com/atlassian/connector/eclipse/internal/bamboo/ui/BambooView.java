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

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooConstants;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildPlanManager;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildsChangedEvent;
import com.atlassian.connector.eclipse.internal.bamboo.core.BuildsChangedListener;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooBuildViewerComparator.SortOrder;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.OpenBambooEditorAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.OpenRepositoryConfigurationAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.RepositoryConfigurationAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.ToggleAutoRefreshAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.dialogs.AddLabelOrCommentDialog;
import com.atlassian.connector.eclipse.internal.bamboo.ui.dialogs.AddLabelOrCommentDialog.Type;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RetrieveBuildLogsJob;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RetrieveTestResultsJob;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RunBuildJob;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.util.DateUtil;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.junit.launcher.AssertionVMArg;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.internal.junit.launcher.JUnitMigrationDelegate;
import org.eclipse.jdt.internal.junit.launcher.TestKindRegistry;
import org.eclipse.jdt.internal.junit.model.JUnitModel;
import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeColumnViewerLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoriesView;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class BambooView extends ViewPart {

	private static final String CREATE_A_NEW_REPOSITORY_LINK = "create a new repository";

	private static final String OPEN_REPOSITORY_VIEW_LINK = "Open the Task Repositories view";

	private class OpenInBrowserAction extends BaseSelectionListenerAction {
		public OpenInBrowserAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
					Object selected = it.next();
					if (selected instanceof BambooBuild) {
						String url = BambooUtil.getUrlFromBuild((BambooBuild) selected);
						TasksUiUtil.openUrl(url);
					}
				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			if (selection.size() >= 1) {
				try {
					Iterator it = selection.iterator();
					while (it.hasNext()) {
						((BambooBuild) it.next()).getNumber();
					}
					return true;
				} catch (UnsupportedOperationException e) {
					// igonre
				}
			}
			return false;
		}
	}

	private class RunBuildAction extends BaseSelectionListenerAction {
		public RunBuildAction() {
			super(null);
		}

		@Override
		public void run() {
			ISelection s = buildViewer.getSelection();
			if (s instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) s;
				Object selected = selection.iterator().next();
				if (selected instanceof BambooBuild) {
					final BambooBuild build = (BambooBuild) selected;
					if (build != null) {
						RunBuildJob job = new RunBuildJob(build, TasksUi.getRepositoryManager().getRepository(
								BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()));
						job.addJobChangeListener(new JobChangeAdapter() {
							@Override
							public void done(IJobChangeEvent event) {
								if (event.getResult().getCode() == IStatus.ERROR) {
									Display.getDefault().syncExec(new Runnable() {
										public void run() {
											MessageDialog.openError(getSite().getShell(), getText(), "Running build "
													+ build.getPlanKey() + " failed. See error log for details.");
										}
									});
								}
							}
						});
						job.schedule();

					}
				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			if (selection.size() != 1) {
				return false;
			}
			BambooBuild build = (BambooBuild) selection.iterator().next();
			if (build != null) {
				try {
					build.getNumber();
					return build.getEnabled();
				} catch (UnsupportedOperationException e) {
					return false;
				}
			}
			return false;
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
				Object selected = selection.iterator().next();
				if (selected instanceof BambooBuild) {
					final BambooBuild build = (BambooBuild) selected;
					if (build != null) {
						final MessageConsole console = prepareConsole(build);
						final MessageConsoleStream messageStream = console.newMessageStream();

						RetrieveBuildLogsJob job = new RetrieveBuildLogsJob(build, TasksUi.getRepositoryManager()
								.getRepository(BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()));
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
										console.activate();
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
			}
		}

		private MessageConsole prepareConsole(BambooBuild build) {
			IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
			MessageConsole buildLogConsole = buildLogConsoles.get(build);
			if (buildLogConsole == null) {
				buildLogConsole = new MessageConsole(BAMBOO_BUILD_LOG_CONSOLE + build.getPlanKey() + " - "
						+ build.getNumber(), BambooImages.CONSOLE);
			}
			BambooView.this.buildLogConsoles.put(build, buildLogConsole);
			consoleManager.addConsoles(new IConsole[] { buildLogConsole });

			buildLogConsole.clearConsole();
			return buildLogConsole;
		}

		private void showConsole(MessageConsole console) {
			IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
			consoleManager.showConsoleView(console);
		}

		private void handledFailedLogRetrieval(MessageConsole console, MessageConsoleStream stream,
				final BambooBuild build) {
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
					MessageDialog.openError(getSite().getShell(), getText(), "Retrieving build logs for "
							+ build.getPlanKey() + "-" + build.getNumber() + " failed. See error log for details.");
				}
			});
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			if (selection.size() == 1) {
				try {
					((BambooBuild) selection.getFirstElement()).getNumber();
					return true;
				} catch (UnsupportedOperationException e) {
					// ignore
				}
			}
			return false;
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
				Object selected = selection.iterator().next();
				if (selected instanceof BambooBuild) {
					final BambooBuild build = (BambooBuild) selected;
					if (build != null) {
						AddLabelOrCommentDialog dialog = new AddLabelOrCommentDialog(getSite().getShell(), build,
								TasksUi.getRepositoryManager().getRepository(BambooCorePlugin.CONNECTOR_KIND,
										build.getServerUrl()), Type.LABEL);
						dialog.open();
					}
				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			if (selection.size() == 1) {
				try {
					((BambooBuild) selection.getFirstElement()).getNumber();
					return true;
				} catch (UnsupportedOperationException e) {
					// ignore
				}
			}
			return false;
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
				Object selected = selection.iterator().next();
				if (selected instanceof BambooBuild) {
					final BambooBuild build = (BambooBuild) selected;
					if (build != null) {
						AddLabelOrCommentDialog dialog = new AddLabelOrCommentDialog(getSite().getShell(), build,
								TasksUi.getRepositoryManager().getRepository(BambooCorePlugin.CONNECTOR_KIND,
										build.getServerUrl()), Type.COMMENT);
						dialog.open();
					}
				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			if (selection.size() == 1) {
				try {
					((BambooBuild) selection.getFirstElement()).getNumber();
					return true;
				} catch (UnsupportedOperationException e) {
					// ignore
				}
			}
			return false;
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
				Object selected = selection.getFirstElement();
				if (selected instanceof BambooBuild) {
					final BambooBuild build = (BambooBuild) selected;
					if (build != null) {
						RetrieveTestResultsJob job = new RetrieveTestResultsJob(build, TasksUi.getRepositoryManager()
								.getRepository(BambooCorePlugin.CONNECTOR_KIND, build.getServerUrl()));
						job.addJobChangeListener(new JobChangeAdapter() {
							@Override
							public void done(IJobChangeEvent event) {
								if (event.getResult() == Status.OK_STATUS) {
									File testResults = ((RetrieveTestResultsJob) event.getJob()).getTestResultsFile();
									if (testResults != null) {
										showJUnitView(testResults, build.getPlanKey() + "-" + build.getNumber());
									} else {
										Display.getDefault().syncExec(new Runnable() {
											public void run() {
												MessageDialog.openError(getSite().getShell(), getText(),
														"Retrieving test result for " + build.getPlanKey() + "-"
																+ build.getNumber()
																+ " failed. See error log for details.");
											}
										});
									}
								}
							}
						});
						job.schedule();

					}
				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			//check if JDT is available
			try {
				if (JUnitPlugin.getDefault() == null) {
					return false;
				}
			} catch (Throwable e) {
				return false;
			}
			if (selection.size() != 1) {
				return false;
			}
			BambooBuild build = (BambooBuild) selection.iterator().next();
			if (build != null) {
				try {
					build.getNumber();
					return (build.getTestsFailed() + build.getTestsPassed()) > 0;
				} catch (UnsupportedOperationException e) {
					return false;
				}
			}
			return false;
		}

		private void showJUnitView(final File testResults, final String buildKey) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@SuppressWarnings("restriction")
				public void run() {
					if (!getSite().getShell().isDisposed()) {
						try {
							getViewSite().getPage().showView(TestRunnerViewPart.NAME);
							final IJavaProject[] javaProjectsTmp = JavaModelManager.getJavaModelManager()
									.getJavaModel()
									.getJavaProjects();
							final Collection<IJavaProject> javaProjects = Arrays.asList(javaProjectsTmp);
							final CompositeJavaProject compositeProject = new CompositeJavaProject(javaProjects);
//							final IJavaProject[] javaProjectsTmp = JavaCore.create(
//									ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
							final TestRunSession trs = new TestRunSession("Bamboo build " + buildKey, compositeProject) {

								@Override
								public boolean rerunTest(String testId, String className, String testName,
										String launchMode) throws CoreException {
									String name = className;
									if (testName != null) {
										name += "." + testName; //$NON-NLS-1$
									}
									final IType testElement = compositeProject.findType(className);
									if (testElement == null) {
										throw new CoreException(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
												"Cannot find Java project which contains class " + className + "."));
									}
									final ILaunchConfigurationWorkingCopy newCfg = createLaunchConfiguration(testElement);
									newCfg.launch(launchMode, null);
									return true;
								}
							};
							JUnitModel.importIntoTestRunSession(testResults, trs);
							JUnitPlugin.getModel().addTestRunSession(trs);
						} catch (Exception e) {
							StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
									"Error opening JUnit View"));
						}
					}
				}
			});
		}
	}

	private static final String EMPTY_STRING = "";

	private static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * this method is practically stolen "as is" from
	 * {@link org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut#createLaunchConfiguration(IJavaElement)}
	 * 
	 * The only meaningful difference is the following line:
	 * 
	 * <pre>
	 * ILaunchConfigurationType configType = getLaunchManager().getLaunchConfigurationType(
	 * 		JUnitLaunchConfigurationConstants.ID_JUNIT_APPLICATION);
	 * </pre>
	 */
	@SuppressWarnings("restriction")
	protected static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IJavaElement element)
			throws CoreException {
		final String testName;
		final String mainTypeQualifiedName;
		final String containerHandleId;

		switch (element.getElementType()) {
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
		case IJavaElement.PACKAGE_FRAGMENT: {
			String name = JavaElementLabels.getTextLabel(element, JavaElementLabels.ALL_FULLY_QUALIFIED);
			containerHandleId = element.getHandleIdentifier();
			mainTypeQualifiedName = EMPTY_STRING;
			testName = name.substring(name.lastIndexOf(IPath.SEPARATOR) + 1);
		}
			break;
		case IJavaElement.TYPE: {
			containerHandleId = EMPTY_STRING;
			mainTypeQualifiedName = ((IType) element).getFullyQualifiedName('.'); // don't replace, fix for binary inner types
			testName = element.getElementName();
		}
			break;
		case IJavaElement.METHOD: {
			IMethod method = (IMethod) element;
			containerHandleId = EMPTY_STRING;
			mainTypeQualifiedName = method.getDeclaringType().getFullyQualifiedName('.');
			testName = method.getDeclaringType().getElementName() + '.' + method.getElementName();
		}
			break;
		default:
			throw new IllegalArgumentException(
					"Invalid element type to create a launch configuration: " + element.getClass().getName()); //$NON-NLS-1$
		}

		String testKindId = TestKindRegistry.getContainerTestKindId(element);

		ILaunchConfigurationType configType = getLaunchManager().getLaunchConfigurationType(
				JUnitLaunchConfigurationConstants.ID_JUNIT_APPLICATION);
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
				getLaunchManager().generateUniqueLaunchConfigurationNameFrom(testName));

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeQualifiedName);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, element.getJavaProject().getElementName());
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_KEEPRUNNING, false);
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER, containerHandleId);
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND, testKindId);
		JUnitMigrationDelegate.mapResources(wc);
		AssertionVMArg.setArgDefault(wc);
		if (element instanceof IMethod) {
			// only set for methods
			wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_METHOD_NAME, element.getElementName());
		}
		return wc;
	}

	private class BuildContentProvider implements ITreeContentProvider {

		private List<BambooBuild> allBuilds;

		public void dispose() {
		}

		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		public Object[] getElements(Object inputElement) {
			allBuilds = new ArrayList<BambooBuild>();
			boolean hasFailed = false;
			for (Collection<BambooBuild> collection : builds.values()) {
				allBuilds.addAll(collection);
				for (BambooBuild build : collection) {
					if (build.getStatus() == BuildStatus.FAILURE) {
						hasFailed = true;
					}
				}
			}
			updateViewIcon(hasFailed);
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
		}
	}

	private static final String CODE_HAS_CHANGED = "Code has changed";

	private static final String BAMBOO_BUILD_LOG_CONSOLE = "Bamboo Build Log for ";

	public static final String ID = "com.atlassian.connector.eclipse.bamboo.ui.plans";

	private TreeViewer buildViewer;

	private Map<TaskRepository, Collection<BambooBuild>> builds;

	final Image buildFailedImage = CommonImages.getImage(BambooImages.STATUS_FAILED);

	final Image buildPassedImage = CommonImages.getImage(BambooImages.STATUS_PASSED);

	final Image buildDisabledImage = CommonImages.getImage(BambooImages.STATUS_DISABLED);

	private final Image bambooImage = CommonImages.getImage(BambooImages.BAMBOO);

	private Image currentTitleImage = bambooImage;

	private final Map<BambooBuild, MessageConsole> buildLogConsoles;

	private Link link;

	private StackLayout stackLayout;

	private Composite treeComp;

	private Composite linkComp;

	private HashMap<String, TaskRepository> linkedRepositories;

	private BaseSelectionListenerAction openInBrowserAction;

	private IWorkbenchAction refreshAction;

	private BaseSelectionListenerAction showBuildLogAction;

	private BaseSelectionListenerAction showTestResultsAction;

	private BaseSelectionListenerAction addLabelToBuildAction;

	private BaseSelectionListenerAction addCommentToBuildAction;

	private BaseSelectionListenerAction runBuildAction;

	private Action repoConfigAction;

	private IWorkbenchAction openRepoConfigAction;

	private BuildsChangedListener buildsChangedListener;

	private IStatusLineManager statusLineManager;

	private ToggleAutoRefreshAction toggleAutoRefreshAction;

	private OpenBambooEditorAction openBambooEditorAction;

	public BambooView() {
		builds = new HashMap<TaskRepository, Collection<BambooBuild>>();
		buildLogConsoles = new HashMap<BambooBuild, MessageConsole>();
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite stackComp = new Composite(parent, SWT.NONE);
		stackLayout = new StackLayout();
		stackComp.setLayout(stackLayout);
		treeComp = new Composite(stackComp, SWT.NONE);
		treeComp.setLayout(new FillLayout());
		linkComp = new Composite(stackComp, SWT.NONE);
		linkComp.setLayout(new GridLayout());

		createLink(linkComp);

		createTreeViewer(treeComp);

		stackLayout.topControl = linkComp;
		stackComp.layout();

		createActions();
		fillTreeContextMenu();
		contributeToActionBars();

		IWorkbenchSiteProgressService progress = (IWorkbenchSiteProgressService) getSite().getAdapter(
				IWorkbenchSiteProgressService.class);
		if (progress != null) {
			progress.showBusyForFamily(BambooConstants.FAMILY_REFRESH_OPERATION);
		}

		buildsChangedListener = new BuildsChangedListener() {
			public void buildsUpdated(final BuildsChangedEvent event) {
				builds = event.getAllBuilds();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						refresh(event.isForcedRefresh(), event.isFailed());
					}
				});
			}
		};
		BuildPlanManager buildPlanManager = BambooCorePlugin.getBuildPlanManager();
		buildPlanManager.addBuildsChangedListener(buildsChangedListener);
		//if the initial synchronization is already finished, get the cache data
		if (buildPlanManager.isFirstScheduledSynchronizationDone()) {
			builds = buildPlanManager.getSubscribedBuilds();
			refresh(false, false);
		}
	}

	@Override
	public void dispose() {
		if (buildsChangedListener != null) {
			BambooCorePlugin.getBuildPlanManager().removeBuildsChangedListener(buildsChangedListener);
			buildsChangedListener = null;
		}
	}

	private void createTreeViewer(Composite parent) {
		buildViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		buildViewer.setContentProvider(new BuildContentProvider());
		buildViewer.setUseHashlookup(true);

		TreeViewerColumn column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Build");
		column.getColumn().setWidth(300);
		column.setLabelProvider(new TreeColumnViewerLabelProvider(new DecoratingLabelProvider(new BuildLabelProvider(),
				PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator())));

		column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Status");
		column.getColumn().setWidth(350);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof BambooBuild) {
					BambooBuild build = ((BambooBuild) element);
					StringBuilder builder = new StringBuilder();
					int totalTests = build.getTestsFailed() + build.getTestsPassed();
					try {
						build.getNumber();
					} catch (UnsupportedOperationException e) {
						return ("N/A");
					}
					if (totalTests == 0) {
						builder.append("Tests: Testless build");
					} else {
						builder.append(NLS.bind("Tests: {0} out of {1} failed", new Object[] { build.getTestsFailed(),
								totalTests }));
					}
					if (build.getReason() != null) {
						builder.append("  [");
						builder.append(build.getReason());

						if (build.getReason().equals(CODE_HAS_CHANGED) && build.getCommiters() != null) {
							builder.append(" by ");
							builder.append(StringUtils.join(build.getCommiters(), ", "));
						}
						builder.append("]");
					}
					return builder.toString();
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
					try {
						build.getNumber();
					} catch (UnsupportedOperationException e) {
						return ("N/A");
					}
					return DateUtil.getRelativeBuildTime(build.getCompletionDate());
				}
				return super.getText(element);
			}
		});

		final BambooBuildViewerComparator comparator = new BambooBuildViewerComparator();

		final Tree tree = buildViewer.getTree();
		tree.getColumns()[0].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SortOrder sortOrder = comparator.toggleSortOrder();
				buildViewer.setComparator(null);
				buildViewer.setComparator(comparator);
				tree.setSortDirection(sortOrder.getDirection());
			}
		});
		tree.setSortColumn(tree.getColumns()[0]);

		buildViewer.setComparator(comparator);

		buildViewer.setInput(builds);

		tree.setHeaderVisible(true);

		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				new OpenInBrowserAction().run();
			}
		});
	}

	private void createLink(Composite parent) {
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		link = new Link(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalIndent = 5;
		gridData.verticalIndent = 5;
		link.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		link.setLayoutData(gridData);
		link.setText("Initializing view...");
		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String link = event.text;
				if (link.equals(CREATE_A_NEW_REPOSITORY_LINK)) {
					new RepositoryConfigurationAction().run();
				} else if (linkedRepositories != null) {
					TaskRepository repository = linkedRepositories.get(link);
					if (repository != null) {
						new OpenRepositoryConfigurationAction(repository, buildViewer).run();
					}
				} else if (link.equals(OPEN_REPOSITORY_VIEW_LINK)) {
					Display.getDefault().asyncExec(new Runnable() {
						@SuppressWarnings("restriction")
						public void run() {
							try {
								getSite().getPage().showView(TaskRepositoriesView.ID);
							} catch (PartInitException e) {
								StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
										"Failed to show Task Repositories View"));
							}
						}
					});
				}
			}
		});
	}

	private void fillLink(Set<TaskRepository> repositories) {
		if (repositories == null || repositories.isEmpty()) {
			link.setText(NLS.bind("No Bamboo repositories defined, <a>{0}</a>...", CREATE_A_NEW_REPOSITORY_LINK));
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append("No subscriptions to Bamboo build plans. ");
			builder.append("<a>");
			builder.append(OPEN_REPOSITORY_VIEW_LINK);
			builder.append("</a>... to configure subscriptions or <a>");
			builder.append(CREATE_A_NEW_REPOSITORY_LINK);
			builder.append("</a>...");
			link.setText(builder.toString());
		}
		link.getParent().layout();
	}

	private void fillTreeContextMenu() {
		MenuManager contextMenuManager = new MenuManager("BAMBOO");
		contextMenuManager.add(openBambooEditorAction);
		contextMenuManager.add(openInBrowserAction);
		contextMenuManager.add(new Separator());
		contextMenuManager.add(showBuildLogAction);
		contextMenuManager.add(showTestResultsAction);
		contextMenuManager.add(new Separator());
		contextMenuManager.add(runBuildAction);
		contextMenuManager.add(addLabelToBuildAction);
		contextMenuManager.add(addCommentToBuildAction);
		contextMenuManager.add(new Separator());
		contextMenuManager.add(refreshAction);
		contextMenuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		contextMenuManager.add(new Separator());
		contextMenuManager.add(openRepoConfigAction);
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
		bars.getMenuManager().add(toggleAutoRefreshAction);
	}

	private void fillToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(repoConfigAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(refreshAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(openInBrowserAction);
		toolBarManager.add(showBuildLogAction);
		toolBarManager.add(showTestResultsAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(runBuildAction);
		toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void createActions() {
		Action refreshLocalAction = new Action() {
			@Override
			public void run() {
				refreshBuilds();
			}
		};
		refreshAction = ActionFactory.REFRESH.create(getSite().getWorkbenchWindow());
		refreshAction.setImageDescriptor(CommonImages.REFRESH);

		openInBrowserAction = new OpenInBrowserAction();
		openInBrowserAction.setEnabled(false);
		openInBrowserAction.setText("Open with Browser");
		openInBrowserAction.setImageDescriptor(CommonImages.BROWSER_SMALL);
		buildViewer.addSelectionChangedListener(openInBrowserAction);

		showBuildLogAction = new ShowBuildLogsAction();
		showBuildLogAction.setText("Show Build Log");
		showBuildLogAction.setImageDescriptor(BambooImages.CONSOLE);
		showBuildLogAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(showBuildLogAction);

		showTestResultsAction = new ShowTestResultsAction();
		showTestResultsAction.setText("Show Test Results");
		showTestResultsAction.setImageDescriptor(BambooImages.JUNIT);
		showTestResultsAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(showTestResultsAction);

		addLabelToBuildAction = new AddLabelToBuildAction();
		addLabelToBuildAction.setText("Add Label to Build...");
		addLabelToBuildAction.setImageDescriptor(BambooImages.LABEL);
		addLabelToBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(addLabelToBuildAction);

		addCommentToBuildAction = new AddCommentToBuildAction();
		addCommentToBuildAction.setText("Add Comment to Build...");
		addCommentToBuildAction.setImageDescriptor(BambooImages.COMMENT);
		addCommentToBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(addCommentToBuildAction);

		runBuildAction = new RunBuildAction();
		runBuildAction.setText("Run Build");
		runBuildAction.setImageDescriptor(BambooImages.RUN_BUILD);
		runBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(runBuildAction);

		repoConfigAction = new RepositoryConfigurationAction();
		repoConfigAction.setText("Add Bamboo Repository...");
		repoConfigAction.setImageDescriptor(BambooImages.ADD_REPOSITORY);
		repoConfigAction.setMenuCreator((IMenuCreator) repoConfigAction);

		toggleAutoRefreshAction = new ToggleAutoRefreshAction();

		OpenRepositoryConfigurationAction openRepoConfigLocalAction = new OpenRepositoryConfigurationAction(buildViewer);
		openRepoConfigAction = ActionFactory.PROPERTIES.create(getSite().getWorkbenchWindow());
		openRepoConfigAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(openRepoConfigLocalAction);

		openBambooEditorAction = new OpenBambooEditorAction(this.buildViewer);
		openBambooEditorAction.setText("Open");

		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), openRepoConfigLocalAction);
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshLocalAction);
	}

	private void refresh(boolean forcedRefresh, boolean failed) {
		boolean hasSubscriptions = false;
		for (Collection<BambooBuild> repoBuilds : builds.values()) {
			if (repoBuilds.size() > 0) {
				hasSubscriptions = true;
				break;
			}
		}
		boolean isTreeShown = stackLayout.topControl == treeComp;
		if (hasSubscriptions && !isTreeShown) {
			stackLayout.topControl = treeComp;
			treeComp.getParent().layout();
		} else if (!hasSubscriptions) { //refresh link widget even if it is already shown to display updated repositories
			fillLink(TasksUi.getRepositoryManager().getRepositories(BambooCorePlugin.CONNECTOR_KIND));
			stackLayout.topControl = linkComp;
			linkComp.getParent().layout();
		}
		statusLineManager = getViewSite().getActionBars().getStatusLineManager();
		if (failed) {
			if (forcedRefresh) {
				statusLineManager.setErrorMessage(CommonImages.getImage(CommonImages.WARNING),
						"Error while refreshing build plans. See Error log for details.");
			} else {
				statusLineManager.setErrorMessage(CommonImages.getImage(CommonImages.WARNING),
						"Error while refreshing build plans. Retry by manually invoking a refresh in the view's toolbar.");
			}
		} else {
			statusLineManager.setErrorMessage(null);
			statusLineManager.setMessage("Last Refresh: " + new SimpleDateFormat("MMM d, H:mm:ss").format(new Date()));
		}

		buildViewer.setInput(builds);
		buildViewer.refresh(true);
	}

	private void fillPopupMenu(IMenuManager menuManager) {
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
		BambooCorePlugin.getBuildPlanManager().refreshAllBuilds();
	}
}
