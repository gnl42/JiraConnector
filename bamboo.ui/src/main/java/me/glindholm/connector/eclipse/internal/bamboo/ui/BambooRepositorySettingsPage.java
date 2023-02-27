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

import static me.glindholm.connector.eclipse.internal.core.JiraConnectorCorePlugin.PLUGIN_ID;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.core.ICoreRunnable;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import me.glindholm.bamboo.model.RestInfo;
import me.glindholm.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import me.glindholm.connector.eclipse.internal.bamboo.core.BambooUtil;
import me.glindholm.connector.eclipse.internal.bamboo.core.PlanBranches;
import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClient;
import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClientData;
import me.glindholm.connector.eclipse.internal.bamboo.core.service.BambooLocalConfiguration;
import me.glindholm.connector.eclipse.internal.bamboo.ui.wizards.BambooTaskRepositoryLocation;
import me.glindholm.connector.eclipse.internal.commons.ui.MigrateToSecureStorageJob;
import me.glindholm.connector.eclipse.internal.commons.ui.dialogs.RemoteApiLockedDialog;
import me.glindholm.connector.eclipse.internal.core.client.BambooClientFactory;
import me.glindholm.theplugin.commons.bamboo.BambooPlan;
import me.glindholm.theplugin.commons.cfg.SubscribedPlan;
import me.glindholm.theplugin.commons.remoteapi.CaptchaRequiredException;
import me.glindholm.theplugin.commons.remoteapi.RemoteApiException;

/**
 * Wizard page for configuring a Bamboo repository.
 *
 * @author Shawn Minto
 * @author thomas
 * @author Jacek Jaroczynski
 */
public class BambooRepositorySettingsPage extends AbstractRepositorySettingsPage {

    private class BambooValidator extends Validator {

        private final TaskRepository repository;

        private RestInfo serverInfo;

        public RestInfo getServerInfo() {
            return serverInfo;
        }

        public String getRepositoryUrl() {
            return repository.getRepositoryUrl();
        }

        public BambooValidator(final TaskRepository taskRepository) {
            repository = taskRepository;
        }

