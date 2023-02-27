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

package me.glindholm.connector.eclipse.internal.bamboo.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.mylyn.commons.ui.CommonImages;
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

import me.glindholm.connector.eclipse.internal.bamboo.core.BambooConstants;
import me.glindholm.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import me.glindholm.connector.eclipse.internal.bamboo.core.BambooUtil;
import me.glindholm.connector.eclipse.internal.bamboo.core.BuildPlanManager;
import me.glindholm.connector.eclipse.internal.bamboo.core.BuildsChangedEvent;
import me.glindholm.connector.eclipse.internal.bamboo.core.BuildsChangedListener;
import me.glindholm.connector.eclipse.internal.bamboo.ui.BambooBuildViewerComparator.SortOrder;
import me.glindholm.connector.eclipse.internal.bamboo.ui.actions.NewTaskFromFailedBuildAction;
import me.glindholm.connector.eclipse.internal.bamboo.ui.actions.OpenRepositoryConfigurationAction;
import me.glindholm.connector.eclipse.internal.bamboo.ui.actions.RepositoryConfigurationAction;
import me.glindholm.connector.eclipse.internal.bamboo.ui.actions.RunBuildAction;
import me.glindholm.connector.eclipse.internal.bamboo.ui.actions.ShowTestResultsAction;
import me.glindholm.connector.eclipse.internal.bamboo.ui.actions.ToggleAutoRefreshAction;
import me.glindholm.theplugin.commons.bamboo.BambooBuild;
import me.glindholm.theplugin.commons.bamboo.PlanState;
import me.glindholm.theplugin.commons.util.DateUtil;
import me.glindholm.theplugin.commons.util.MiscUtil;

