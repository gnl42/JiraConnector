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
import com.atlassian.connector.eclipse.internal.bamboo.core.client.model.BambooCachedPlan;
import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonsUiUtil;
import org.eclipse.mylyn.internal.provisional.commons.ui.ICoreRunnable;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Wizard page for configuring a Bamboo repository.
 * 
 * @author Shawn Minto
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
				client.validate(monitor);
			} finally {
				if (client != null) {
					clientManager.deleteTempClient(client);
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

	public BambooRepositorySettingsPage(TaskRepository taskRepository) {
		super("Bamboo Repository Settings", "Enter Bamboo server information", taskRepository);
		setNeedsHttpAuth(true);
		setNeedsEncoding(false);
		setNeedsAnonymousLogin(false);
		setNeedsAdvanced(false);
	}

	@Override
	public void applyTo(TaskRepository repository) {
		// ignore
		super.applyTo(repository);
		Object[] items = planViewer.getCheckedElements();
		Collection<SubscribedPlan> plans = new ArrayList<SubscribedPlan>(items.length);
		for (Object item : items) {
			if (item instanceof BambooPlan) {
				plans.add(new SubscribedPlan(((BambooPlan) item).getPlanKey()));
			}
		}
		BambooUtil.setSubcribedPlans(repository, plans);
		//update cache
		updateAndWriteCache();
	}

	@Override
	protected void createAdditionalControls(Composite parent) {
		addRepositoryTemplatesToServerUrlCombo();
	}

	@Override
	protected void createContributionControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).minSize(SWT.DEFAULT,
				BUILD_PLAN_VIEWER_HEIGHT).applyTo(composite);
		composite.setLayout(new GridLayout(2, false));

		planViewer = new CheckboxTreeViewer(composite, SWT.V_SCROLL | SWT.BORDER);
		planViewer.setContentProvider(new BuildPlanContentProvider());
		planViewer.setLabelProvider(new BambooLabelProvider());
		setCachedPlanInput();
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(planViewer.getControl());

		Button refreshButton = new Button(composite, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(refreshButton);
		refreshButton.setText("Refresh");
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				refreshBuildPlans();
			}
		});
	}

	private void setCachedPlanInput() {
		BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
		BambooClient client = clientManager.getClient(repository);
		clientManager.readCache();
		BambooClientData data = client.getClientData();
		updateUIRestoreState(new Object[0], data);
	}

	private void updateAndWriteCache() {
		BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
		BambooClient client = clientManager.getClient(repository);
		BambooClientData data = client.getClientData();
		for (Object obj : planViewer.getCheckedElements()) {
			BambooCachedPlan checkedPlan = (BambooCachedPlan) obj;
			for (BambooCachedPlan cachedPlan : data.getPlans()) {
				if (checkedPlan.equals(cachedPlan)) {
					cachedPlan.setSubscribed(true);
				}
			}
		}
		BambooCorePlugin.getRepositoryConnector().getClientManager().writeCache();
	}

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

	private void refreshBuildPlans() {
		try {
			final TaskRepository repository = new TaskRepository(connector.getConnectorKind(), getRepositoryUrl());
			applyTo(repository);

			// preserve ui state
			Object[] checkedElements = planViewer.getCheckedElements();

			// update configuration
			final BambooClientData[] data = new BambooClientData[1];
			CommonsUiUtil.run(getContainer(), new ICoreRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
					BambooClient client = null;
					try {
						client = clientManager.getClient(repository);
						data[0] = client.updateRepositoryData(monitor);
					} finally {
						if (client != null) {
							clientManager.deleteTempClient(client);
						}
					}
				}
			});

			// update ui and restore state
			if (data[0] != null) {
				updateUIRestoreState(checkedElements, data[0]);
			}
		} catch (CoreException e) {
			CommonsUiUtil.setMessage(this, e.getStatus());
		} catch (OperationCanceledException e) {
			// ignore
		}
	}

	private void updateUIRestoreState(Object[] checkedElements, final BambooClientData data) {
		boolean initialize = planViewer.getInput() == null;
		Collection<BambooCachedPlan> plans = data.getPlans();
		if (plans != null) {
			planViewer.setInput(plans);
			if (initialize && getRepository() != null) {
				Set<SubscribedPlan> subscribedPlans = new HashSet<SubscribedPlan>(
						BambooUtil.getSubscribedPlans(getRepository()));
				for (BambooCachedPlan plan : plans) {
					if (plan.isSubscribed() || subscribedPlans.contains(new SubscribedPlan(plan.getKey()))) {
						planViewer.setChecked(plan, true);
					}
				}
			} else {
				for (BambooCachedPlan plan : plans) {
					if (plan.isSubscribed()) {
						planViewer.setChecked(plan, true);
					}
				}
				for (Object plan : checkedElements) {
					planViewer.setChecked(plan, true);
				}
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