        @Override
        public void run(final IProgressMonitor monitor) throws CoreException {
            try {
                new URL(repository.getRepositoryUrl());
            } catch (final MalformedURLException ex) {
                throw new CoreException(new Status(IStatus.ERROR, BambooUiPlugin.ID_PLUGIN, IStatus.OK, "Invalid URL", null));
            }

            final AbstractWebLocation location = new BambooTaskRepositoryLocation(repository);
            final BambooLocalConfiguration configuration = BambooUtil.getLocalConfiguration(repository);
            try {
                serverInfo = BambooClientFactory.getDefault().validateConnection(location, configuration, monitor);
            } catch (final CaptchaRequiredException e) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        new RemoteApiLockedDialog(WorkbenchUtil.getShell(), repository.getRepositoryUrl()).open();
                    }
                });
                throw new CoreException(RepositoryStatus.createStatus(repository.getRepositoryUrl(), IStatus.ERROR, BambooUiPlugin.ID_PLUGIN,
                        "Wrong credentials or you have been locked out"));
            } catch (final Exception e) {
                StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.ID_PLUGIN, e.getMessage(), e));
                throw new CoreException(BambooCorePlugin.toStatus(repository, e));
            }

            final MultiStatus status = new MultiStatus(BambooUiPlugin.ID_PLUGIN, 0, NLS.bind("Validation results for {0}", //$NON-NLS-1$
                    repository.getRepositoryLabel()), null);
            // status.addAll(serverInfo.getStatistics().getStatus());
            status.add(new Status(IStatus.INFO, BambooUiPlugin.ID_PLUGIN, NLS.bind("Web base: {0}", repository.getRepositoryUrl()))); //$NON-NLS-1$
            // status.add(new Status(IStatus.INFO, JiraUiPlugin.ID_PLUGIN, NLS.bind(
            // "Character encoding: {0}", serverInfo.getCharacterEncoding())));
            // //$NON-NLS-1$
            status.add(new Status(IStatus.INFO, BambooUiPlugin.ID_PLUGIN, NLS.bind("Version: {0}", serverInfo.toString()))); //$NON-NLS-1$
            StatusHandler.log(status);

        }
    }

    private class BuildPlanContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getChildren(final Object parentElement) {
            return new Object[0];
        }

        @Override
        public Object[] getElements(final Object inputElement) {
            return ((Collection<?>) inputElement).toArray();
        }

        @Override
        public Object getParent(final Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(final Object element) {
            return false;
        }

        @Override
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        }

    }

    private static final int BUILD_PLAN_VIEWER_HEIGHT = 100;

    private CheckboxTreeViewer planViewer;

    private boolean validSettings;

    private boolean initialized;

    private Button btnUseFavourites;

    private Button selectAllButton;

    private Button deselectAllButton;

    private Button refreshButton;

    private Collection<BambooPlan> allPlans;

    private Combo btnPlanBranches;

    private Label planBranchesExplanationLabel;

    public BambooRepositorySettingsPage(final TaskRepository taskRepository) {
        super("Bamboo Repository Settings", "Enter Bamboo server information", taskRepository);
        setNeedsHttpAuth(true);
        setNeedsEncoding(false);
        setNeedsAnonymousLogin(false);
        setNeedsAdvanced(false);
    }

    @Override
    public void applyTo(final TaskRepository repository) {
        this.repository = applyToValidate(repository);
        repository.setProperty(IRepositoryConstants.PROPERTY_CATEGORY, IRepositoryConstants.CATEGORY_BUILD);

        final BambooClient client = BambooClientFactory.getDefault().getBambooClient(repository);
        if (allPlans != null && client != null && client.getClientData() != null) {
            client.getClientData().setPlans(allPlans);
        }

        BambooUtil.setUseFavourites(repository, btnUseFavourites.getSelection());
        BambooUtil.setPlanBranches(repository, PlanBranches.from(btnPlanBranches.getText()));

        final Object[] items = planViewer.getCheckedElements();
        final Collection<SubscribedPlan> plans = new ArrayList<>(items.length);
        for (final Object item : items) {
            if (item instanceof BambooPlan) {
                plans.add(new SubscribedPlan(((BambooPlan) item).getKey()));
            }
        }
        BambooUtil.setSubcribedPlans(this.repository, plans);
        // update cache
        // updateAndWriteCache();
        BambooCorePlugin.getBuildPlanManager().buildSubscriptionsChanged(this.repository);
    }

    /**
     * Helper method for distinguishing between hitting Finish and Validate (because
     * Validation leads to calling applyTo in the superclass)
     */
    public TaskRepository applyToValidate(final TaskRepository repository) {
        MigrateToSecureStorageJob.migrateToSecureStorage(repository);
        super.applyTo(repository);
        return repository;
    }

    @Override
    public TaskRepository createTaskRepository() {
        final TaskRepository repository = new TaskRepository(connector.getConnectorKind(), getRepositoryUrl());
        return applyToValidate(repository);
    }

    @Override
    protected void createAdditionalControls(final Composite parent) {
        addRepositoryTemplatesToServerUrlCombo();
    }

    @Override
    protected void createContributionControls(final Composite parent) {
        // don't call the super method since the Bamboo connector does not take
        // advantage of the tasks UI extensions

        final ExpandableComposite section = createSection(parent, "Build Plans");
        section.setExpanded(true);
        if (section.getLayoutData() instanceof GridData) {
            final GridData gd = (GridData) section.getLayoutData();
            gd.grabExcessVerticalSpace = true;
            gd.verticalAlignment = SWT.FILL;
        }

        final Composite composite = new Composite(section, SWT.NONE);
        final GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.verticalSpacing = 10;
        composite.setLayout(layout);
        section.setClient(composite);

        btnUseFavourites = new Button(composite, SWT.CHECK);
        btnUseFavourites.setText("Use Favourite Builds for Server");
        GridDataFactory.fillDefaults().span(2, 1).indent(0, 5).applyTo(btnUseFavourites);

        planViewer = new CheckboxTreeViewer(composite, SWT.V_SCROLL | SWT.BORDER);
        planViewer.setContentProvider(new BuildPlanContentProvider());
        planViewer.setLabelProvider(new BambooLabelProvider());
        setCachedPlanInput();
        final int height = convertVerticalDLUsToPixels(BUILD_PLAN_VIEWER_HEIGHT);
        GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).hint(SWT.DEFAULT, height).applyTo(planViewer.getControl());

        final Composite buttonComposite = new Composite(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(buttonComposite);
        final RowLayout buttonLayout = new RowLayout(SWT.VERTICAL);
        buttonLayout.fill = true;
        buttonComposite.setLayout(buttonLayout);

        selectAllButton = new Button(buttonComposite, SWT.PUSH);
        selectAllButton.setText("&Select All");
        selectAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                Object input = planViewer.getInput();
                // if there are no plans, let's call validate first
                if (!(input instanceof Collection<?>)) {
                    validateSettings();
                }
                input = planViewer.getInput();
                if (input instanceof Collection<?>) {
                    planViewer.setCheckedElements(((Collection<?>) input).toArray());
                }
            }
        });

        deselectAllButton = new Button(buttonComposite, SWT.PUSH);
        deselectAllButton.setText("&Deselect All");
        deselectAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                planViewer.setCheckedElements(new Object[0]);
            }
        });

        refreshButton = new Button(buttonComposite, SWT.PUSH);
        refreshButton.setText("Refresh");
        refreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                validateSettings();
            }
        });

        final Composite planBranchesComposite = new Composite(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(planBranchesComposite);
        final RowLayout planBranchesLayout = new RowLayout(SWT.HORIZONTAL);
        planBranchesComposite.setLayout(planBranchesLayout);

        // labelComposite is used only to correctly position label
        final Composite labelComposite = new Composite(planBranchesComposite, SWT.NONE);
        RowDataFactory.swtDefaults().applyTo(labelComposite);
        final RowLayout labelLayout = new RowLayout();
        labelLayout.marginLeft = 0;
        labelComposite.setLayout(labelLayout);

        final Label planBranchesLabel = new Label(labelComposite, SWT.NONE);
        planBranchesLabel.setText("Show Plan Branches: ");

        btnPlanBranches = new Combo(planBranchesComposite, SWT.READ_ONLY);
        btnPlanBranches.setItems(PlanBranches.stringValues());
        btnPlanBranches.select(0);

        // labelComposite is used only to correctly position label
        final Composite labelComposite2 = new Composite(planBranchesComposite, SWT.NONE);
        RowDataFactory.swtDefaults().applyTo(labelComposite);
        final RowLayout labelLayout2 = new RowLayout();
        labelComposite2.setLayout(labelLayout2);

        planBranchesExplanationLabel = new Label(labelComposite2, SWT.NONE);
        planBranchesExplanationLabel.setText(" (shows only branches where I am the last commiter)");
        planBranchesExplanationLabel.setVisible(false);

        btnPlanBranches.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (PlanBranches.values()[btnPlanBranches.getSelectionIndex()] == PlanBranches.MINE) {
                    planBranchesExplanationLabel.setVisible(true);
                } else {
                    planBranchesExplanationLabel.setVisible(false);
                }
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
        });

        btnUseFavourites.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                favouritesSelected(btnUseFavourites.getSelection());
            }
        });

        restoreOldValues();
    }

    private void restoreOldValues() {
        if (BambooUtil.isUseFavourites(repository)) {
            btnUseFavourites.setSelection(true);
            favouritesSelected(true);
        }

        final PlanBranches planBranches = BambooUtil.getPlanBranches(repository);
        btnPlanBranches.select(planBranches.ordinal());
        if (planBranches == PlanBranches.MINE) {
            planBranchesExplanationLabel.setVisible(true);
        }
    }

    private void favouritesSelected(final boolean enabled) {
        planViewer.getControl().setEnabled(!enabled);
        selectAllButton.setEnabled(!enabled);
        deselectAllButton.setEnabled(!enabled);
        refreshButton.setEnabled(!enabled);
    }

    private void setCachedPlanInput() {
        if (repository != null) {
            final BambooClient client = BambooClientFactory.getDefault().getBambooClient(repository);
            updateUIRestoreState(new Object[0], client.getClientData());
        }
    }

