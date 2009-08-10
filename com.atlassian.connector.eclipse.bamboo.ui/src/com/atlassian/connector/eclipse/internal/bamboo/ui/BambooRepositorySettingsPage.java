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

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooClientManager;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClient;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClientData;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.internal.provisional.commons.ui.ICoreRunnable;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Wizard page for configuring a Bamboo repository.
 * 
 * @author Shawn Minto
 * @author thomas
 */
public class BambooRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private class BambooValidator extends Validator {

		private final TaskRepository taskRepository;

		public BambooValidator(TaskRepository taskRepository) {
			this.taskRepository = taskRepository;
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
			BambooClient client = null;
			try {
				client = clientManager.createTempClient(taskRepository, new BambooClientData());

				monitor = Policy.backgroundMonitorFor(monitor);
				client.validate(monitor, taskRepository);
			} finally {
				if (client != null) {
					clientManager.deleteTempClient(client.getServerData());
				}
			}
		}
	}

	private class BuildPlanContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		public Object[] getElements(Object inputElement) {
			return ((Collection<?>) inputElement).toArray();
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

	private static final int BUILD_PLAN_VIEWER_HEIGHT = 100;

	private CheckboxTreeViewer planViewer;

	private boolean validSettings;

	private boolean initialized;

	public BambooRepositorySettingsPage(TaskRepository taskRepository) {
		super("Bamboo Repository Settings", "Enter Bamboo server information", taskRepository);
		setNeedsHttpAuth(true);
		setNeedsEncoding(false);
		setNeedsAnonymousLogin(false);
		setNeedsAdvanced(false);
	}

	@Override
	public void applyTo(final TaskRepository repository) {
		this.repository = applyToValidate(repository);
		Object[] items = planViewer.getCheckedElements();
		Collection<SubscribedPlan> plans = new ArrayList<SubscribedPlan>(items.length);
		for (Object item : items) {
			if (item instanceof BambooPlan) {
				plans.add(new SubscribedPlan(((BambooPlan) item).getKey()));
			}
		}
		BambooUtil.setSubcribedPlans(this.repository, plans);
		//update cache
		//updateAndWriteCache();
		BambooCorePlugin.getBuildPlanManager().buildSubscriptionsChanged(this.repository);
	}

	/**
	 * Helper method for distinguishing between hitting Finish and Validate (because Validation leads to calling applyTo
	 * in the superclass)
	 */
	public TaskRepository applyToValidate(TaskRepository repository) {
		super.applyTo(repository);
		return repository;
	}

	@Override
	public TaskRepository createTaskRepository() {
		TaskRepository repository = new TaskRepository(connector.getConnectorKind(), getRepositoryUrl());
		return applyToValidate(repository);
	}

	@Override
	protected void createAdditionalControls(Composite parent) {
		addRepositoryTemplatesToServerUrlCombo();
	}

	@Override
	protected void createContributionControls(Composite parent) {
		// don't call the super method since the Bamboo connector does not take advantage of the tasks UI extensions

		ExpandableComposite section = createSection(parent, "Build Plans");
		section.setExpanded(true);
		if (section.getLayoutData() instanceof GridData) {
			GridData gd = ((GridData) section.getLayoutData());
			gd.grabExcessVerticalSpace = true;
			gd.verticalAlignment = SWT.FILL;
		}

		Composite composite = new Composite(section, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		composite.setLayout(layout);
		section.setClient(composite);

		planViewer = new CheckboxTreeViewer(composite, SWT.V_SCROLL | SWT.BORDER);
		planViewer.setContentProvider(new BuildPlanContentProvider());
		planViewer.setLabelProvider(new BambooLabelProvider());
		setCachedPlanInput();
		int height = convertVerticalDLUsToPixels(BUILD_PLAN_VIEWER_HEIGHT);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).hint(SWT.DEFAULT, height).applyTo(
				planViewer.getControl());

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(buttonComposite);
		RowLayout buttonLayout = new RowLayout(SWT.VERTICAL);
		buttonLayout.fill = true;
		buttonComposite.setLayout(buttonLayout);

		Button selectFavorites = new Button(buttonComposite, SWT.PUSH);
		selectFavorites.setText("&Favourites");
		selectFavorites.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				Object input = planViewer.getInput();
				if (input instanceof Collection<?>) {
					List<BambooPlan> favorites = new ArrayList<BambooPlan>();
					for (BambooPlan plan : (Collection<BambooPlan>) input) {
						if (plan.isFavourite()) {
							favorites.add(plan);
						}
					}
					planViewer.setCheckedElements(favorites.toArray());
				}
			}
		});

		Button selectAllButton = new Button(buttonComposite, SWT.PUSH);
		selectAllButton.setText("&Select All");
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Object input = planViewer.getInput();
				if (input instanceof Collection<?>) {
					planViewer.setCheckedElements(((Collection<?>) input).toArray());
				}
			}
		});

		Button deselectAllButton = new Button(buttonComposite, SWT.PUSH);
		deselectAllButton.setText("&Deselect All");
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				planViewer.setCheckedElements(new Object[0]);
			}
		});

		Button refreshButton = new Button(buttonComposite, SWT.PUSH);
		refreshButton.setText("Refresh");
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				validateSettings();
			}
		});
	}

	private void setCachedPlanInput() {
		if (repository != null) {
			BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
			BambooClient client = clientManager.getClient(repository);
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
	protected Validator getValidator(TaskRepository repository) {
		return new BambooValidator(repository);
	}

	@Override
	protected boolean isValidUrl(String name) {
		if (name.startsWith(URL_PREFIX_HTTPS) || name.startsWith(URL_PREFIX_HTTP)) {
			try {
				new URL(name);
				return true;
			} catch (MalformedURLException e) {
				// ignore
			}
		}
		return false;
	}

	@Override
	protected void applyValidatorResult(Validator validator) {
		this.validSettings = validator != null && validator.getStatus() == Status.OK_STATUS;
		super.applyValidatorResult(validator);
	}

	@Override
	protected void validateSettings() {
		super.validateSettings();
		if (validSettings) {
			refreshBuildPlans();
		}
	}

	private void refreshBuildPlans() {
		try {
			final TaskRepository repository = createTaskRepository();

			// preserve ui state
			Object[] checkedElements = planViewer.getCheckedElements();

			// update configuration
			final BambooClientData[] data = new BambooClientData[1];
			CommonUiUtil.run(getContainer(), new ICoreRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
					final BambooClient client = clientManager.getClient(repository);
					data[0] = client.updateRepositoryData(monitor, repository);
				}
			});

			// update ui and restore state
			if (data[0] != null) {
				updateUIRestoreState(checkedElements, data[0]);
			}
		} catch (CoreException e) {
			CommonUiUtil.setMessage(this, e.getStatus());
		} catch (OperationCanceledException e) {
			// ignore
		}
	}

	private void updateUIRestoreState(Object[] checkedElements, final BambooClientData data) {
		Collection<BambooPlan> plans = data.getPlans();
		if (plans != null) {
			planViewer.setInput(plans);
			if (!initialized) {
				// if plans are empty this indicates a loss of configuration, the initialized flag is not set 
				// in this case do nothing to re-trigger initialization after he next refresh  
				if (plans.size() > 0) {
					initialized = true;
					if (getRepository() != null) {
						// restore selection from repository
						Set<SubscribedPlan> subscribedPlans = new HashSet<SubscribedPlan>(
								BambooUtil.getSubscribedPlans(getRepository()));
						for (BambooPlan plan : plans) {
							if (subscribedPlans.contains(new SubscribedPlan(plan.getKey()))) {
								planViewer.setChecked(plan, true);
							}
						}
					} else {
						// new repository: select favorite plan by default
						for (BambooPlan plan : plans) {
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
	protected void repositoryTemplateSelected(RepositoryTemplate template) {
		repositoryLabelEditor.setStringValue(template.label);
		setUrl(template.repositoryUrl);
		getContainer().updateButtons();
	}

}
