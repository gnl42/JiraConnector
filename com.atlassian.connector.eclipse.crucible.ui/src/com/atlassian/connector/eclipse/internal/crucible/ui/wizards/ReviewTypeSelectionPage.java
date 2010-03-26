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

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.wizards.ReviewWizard.Type;
import com.atlassian.connector.eclipse.team.ui.AtlassianTeamUiPlugin;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * Page for selecting which kind of review to create
 * 
 * @author Thomas Ehrnhoefer
 */
public class ReviewTypeSelectionPage extends WizardSelectionPage {

	private Button patchReview;

	private Button changesetReview;

	private Button workspacePatchReview;

	private final TaskRepository taskRepository;

	private final CrucibleUiPlugin plugin;

	public ReviewTypeSelectionPage(TaskRepository taskRepository) {
		super("crucibleSelection"); //$NON-NLS-1$
		setTitle("Select type of review to create");
		setDescription("Select which kind of review you want to create.");
		this.taskRepository = taskRepository;
		this.plugin = CrucibleUiPlugin.getDefault();
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).create());

		new Label(composite, SWT.NONE).setText("Select how you want to add files to the review:");

		Composite buttonComp = new Composite(composite, SWT.NONE);
		buttonComp.setLayout(GridLayoutFactory.fillDefaults().margins(10, 5).create());

		changesetReview = new Button(buttonComp, SWT.CHECK);
		changesetReview.setText("From a Changeset");
		changesetReview.setSelection(plugin.getPreviousChangesetReviewSelection());
		changesetReview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				plugin.setPreviousChangesetReviewSelection(changesetReview.getSelection());
			}
		});

		patchReview = new Button(buttonComp, SWT.CHECK);
		patchReview.setText("From a Patch");
		patchReview.setSelection(plugin.getPreviousPatchReviewSelection());
		patchReview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				plugin.setPreviousPatchReviewSelection(patchReview.getSelection());
			}
		});

		workspacePatchReview = new Button(buttonComp, SWT.CHECK);
		workspacePatchReview.setText("From Workspace Changes");
		workspacePatchReview.setSelection(plugin.getPreviousWorkspacePatchReviewSelection());
		workspacePatchReview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				plugin.setPreviousWorkspacePatchReviewSelection(workspacePatchReview.getSelection());
			}
		});

		// check if this is a Crucible 2.x repository, if so, we don't need SCM integration
		CrucibleVersionInfo versionInfo = CrucibleUiUtil.getCrucibleVersionInfo(taskRepository);
		if (versionInfo == null) {
			CrucibleUiUtil.updateTaskRepositoryCache(taskRepository, getContainer(), ReviewTypeSelectionPage.this);
			versionInfo = CrucibleUiUtil.getCrucibleVersionInfo(taskRepository);
		}

		Set<ITeamUiResourceConnector> teamConnectors = AtlassianTeamUiPlugin.getDefault()
				.getTeamResourceManager()
				.getTeamConnectors();

		if (teamConnectors.size() == 0) {
			if (versionInfo == null || !versionInfo.isVersion2OrGreater()) {
				disablePostCommits();
			}
			disablePreCommits();
			showScmRelatedWarning(
					buttonComp,
					"You don't have any SCM integration installed for Atlassian Connector for Eclipse. "
							+ "You need to install at least Subversion or Perforce integration to be able to create reviews. "
							+ "<A href=\"http://confluence.atlassian.com/display/IDEPLUGIN/Installing+the+Eclipse+Connector\">"
							+ "Check installation guide for details</A>. "
							+ "\n\nIf you need <A href=\"https://studio.atlassian.com/browse/PLE-728\">CVS</A>"
							+ " integration help us prioritize our backlog by voting for it.");
		} else {
			final int[] repoCount = { 0 };
			final IRunnableWithProgress getRepositories = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					repoCount[0] = TeamUiUtils.getRepositories(monitor).size();
				}
			};
			try {
				getContainer().run(true, true, getRepositories);
			} catch (InvocationTargetException e) {
				StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID,
						"Failed to retrieve repositories", e));
			} catch (InterruptedException e) {
				StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID,
						"Failed to retrieve repositories", e));
			}
			if (repoCount[0] == 0) {
				if (versionInfo == null || !versionInfo.isVersion2OrGreater()) {
					disablePostCommits();
				}
				disablePreCommits();
				showScmRelatedWarning(
						buttonComp,
						"In order to create regular pre- or post-commit review you need to have at least one "
								+ "supported SCM repository configured in your Eclipse workspace.\n"
								+ "If you use SVN, make sure that your \"SVN Repositories\" View contain at least one entry.");
			}
		}

		GridDataFactory.fillDefaults().grab(true, true).applyTo(buttonComp);
		setControl(composite);

		setSelectedNode(new IWizardNode() {
			private ReviewWizard wizard;

			public boolean isContentCreated() {
				// re-create this wizard every time
				return false;
			}

			public IWizard getWizard() {
				dispose();
				wizard = new ReviewWizard(taskRepository, getTypes());
				return wizard;
			}

			public Point getExtent() {
				return null;
			}

			public void dispose() {
				if (wizard != null) {
					wizard.dispose();
				}
			}
		});

	}

	private void disablePreCommits() {
		workspacePatchReview.setSelection(false);
		workspacePatchReview.setEnabled(false);
	}

	private void disablePostCommits() {
		changesetReview.setSelection(false);
		changesetReview.setEnabled(false);
	}

	private void showScmRelatedWarning(Composite parent, String infoMessage) {
		Link missingTeamConnectors = new Link(parent, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, true).hint(250, SWT.DEFAULT).applyTo(missingTeamConnectors);
		missingTeamConnectors.setText(infoMessage);
		final SelectionAdapter openInBrowserListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				WorkbenchUtil.openUrl(e.text, IWorkbenchBrowserSupport.AS_EXTERNAL);
			}
		};
		missingTeamConnectors.addSelectionListener(openInBrowserListener);
	}

	public Set<Type> getTypes() {
		Set<Type> types = new HashSet<Type>();
		if (patchReview.getSelection()) {
			types.add(Type.ADD_PATCH);
		}
		if (changesetReview.getSelection()) {
			types.add(Type.ADD_CHANGESET);
		}
		if (workspacePatchReview.getSelection()) {
			types.add(Type.ADD_WORKSPACE_PATCH);
		}
		return types;
	}

}