/**
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 * @author Jacek Jaroczynski
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
            final ISelection s = buildViewer.getSelection();
            if (s instanceof IStructuredSelection) {
                final IStructuredSelection selection = (IStructuredSelection) s;
                for (final Object selected : selection) {
                    if (selected instanceof EclipseBambooBuild) {
                        final String url = BambooUtil.getUrlFromBuild(((EclipseBambooBuild) selected).getBuild());
                        TasksUiUtil.openUrl(url);
                    }
                }
            }
        }

        @Override
        protected boolean updateSelection(final IStructuredSelection selection) {
            if (selection.size() >= 1) {
                try {
                    final Iterator<?> it = selection.iterator();
                    while (it.hasNext()) {
                        ((EclipseBambooBuild) it.next()).getBuild().getNumber();
                    }
                    return true;
                } catch (final UnsupportedOperationException e) {
                    // igonre
                }
            }
            return false;
        }
    }

    private class BuildContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getChildren(final Object parentElement) {

            final List<EclipseBambooBuild> children = new ArrayList<>(builds.size());

            if (parentElement instanceof EclipseBambooBuild) {
                final EclipseBambooBuild build = (EclipseBambooBuild) parentElement;

                for (final EclipseBambooBuild b : builds) {
                    if (build.getBuild().getPlanKey().equals(b.getBuild().getMasterPlanKey())) {
                        children.add(b);
                    }
                }

            }

            return children.toArray();
        }

        @Override
        public Object[] getElements(final Object inputElement) {

            final List<EclipseBambooBuild> ret = new ArrayList<>(builds.size());

            // select and return top level builds
            for (final EclipseBambooBuild build : builds) {
                if (build.getBuild().getMasterPlanKey() == null || build.getBuild().getMasterPlanKey().length() == 0) {
                    ret.add(build);
                }
            }

            return ret.toArray();
        }

        @Override
        public Object getParent(final Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(final Object element) {
            if (element instanceof EclipseBambooBuild) {
                final EclipseBambooBuild build = (EclipseBambooBuild) element;

                for (final EclipseBambooBuild b : builds) {
                    if (build.getBuild().getPlanKey().equals(b.getBuild().getMasterPlanKey())) {
                        return true;
                    }
                }

            }
            return false;
        }

        @Override
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        }

    }

    private static final String CODE_HAS_CHANGED = "Code has changed";

    public static final String ID = "me.glindholm.connector.eclipse.bamboo.ui.plans";

    private enum ViewStatus {
        NONE, PASSED, FAILED, ERROR,
    }

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

    // private BaseSelectionListenerAction showBuildLogAction;

    private BaseSelectionListenerAction showTestResultsAction;

    private BaseSelectionListenerAction runBuildAction;

    private BaseSelectionListenerAction newTaskFromFailedBuildAction;

    private Action repoConfigAction;

    private BaseSelectionListenerAction openRepoConfigAction;

    private BuildsChangedListener buildsChangedListener;

    private IStatusLineManager statusLineManager;

    private ToggleAutoRefreshAction toggleAutoRefreshAction;

    public BambooView() {
        builds = new ArrayList<>();
    }

    @Override
    public void createPartControl(final Composite parent) {
        final Composite stackComp = new Composite(parent, SWT.NONE);
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

        final IWorkbenchSiteProgressService progress = getSite().getAdapter(IWorkbenchSiteProgressService.class);
        if (progress != null) {
            progress.showBusyForFamily(BambooConstants.FAMILY_REFRESH_OPERATION);
        }

        buildsChangedListener = new BuildsChangedListener() {
            @Override
            public void buildsUpdated(final BuildsChangedEvent event) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        builds = toEclipseBambooBuildCollection(event.getAllBuilds());
                        refresh(event.isForcedRefresh(), event.isFailed());
                    }
                });
            }
        };
        final BuildPlanManager buildPlanManager = BambooCorePlugin.getBuildPlanManager();
        buildPlanManager.addBuildsChangedListener(buildsChangedListener);
        // if the initial synchronization is already finished, get the cache data
        if (buildPlanManager.isFirstScheduledSynchronizationDone()) {
            builds = toEclipseBambooBuildCollection(buildPlanManager.getSubscribedBuilds());
            refresh(false, false);
        }
    }

    private Collection<EclipseBambooBuild> toEclipseBambooBuildCollection(final Map<TaskRepository, Collection<BambooBuild>> buildsPerTaskRepo) {
        final Collection<EclipseBambooBuild> res = MiscUtil.buildArrayList();
        for (final TaskRepository taskRepository : buildsPerTaskRepo.keySet()) {
            // we are checking offline mode here, as BuildPlanManager passes here also
            // builds for disconnected repos
            // BuildPlanManager is so complicated (unnecessarily) that I don't dare now to
            // refactor it
            if (!taskRepository.isOffline()) {
                final Collection<BambooBuild> bambooBuilds = buildsPerTaskRepo.get(taskRepository);
                for (final BambooBuild bambooBuild : bambooBuilds) {
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
        public Font getFont(final Object element) {
            return BambooUiUtil.getFontForBuildStatus(element);
        }
    }

    private void createTreeViewer(final Composite parent) {
        buildViewer = new BuildTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        buildViewer.setContentProvider(new BuildContentProvider());
        buildViewer.setUseHashlookup(true);

        final TreeViewerColumn columnName = new TreeViewerColumn(buildViewer, SWT.NONE);
        columnName.getColumn().setText("Build");
        columnName.getColumn().setWidth(300);
        columnName.setLabelProvider(new TreeColumnViewerLabelProvider(
                new DecoratingLabelProvider(new BuildLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator())));

        TreeViewerColumn column = new TreeViewerColumn(buildViewer, SWT.NONE);
        column.getColumn().setText("Status");
        column.getColumn().setWidth(350);
        column.setLabelProvider(new BambooColumnProvider() {
            @Override
            public String getText(final Object element) {
                if (element instanceof EclipseBambooBuild) {
                    final BambooBuild build = ((EclipseBambooBuild) element).getBuild();
                    final StringBuilder builder = new StringBuilder();
                    final int totalTests = build.getTestsFailed() + build.getTestsPassed();
                    try {
                        build.getNumber();
                    } catch (final UnsupportedOperationException e) {
                        return "N/A";
                    }
                    if (totalTests == 0) {
                        builder.append("Tests: Testless build");
                    } else {
                        if (build.getTestsFailed() > 0) {
                            builder.append(NLS.bind("Tests: {0} out of {1} failed", new Object[] { build.getTestsFailed(), totalTests }));
                        } else {
                            builder.append(NLS.bind("Tests: All {0} tests passed", new Object[] { totalTests }));
                        }
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
            public String getText(final Object element) {
                if (element instanceof EclipseBambooBuild) {
                    final BambooBuild build = ((EclipseBambooBuild) element).getBuild();
                    try {
                        build.getNumber();
                    } catch (final UnsupportedOperationException e) {
                        return "N/A";
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
            public String getText(final Object element) {
                if (element instanceof EclipseBambooBuild) {
                    final BambooBuild build = ((EclipseBambooBuild) element).getBuild();
                    final PlanState planState = build.getPlanState();
                    if (planState != null) {
                        switch (planState) {
                        case BUILDING:
                            return "Building";
                        case IN_QUEUE:
                            return "In queue";
                        case STANDING: // FIXME What to do?
                            break;
                        default: // FIXME What to do?
                            break;
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
            public void widgetSelected(final SelectionEvent e) {
                final SortOrder sortOrder = comparator.toggleSortOrder();
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
            public void widgetDefaultSelected(final SelectionEvent e) {
                if (openInBrowserAction.isEnabled()) {
                    openInBrowserAction.run();
                }
            }
        });
    }

    private void createLink(final Composite parent) {
        parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        link = new Link(parent, SWT.NONE);
        final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalIndent = 5;
        gridData.verticalIndent = 5;
        link.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        link.setLayoutData(gridData);

        if (BambooCorePlugin.isAutoRefresh()) {
            link.setText("Initializing view...");
        } else {
            link.setText(NLS.bind("Automatic refresh is disabled. <a>{0}</a> it or <a>{1}</a>.", ENABLE_AUTOMATIC_REFRESH, REFRESH_BAMBOO_VIEW_LINK));
        }

        link.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                final String link = event.text;
                if (link.equals(REFRESH_BAMBOO_VIEW_LINK)) {
                    refreshBuilds();
                } else if (link.equals(ENABLE_AUTOMATIC_REFRESH)) {
                    PreferencesUtil.createPreferenceDialogOn(getSite().getShell(), "me.glindholm.connector.eclipse.bamboo.ui.preferences.BambooPreferencePage",
                            null, null).open();
                } else if (link.equals(CREATE_A_NEW_REPOSITORY_LINK)) {
                    new RepositoryConfigurationAction().run();
                } else if (linkedRepositories != null) {
                    final TaskRepository repository = linkedRepositories.get(link);
                    if (repository != null) {
                        final BaseSelectionListenerAction openRepAction = new OpenRepositoryConfigurationAction();
                        openRepAction.selectionChanged(new StructuredSelection(repository));
                        openRepAction.run();
                    }
                } else if (link.equals(OPEN_REPOSITORY_VIEW_LINK)) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getSite().getPage().showView(ITasksUiConstants.ID_VIEW_REPOSITORIES);
                            } catch (final PartInitException e) {
                                StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.ID_PLUGIN, "Failed to show Task Repositories View"));
                            }
                        }
                    });
                }
            }
        });
    }

    private void fillLink(final Set<TaskRepository> repositories) {
        if (link.isDisposed()) {
            return;
        }
        if (repositories == null || repositories.isEmpty()) {
            link.setText(NLS.bind("No Bamboo repositories defined, <a>{0}</a>...", CREATE_A_NEW_REPOSITORY_LINK));
        } else {
            final StringBuilder builder = new StringBuilder();
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
        final MenuManager contextMenuManager = new MenuManager("BAMBOO");
        contextMenuManager.add(openInBrowserAction);
        contextMenuManager.add(new Separator());
        // contextMenuManager.add(showBuildLogAction);
        contextMenuManager.add(showTestResultsAction);
        contextMenuManager.add(new Separator());
        contextMenuManager.add(runBuildAction);
        contextMenuManager.add(newTaskFromFailedBuildAction);
        contextMenuManager.add(new Separator());
        contextMenuManager.add(refreshAction);
        contextMenuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        contextMenuManager.add(new Separator());
        contextMenuManager.add(openRepoConfigAction);
        final Menu contextMenu = contextMenuManager.createContextMenu(buildViewer.getControl());
        buildViewer.getControl().setMenu(contextMenu);
        getSite().registerContextMenu(contextMenuManager, buildViewer);
    }

    @Override
    public void setFocus() {
        // ignore

    }

    private void contributeToActionBars() {
        final IActionBars bars = getViewSite().getActionBars();
        fillPopupMenu(bars.getMenuManager());
        fillToolBar(bars.getToolBarManager());
        bars.getMenuManager().add(toggleAutoRefreshAction);
    }

    private void fillToolBar(final IToolBarManager toolBarManager) {
        toolBarManager.add(repoConfigAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(refreshAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(openInBrowserAction);
        // toolBarManager.add(showBuildLogAction);
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

        // showBuildLogAction = new ShowBuildLogAction();
        // showBuildLogAction.setEnabled(false);
        // buildViewer.addSelectionChangedListener(showBuildLogAction);

        showTestResultsAction = new ShowTestResultsAction();
        showTestResultsAction.setEnabled(false);
        buildViewer.addSelectionChangedListener(showTestResultsAction);

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

        final IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), openRepoConfigAction);
        actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
    }

    private void refresh(final boolean forcedRefresh, final boolean failed) {
        final boolean hasSubscriptions = !builds.isEmpty();
        final ViewStatus status = getMostSevereStatus(builds);
        final boolean isTreeShown = stackLayout.topControl == treeComp;
        if (hasSubscriptions && !isTreeShown) {
            stackLayout.topControl = treeComp;
            treeComp.getParent().layout();
        } else if (!hasSubscriptions) { // refresh link widget even if it is already shown to display updated
                                        // repositories
            fillLink(TasksUi.getRepositoryManager().getRepositories(BambooCorePlugin.CONNECTOR_KIND));
            stackLayout.topControl = linkComp;
            linkComp.getParent().layout();
        }
        statusLineManager = getViewSite().getActionBars().getStatusLineManager();
        if (failed) {
            if (forcedRefresh) {
                statusLineManager.setErrorMessage(CommonImages.getImage(CommonImages.WARNING),
                        "Error while refreshing build plans. See Error Log for details.");
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

    private ViewStatus getViewStatus(final BambooBuild build) {
        if (build.getStatus() != null) { // FIXME Until we can solve the NPE
            switch (build.getStatus()) {
            case FAILURE:
                return ViewStatus.FAILED;
            case SUCCESS:
                return ViewStatus.PASSED;
            case UNKNOWN: // FIXME What to do?
                break;
            default: // FIXME What to do?
                break;
            }
        }
        return ViewStatus.NONE;
    }

    private ViewStatus getMostSevereStatus(final Collection<EclipseBambooBuild> repoBuilds) {
        ViewStatus status = ViewStatus.NONE;
        for (final EclipseBambooBuild buildAdapter : repoBuilds) {
            final BambooBuild build = buildAdapter.getBuild();
            if (build.getEnabled()) {
                final ViewStatus buildStatus = getViewStatus(build);
                if (buildStatus.ordinal() > status.ordinal()) {
                    status = buildStatus;
                }
            }
        }
        return status;
    }

    private void updateViewIcon(final ViewStatus status) {
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

    private void fillPopupMenu(final IMenuManager menuManager) {
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
