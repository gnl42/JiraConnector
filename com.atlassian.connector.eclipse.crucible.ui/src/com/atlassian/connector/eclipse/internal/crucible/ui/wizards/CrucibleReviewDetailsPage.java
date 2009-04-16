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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedProject;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedUser;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleProjectsContentProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleProjectsLabelProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserContentProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserLabelProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.ReviewersSelectionTreePart;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Page for entering details for the new crucible review
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleReviewDetailsPage extends WizardPage {
	private final TaskRepository repository;

	private final ReviewBean newReview;

	private ComboViewer authorComboViewer;

	private ComboViewer projectsComboViewer;

	private ComboViewer moderatorComboViewer;

	private Text titleText;

	private Text objectivesText;

	private ReviewersSelectionTreePart reviewersSelectionTreePart;

	private Button anyoneCanJoin;

	public CrucibleReviewDetailsPage(TaskRepository repository) {
		super("crucibleDetails"); //$NON-NLS-1$
		Assert.isNotNull(repository);
		setTitle("New Crucible Review");
		setDescription("Please enter the details of the review.");
		this.repository = repository;
		newReview = new ReviewBean(repository.getUrl());
	}

	@Override
	public void setVisible(final boolean visible) {
		//check if cached data is available, if not, start background process to fetch it
		if (visible && !CrucibleUiUtil.hasCachedData(repository)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					// ignore
					updateCache();
				}
			});
		} else if (visible) {
			//preselect
			preselectDefaultUsers();
		}
		super.setVisible(visible);
	}

	private void preselectDefaultUsers() {
		Set<CrucibleCachedProject> cachedProjects = CrucibleUiUtil.getCachedProjects(repository);
		projectsComboViewer.setInput(cachedProjects);
		Set<CrucibleCachedUser> cachedUsers = CrucibleUiUtil.getCachedUsers(repository);
		moderatorComboViewer.setInput(cachedUsers);
		authorComboViewer.setInput(cachedUsers);
		CrucibleCachedUser currentUser = CrucibleUiUtil.getCurrentCachedUser(repository);
		if (currentUser != null) {
			if (newReview.getAuthor() == null) {
				newReview.setAuthor(currentUser.createUserFromCachedUser());
				authorComboViewer.setSelection(new StructuredSelection(currentUser));
			} else {
				authorComboViewer.setSelection(new StructuredSelection(new CrucibleCachedUser(newReview.getAuthor())));
			}
			if (newReview.getModerator() == null) {
				newReview.setModerator(currentUser.createUserFromCachedUser());
				moderatorComboViewer.setSelection(new StructuredSelection(currentUser));
			} else {
				moderatorComboViewer.setSelection(new StructuredSelection(new CrucibleCachedUser(
						newReview.getModerator())));
			}
			if (newReview.getCrucibleProject() != null) {
				projectsComboViewer.setSelection(new StructuredSelection(new CrucibleCachedProject(
						newReview.getCrucibleProject())));
			}
		}
	}

	private void updateCache() {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				CrucibleRepositoryConnector connector = CrucibleCorePlugin.getRepositoryConnector();
				CrucibleClient client = connector.getClientManager().getClient(repository);
				if (client != null) {
					try {
						client.updateRepositoryData(monitor, repository);
					} catch (CoreException e) {
						StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"Failed to update repository data", e));
					}
				}
			}
		};
		try {
			getContainer().run(true, true, runnable);
		} catch (Exception ex) {
			StatusHandler.log(new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID, "Failed to update repository data",
					ex));
		}
		if (!CrucibleUiUtil.hasCachedData(repository)) {
			setErrorMessage("Could not retrieve available projects and users from server.");
		}
		preselectDefaultUsers();
		reviewersSelectionTreePart.updateInput();
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(6).create());

		new Label(composite, SWT.NONE).setText("Title:");
		titleText = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(5, 1).grab(true, false).applyTo(titleText);

		new Label(composite, SWT.NONE).setText("Project:");
		final CCombo projectsCombo = new CCombo(composite, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(projectsCombo);
		projectsCombo.setEditable(false);
		projectsComboViewer = new ComboViewer(projectsCombo);
		projectsComboViewer.setLabelProvider(new CrucibleProjectsLabelProvider());
		projectsComboViewer.setContentProvider(new CrucibleProjectsContentProvider());
		Set<CrucibleCachedProject> cachedProjects = CrucibleUiUtil.getCachedProjects(repository);
		projectsComboViewer.setInput(cachedProjects);
		projectsComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object firstElement = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (firstElement != null) {
					newReview.setProject(((CrucibleCachedProject) firstElement).createProjectBeanFromCachedProject());
					newReview.setProjectKey(newReview.getCrucibleProject().getKey());
					getWizard().getContainer().updateButtons();
				}
			}
		});

		new Label(composite, SWT.NONE).setText("Moderator:");
		CCombo moderatorCombo = new CCombo(composite, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(moderatorCombo);
		moderatorCombo.setEditable(false);
		moderatorComboViewer = new ComboViewer(moderatorCombo);
		moderatorComboViewer.setLabelProvider(new CrucibleUserLabelProvider());
		moderatorComboViewer.setContentProvider(new CrucibleUserContentProvider());
		Set<CrucibleCachedUser> cachedUsers = CrucibleUiUtil.getCachedUsers(repository);
		moderatorComboViewer.setInput(cachedUsers);
		CrucibleCachedUser currentUser = CrucibleUiUtil.getCurrentCachedUser(repository);
		if (currentUser != null) {
			moderatorComboViewer.setSelection(new StructuredSelection(currentUser));
		}
		moderatorComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object firstElement = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (firstElement != null) {
					newReview.setModerator(((CrucibleCachedUser) firstElement).createUserFromCachedUser());
					getWizard().getContainer().updateButtons();
				}
			}
		});

		new Label(composite, SWT.NONE).setText("Author:");
		final CCombo authorCombo = new CCombo(composite, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(authorCombo);
		authorCombo.setEditable(false);
		authorComboViewer = new ComboViewer(authorCombo);
		authorComboViewer.setLabelProvider(new CrucibleUserLabelProvider());
		authorComboViewer.setContentProvider(new CrucibleUserContentProvider());
		authorComboViewer.setInput(cachedUsers);
		if (currentUser != null) {
			authorComboViewer.setSelection(new StructuredSelection(currentUser));
		}
		authorComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object firstElement = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (firstElement != null) {
					newReview.setAuthor(((CrucibleCachedUser) firstElement).createUserFromCachedUser());
					getWizard().getContainer().updateButtons();
				}
			}
		});

		Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).span(6, 1).applyTo(label);

		label = new Label(composite, SWT.NONE);
		label.setText("Objectives:");
		GridDataFactory.fillDefaults().span(4, 1).applyTo(label);

		label = new Label(composite, SWT.NONE);
		label.setText("Reviewers:");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		objectivesText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, true).span(4, 2).hint(-1, -1).applyTo(objectivesText);

		reviewersSelectionTreePart = new ReviewersSelectionTreePart(newReview);
		Composite reviewersComp = reviewersSelectionTreePart.createControl(composite);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).hint(-1, 150).applyTo(reviewersComp);
		reviewersSelectionTreePart.setCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				getWizard().getContainer().updateButtons();
			}
		});

		anyoneCanJoin = new Button(composite, SWT.CHECK);
		anyoneCanJoin.setText("Allow anyone to join");
		GridDataFactory.fillDefaults().indent(5, SWT.DEFAULT).span(2, 1).applyTo(anyoneCanJoin);

		Button updateData = new Button(composite, SWT.PUSH);
		updateData.setText("Update Repository Data");
		GridDataFactory.fillDefaults().span(6, 1).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(updateData);
		updateData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateCache();
			}
		});

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	@Override
	public boolean isPageComplete() {
		return newReview != null && hasRequiredFields() && hasValidReviewers();
	}

	private boolean hasRequiredFields() {
		setErrorMessage(null);
		if (newReview.getProjectKey() == null) {
			setErrorMessage("Please select a project");
			return false;
		}
		if (newReview.getModerator() == null) {
			setErrorMessage("Please select a moderator");
			return false;
		}
		if (newReview.getAuthor() == null) {
			setErrorMessage("Please select an author");
			return false;
		}
		return true;
	}

	private boolean hasValidReviewers() {
		setErrorMessage(null);
		for (Reviewer reviewer : reviewersSelectionTreePart.getSelectedReviewers()) {
			if (newReview.getAuthor().getUserName().equals(reviewer.getUserName())) {
				setErrorMessage("The author might not be a reviewer as well.");
				return false;
			}
			if (newReview.getModerator().getUserName().equals(reviewer.getUserName())) {
				setErrorMessage("The moderator might not be a reviewer as well.");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	public void applyTo() {
		newReview.setName(titleText.getText());
		newReview.setDescription(objectivesText.getText());
		newReview.setReviewers(reviewersSelectionTreePart.getSelectedReviewers());
		newReview.setAllowReviewerToJoin(anyoneCanJoin.getEnabled());
		newReview.setCreator(CrucibleUiUtil.getCurrentCachedUser(repository).createUserFromCachedUser());
	}

	public ReviewBean getReview() {
		return newReview;
	}

	public Set<Reviewer> getReviewers() {
		return reviewersSelectionTreePart.getSelectedReviewers();
	}

}