//	private void updateAndWriteCache() {
//		BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
//		BambooClient client = clientManager.getClient(repository);
//		BambooClientData data = client.getClientData();
//		for (BambooPlan cachedPlan : data.getPlans()) {
//			cachedPlan.setSubscribed(false);
//			for (Object obj : planViewer.getCheckedElements()) {
//				BambooPlan checkedPlan = (BambooPlan) obj;
//				if (checkedPlan.equals(cachedPlan)) {
//					cachedPlan.setSubscribed(true);
//				}
//			}
//		}
//		BambooCorePlugin.getRepositoryConnector().getClientManager().writeCache();
//	}

    @Override
    public String getConnectorKind() {
        return BambooCorePlugin.CONNECTOR_KIND;
    }

    @Override
    protected Validator getValidator(final TaskRepository repository) {
        return new BambooValidator(repository);
    }

    @Override
    protected boolean isValidUrl(final String name) {
        if (name.startsWith(URL_PREFIX_HTTPS) || name.startsWith(URL_PREFIX_HTTP)) {
            try {
                new URL(name);
                return true;
            } catch (final MalformedURLException e) {
                // ignore
            }
        }
        return false;
    }

    @Override
    protected void applyValidatorResult(final Validator validator) {
        validSettings = validator != null && validator.getStatus() == Status.OK_STATUS;
        super.applyValidatorResult(validator);
    }

    @Override
    protected void validateSettings() {
        if (repository != null) {
            final AuthenticationCredentials repoCredentials = repository.getCredentials(AuthenticationType.REPOSITORY);
            final AuthenticationCredentials proxyCredentials = repository.getCredentials(AuthenticationType.PROXY);
            final AuthenticationCredentials httpCredentials = repository.getCredentials(AuthenticationType.HTTP);

            super.validateSettings();
            if (validSettings && !btnUseFavourites.getSelection()) {
                refreshBuildPlans();
            }

            repository.setCredentials(AuthenticationType.REPOSITORY, repoCredentials, repository.getSavePassword(AuthenticationType.REPOSITORY));
            repository.setCredentials(AuthenticationType.HTTP, httpCredentials, repository.getSavePassword(AuthenticationType.HTTP));
            repository.setCredentials(AuthenticationType.PROXY, proxyCredentials, repository.getSavePassword(AuthenticationType.PROXY));
        } else {
            super.validateSettings();
            if (validSettings && !btnUseFavourites.getSelection()) {
                refreshBuildPlans();
            }
        }

    }

    private void refreshBuildPlans() {
        try {
            final TaskRepository repository = createTaskRepository();

            // preserve ui state
            final Object[] checkedElements = planViewer.getCheckedElements();

            // update configuration
            final BambooClientData[] data = new BambooClientData[1];
            CommonUiUtil.run(getContainer(), new ICoreRunnable() {

                @Override
                public void run(final IProgressMonitor monitor) throws CoreException {
                    final BambooClient client = BambooClientFactory.getDefault().getBambooClient(repository);
                    try {
                        final int i = 2;
                        data[0] = client.updateRepositoryData(monitor, repository);
                    } catch (final RemoteApiException e) {
                        throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, RepositoryStatus.ERROR_REPOSITORY_LOGIN, e.getMessage(), e)); // FIXME
                    }
                    final Collection<BambooPlan> plans = client.getClientData().getPlans();
                    rememberPlans(plans);
                }
            });

            // update ui and restore state
            if (data[0] != null) {
                updateUIRestoreState(checkedElements, data[0]);
            }
        } catch (final CoreException e) {
            CommonUiUtil.setMessage(this, e.getStatus());
        } catch (final OperationCanceledException e) {
            // ignore
        }
    }

    private void rememberPlans(final Collection<BambooPlan> plans) {
        allPlans = plans;
    }

    private void updateUIRestoreState(final Object[] checkedElements, final BambooClientData data) {
        final Collection<BambooPlan> plans = data.getPlans();
        if (plans != null) {
            planViewer.setInput(plans);
            if (!initialized) {
                // if plans are empty this indicates a loss of configuration, the initialized
                // flag is not set
                // in this case do nothing to re-trigger initialization after he next refresh
                if (plans.size() > 0) {
                    initialized = true;
                    if (getRepository() != null) {
                        // restore selection from repository
                        final Set<SubscribedPlan> subscribedPlans = new HashSet<>(BambooUtil.getSubscribedPlans(getRepository()));
                        for (final BambooPlan plan : plans) {
                            if (subscribedPlans.contains(new SubscribedPlan(plan.getKey()))) {
                                planViewer.setChecked(plan, true);
                            }
                        }
                    } else {
                        // new repository: select favorite plan by default
                        for (final BambooPlan plan : plans) {
                            if (plan.isFavourite()) {
                                planViewer.setChecked(plan, true);
                            }
                        }
                    }
                }
            } else {
//				for (BambooPlan plan : plans) {
//					if (plan.isSubscribed()) {
//						planViewer.setChecked(plan, true);
//					}
//				}
//				for (Object plan : checkedElements) {
//					planViewer.setChecked(plan, true);
//				}
                planViewer.setCheckedElements(checkedElements);
            }
        }
    }

    @Override
    protected void repositoryTemplateSelected(final RepositoryTemplate template) {
        repositoryLabelEditor.setStringValue(template.label);
        setUrl(template.repositoryUrl);
        getContainer().updateButtons();
    }

}
