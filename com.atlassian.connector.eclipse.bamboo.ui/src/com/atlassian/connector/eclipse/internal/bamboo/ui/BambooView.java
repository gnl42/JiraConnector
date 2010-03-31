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
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.AddCommentToBuildAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.AddLabelToBuildAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.NewTaskFromFailedBuildAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.OpenBambooEditorAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.OpenRepositoryConfigurationAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.RepositoryConfigurationAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.RunBuildAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.ShowBuildLogAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.ShowTestResultsAction;
import com.atlassian.connector.eclipse.internal.bamboo.ui.actions.ToggleAutoRefreshAction;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.PlanState;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeColumnViewerLabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 */
public class BambooView extends ViewPart {

	private static final String CREATE_A_NEW_REPOSITORY_LINK = "create a new repository";

	private static final String OPEN_REPOSITORY_VIEW_LINK = "Open the Task Repositories view";

	private static final String REFRESH_BAMBOO_VIEW_LINK = "Refresh Bamboo View manually";

	private static final String ENABLE_AUTOMATIC_REFRESH = "Enable";

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
					if (selected instanceof EclipseBambooBuild) {
						String url = BambooUtil.getUrlFromBuild(((EclipseBambooBuild) selected).getBuild());
						TasksUiUtil.openUrl(url);
					}
				}
			}
		}

		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			if (selection.size() >= 1) {
				try {
					Iterator<?> it = selection.iterator();
					while (it.hasNext()) {
						((EclipseBambooBuild) it.next()).getBuild().getNumber();
					}
					return true;
				} catch (UnsupportedOperationException e) {
					// igonre
				}
			}
			return false;
		}
	}

	private class BuildContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		public Object[] getElements(Object inputElement) {
			return builds.toArray();
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private static final String CODE_HAS_CHANGED = "Code has changed";

	public static final String ID = "com.atlassian.connector.eclipse.bamboo.ui.plans";

	private enum ViewStatus {
		NONE, PASSED, FAILED, ERROR,
	};

	private BuildTreeViewer buildViewer;

	private Collection<EclipseBambooBuild> builds;

	final Image buildFailedImage = CommonImages.getImage(BambooImages.VIEW_STATUS_FAILED);

	final Image buildPassedImage = CommonImages.getImage(BambooImages.VIEW_STATUS_PASSED);

	final Image buildErrorImage = CommonImages.getImage(BambooImages.VIEW_STATUS_WARNING);

	private final Image bambooImage = CommonImages.getImage(BambooImages.BAMBOO);

	private Image currentTitleImage = bambooImage;

	private Link link;

	private StackLayout stackLayout;

	private Composite treeComp;

	private Composite linkComp;

	private HashMap<String, TaskRepository> linkedRepositories;

	private BaseSelectionListenerAction openInBrowserAction;

	private Action refreshAction;

	private BaseSelectionListenerAction showBuildLogAction;

	private BaseSelectionListenerAction showTestResultsAction;

	private BaseSelectionListenerAction addLabelToBuildAction;

	private BaseSelectionListenerAction addCommentToBuildAction;

	private BaseSelectionListenerAction runBuildAction;

	private BaseSelectionListenerAction newTaskFromFailedBuildAction;

	private Action repoConfigAction;

	private BaseSelectionListenerAction openRepoConfigAction;

	private BuildsChangedListener buildsChangedListener;

	private IStatusLineManager statusLineManager;

	private ToggleAutoRefreshAction toggleAutoRefreshAction;

	private OpenBambooEditorAction openBambooEditorAction;

	public BambooView() {
		builds = MiscUtil.buildArrayList();
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
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						builds = toEclipseBambooBuildCollection(event.getAllBuilds());
						refresh(event.isForcedRefresh(), event.isFailed());
					}
				});
			}
		};
		BuildPlanManager buildPlanManager = BambooCorePlugin.getBuildPlanManager();
		buildPlanManager.addBuildsChangedListener(buildsChangedListener);
		//if the initial synchronization is already finished, get the cache data
		if (buildPlanManager.isFirstScheduledSynchronizationDone()) {
			builds = toEclipseBambooBuildCollection(buildPlanManager.getSubscribedBuilds());
			refresh(false, false);
		}
	}

	private Collection<EclipseBambooBuild> toEclipseBambooBuildCollection(
			Map<TaskRepository, Collection<BambooBuild>> buildsPerTaskRepo) {
		Collection<EclipseBambooBuild> res = MiscUtil.buildArrayList();
		for (TaskRepository taskRepository : buildsPerTaskRepo.keySet()) {
			// we are checking offline mode here, as BuildPlanManager passes here also builds for disconnected repos
			// BuildPlanManager is so complicated (unnecessarily) that I don't dare now to refactor it  
			if (!taskRepository.isOffline()) {
				Collection<BambooBuild> bambooBuilds = buildsPerTaskRepo.get(taskRepository);
				for (BambooBuild bambooBuild : bambooBuilds) {
					res.add(new EclipseBambooBuild(bambooBuild, taskRepository));
				}
			}
		}
		return res;

	}

	@Override
	public void dispose() {
		if (buildsChangedListener != null) {
			BambooCorePlugin.getBuildPlanManager().removeBuildsChangedListener(buildsChangedListener);
			buildsChangedListener = null;
		}

		buildViewer.dispose();
	}

	private static class BambooColumnProvider extends ColumnLabelProvider {
		@Override
		public Font getFont(Object element) {
			return BambooUiUtil.getFontForBuildStatus(element);
		}
	}

	private void createTreeViewer(Composite parent) {
		buildViewer = new BuildTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		buildViewer.setContentProvider(new BuildContentProvider());
		buildViewer.setUseHashlookup(true);

		final TreeViewerColumn columnName = new TreeViewerColumn(buildViewer, SWT.NONE);
		columnName.getColumn().setText("Build");
		columnName.getColumn().setWidth(300);
		columnName.setLabelProvider(new TreeColumnViewerLabelProvider(new DecoratingLabelProvider(
				new BuildLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator())));

		TreeViewerColumn column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("Status");
		column.getColumn().setWidth(350);
		column.setLabelProvider(new BambooColumnProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EclipseBambooBuild) {
					BambooBuild build = ((EclipseBambooBuild) element).getBuild();
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
		column.setLabelProvider(new BambooColumnProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EclipseBambooBuild) {
					BambooBuild build = ((EclipseBambooBuild) element).getBuild();
					try {
						build.getNumber();
					} catch (UnsupportedOperationException e) {
						return ("N/A");
					}
					return DateUtil.getRelativePastDate(build.getCompletionDate());
				}
				return super.getText(element);
			}

		});

		column = new TreeViewerColumn(buildViewer, SWT.NONE);
		column.getColumn().setText("State");
		column.getColumn().setWidth(60);
		column.setLabelProvider(new BambooColumnProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EclipseBambooBuild) {
					final BambooBuild build = ((EclipseBambooBuild) element).getBuild();
					final PlanState planState = build.getPlanState();
					if (planState != null) {
						switch (planState) {
						case BUILDING:
							return "Building";
						case IN_QUEUE:
							return "In queue";
						}
					}
				}
				return null;
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
				if (openBambooEditorAction.isEnabled()) {
					openBambooEditorAction.run();
				}
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

		if (BambooCorePlugin.isAutoRefresh()) {
			link.setText("Initializing view...");
		} else {
			link.setText(NLS.bind("Automatic refresh is disabled. <a>{0}</a> it or <a>{1}</a>.",
					ENABLE_AUTOMATIC_REFRESH, REFRESH_BAMBOO_VIEW_LINK));
		}

		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String link = event.text;
				if (link.equals(REFRESH_BAMBOO_VIEW_LINK)) {
					refreshBuilds();
				} else if (link.equals(ENABLE_AUTOMATIC_REFRESH)) {
					PreferencesUtil.createPreferenceDialogOn(getSite().getShell(),
							"com.atlassian.connector.eclipse.bamboo.ui.preferences.BambooPreferencePage", null, null)
							.open();
				} else if (link.equals(CREATE_A_NEW_REPOSITORY_LINK)) {
					new RepositoryConfigurationAction().run();
				} else if (linkedRepositories != null) {
					TaskRepository repository = linkedRepositories.get(link);
					if (repository != null) {
						BaseSelectionListenerAction openRepAction = new OpenRepositoryConfigurationAction();
						openRepAction.selectionChanged(new StructuredSelection(repository));
						openRepAction.run();
					}
				} else if (link.equals(OPEN_REPOSITORY_VIEW_LINK)) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							try {
								getSite().getPage().showView(ITasksUiConstants.ID_VIEW_REPOSITORIES);
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
		if (link.isDisposed()) {
			return;
		}
		if (repositories == null || repositories.isEmpty()) {
			link.setText(NLS.bind("No Bamboo repositories defined, <a>{0}</a>...", CREATE_A_NEW_REPOSITORY_LINK));
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append("No subscriptions to Bamboo build plans. ");
			builder.append("<a>");
			builder.append(OPEN_REPOSITORY_VIEW_LINK);
			builder.append("</a> to configure subscriptions or <a>");
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
		contextMenuManager.add(newTaskFromFailedBuildAction);
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
		refreshAction = new Action() {
			@Override
			public void run() {
				refreshBuilds();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh all builds");
		refreshAction.setActionDefinitionId("org.eclipse.ui.file.refresh");
		refreshAction.setImageDescriptor(CommonImages.REFRESH);

		openInBrowserAction = new OpenInBrowserAction();
		openInBrowserAction.setEnabled(false);
		openInBrowserAction.setText(BambooConstants.OPEN_WITH_BROWSER_ACTION_LABEL);
		openInBrowserAction.setImageDescriptor(CommonImages.BROWSER_SMALL);
		buildViewer.addSelectionChangedListener(openInBrowserAction);

		showBuildLogAction = new ShowBuildLogAction();
		showBuildLogAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(showBuildLogAction);

		showTestResultsAction = new ShowTestResultsAction();
		showTestResultsAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(showTestResultsAction);

		addLabelToBuildAction = new AddLabelToBuildAction();
		addLabelToBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(addLabelToBuildAction);

		addCommentToBuildAction = new AddCommentToBuildAction();
		addCommentToBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(addCommentToBuildAction);

		newTaskFromFailedBuildAction = new NewTaskFromFailedBuildAction();
		newTaskFromFailedBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(newTaskFromFailedBuildAction);

		runBuildAction = new RunBuildAction(refreshAction);
		runBuildAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(runBuildAction);

		repoConfigAction = new RepositoryConfigurationAction();
		repoConfigAction.setMenuCreator((IMenuCreator) repoConfigAction);

		toggleAutoRefreshAction = new ToggleAutoRefreshAction();

		openRepoConfigAction = new OpenRepositoryConfigurationAction();
		openRepoConfigAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(openRepoConfigAction);

		openBambooEditorAction = new OpenBambooEditorAction();
		openBambooEditorAction.setEnabled(false);
		buildViewer.addSelectionChangedListener(openBambooEditorAction);

		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), openRepoConfigAction);
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
	}

	private void refresh(boolean forcedRefresh, boolean failed) {
		final boolean hasSubscriptions = !builds.isEmpty();
		final ViewStatus status = getMostSevereStatus(builds);
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

		updateViewIcon(status);

		buildViewer.setBuilds(builds);
		buildViewer.refresh(true);
	}

	private ViewStatus getViewStatus(BambooBuild build) {
		switch (build.getStatus()) {
		case FAILURE:
			return ViewStatus.FAILED;
		case SUCCESS:
			return ViewStatus.PASSED;
		}
		return ViewStatus.NONE;
	}

	private ViewStatus getMostSevereStatus(Collection<EclipseBambooBuild> repoBuilds) {
		ViewStatus status = ViewStatus.NONE;
		for (EclipseBambooBuild buildAdapter : repoBuilds) {
			BambooBuild build = buildAdapter.getBuild();
			if (build.getEnabled()) {
				final ViewStatus buildStatus = getViewStatus(build);
				if (buildStatus.ordinal() > status.ordinal()) {
					status = buildStatus;
				}
			}
		}
		return status;
	}

	private void updateViewIcon(ViewStatus status) {
		switch (status) {
		case PASSED:
			currentTitleImage = buildPassedImage;
			break;
		case FAILED:
			currentTitleImage = buildFailedImage;
			break;
		case ERROR:
			currentTitleImage = buildErrorImage;
			break;
		default:
			currentTitleImage = bambooImage;
		}
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

	private void fillPopupMenu(IMenuManager menuManager) {
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
