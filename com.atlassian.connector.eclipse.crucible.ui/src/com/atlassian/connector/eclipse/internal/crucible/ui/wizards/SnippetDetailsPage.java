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
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleProjectsLabelProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserLabelProvider;
import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewType;
import com.atlassian.theplugin.commons.crucible.api.model.User;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * Page for entering details for the new crucible review
 * 
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 */
public class SnippetDetailsPage extends WizardPage {
	private static final String ENTER_THE_DETAILS_OF_THE_REVIEW = "Enter the details of the review.";

	private final TaskRepository taskRepository;

	private ComboViewer authorComboViewer;

	private ComboViewer projectsComboViewer;

	private Text titleText;

	private boolean firstTimeCheck = true;

	public SnippetDetailsPage(TaskRepository repository) {
		super("crucibleDetails"); //$NON-NLS-1$
		Assert.isNotNull(repository);
		setTitle("New Crucible Snippet Review");
		setDescription(ENTER_THE_DETAILS_OF_THE_REVIEW);
		this.taskRepository = repository;
	}

	private boolean firstTime = true;

	@Override
	public void setVisible(final boolean visible) {
		// check if cached data is available, if not, start background process to fetch it
		if (visible && firstTime) {
			firstTime = false;
			if (!CrucibleUiUtil.hasCachedData(taskRepository)) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						CrucibleUiUtil.updateTaskRepositoryCache(taskRepository, getContainer(),
								SnippetDetailsPage.this);
						setInputAndInitialSelections();
					}
				});
			} else {
				// preselect
				setInputAndInitialSelections();
			}
		}
		super.setVisible(visible);
		if (visible) {
			titleText.setFocus();
		}

	}

	private void setInputAndInitialSelections() {
		updateInput();

		final String lastSelectedProjectKey = CrucibleUiPlugin.getDefault().getLastSelectedProjectKey(taskRepository);
		final BasicProject lastSelectedProject = CrucibleUiUtil.getCachedProject(taskRepository, lastSelectedProjectKey);
		if (lastSelectedProject != null) {
			projectsComboViewer.setSelection(new StructuredSelection(lastSelectedProject));
		} else {
			if (projectsComboViewer.getElementAt(0) != null) {
				projectsComboViewer.setSelection(new StructuredSelection(projectsComboViewer.getElementAt(0)));
			}
		}

		User currentUser = CrucibleUiUtil.getCurrentCachedUser(taskRepository);
		if (currentUser != null) {
			authorComboViewer.setSelection(new StructuredSelection(currentUser));
		} else {
			if (authorComboViewer.getElementAt(0) != null) {
				authorComboViewer.setSelection(new StructuredSelection(authorComboViewer.getElementAt(0)));
			}
		}
	}

	private void updateInput() {
		Collection<BasicProject> cachedProjects = CrucibleUiUtil.getCachedProjects(taskRepository);
		projectsComboViewer.setInput(cachedProjects);

		Set<User> cachedUsers = CrucibleUiUtil.getCachedUsers(taskRepository);
		authorComboViewer.setInput(cachedUsers);
	}

	private void updateInputAndRestoreSelections() {
		BasicProject previousProject = (BasicProject) ((IStructuredSelection) projectsComboViewer.getSelection()).getFirstElement();
		User previousAuthor = (User) ((IStructuredSelection) authorComboViewer.getSelection()).getFirstElement();

		updateInput();

		if (previousProject != null) {
			projectsComboViewer.setSelection(new StructuredSelection(previousProject));
		}

		if (previousAuthor != null) {
			authorComboViewer.setSelection(new StructuredSelection(previousAuthor));
		}
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(6).margins(5, 5).create());

		new Label(composite, SWT.NONE).setText("Title:");
		titleText = new Text(composite, SWT.BORDER);
		titleText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().updateButtons();
			}
		});
		GridDataFactory.fillDefaults().span(5, 1).grab(true, false).applyTo(titleText);

		new Label(composite, SWT.NONE).setText("Project:");
		projectsComboViewer = new ComboViewer(composite);
		projectsComboViewer.setLabelProvider(new CrucibleProjectsLabelProvider());
		projectsComboViewer.setContentProvider(new ArrayContentProvider());
		projectsComboViewer.setSorter(new ViewerSorter());
		projectsComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				BasicProject project = getSelectedProject();
				CrucibleUiPlugin.getDefault().updateLastSelectedProject(taskRepository,
						project != null ? project.getKey() : null);
				getWizard().getContainer().updateButtons();
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(projectsComboViewer.getCombo());

		new Label(composite, SWT.NONE).setText("Author:");
		authorComboViewer = new ComboViewer(composite);
		authorComboViewer.setLabelProvider(new CrucibleUserLabelProvider());
		authorComboViewer.setContentProvider(new ArrayContentProvider());
		authorComboViewer.setComparator(new ViewerComparator());
		authorComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object firstElement = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (firstElement != null) {
					getWizard().getContainer().updateButtons();
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(authorComboViewer.getCombo());

		Button updateData = new Button(composite, SWT.PUSH);
		updateData.setText("Update Repository Data");
		GridDataFactory.fillDefaults().span(4, 2).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(updateData);
		updateData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CrucibleUiUtil.updateTaskRepositoryCache(taskRepository, getContainer(), SnippetDetailsPage.this);
				updateInputAndRestoreSelections();
			}
		});

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	@Override
	public boolean isPageComplete() {
		Review review = getReview();
		return review != null && hasRequiredFields(review);
	}

	private boolean hasRequiredFields(Review newReview) {
		setErrorMessage(null);
		String newMessage = null;

		if (getSelectedProject() == null) {
			newMessage = "Select a project";
		}
		if (getSelectedAuthor() == null) {
			newMessage = "Select an author";
		}
		if (newReview.getSummary() == null || newReview.getSummary().trim().length() == 0) {
			newMessage = "Enter a title for the review";
		}
		if (newMessage != null) {
			if (firstTimeCheck) {
				setMessage(newMessage);
			} else {
				setErrorMessage(newMessage);
			}
			return false;
		}
		setMessage(ENTER_THE_DETAILS_OF_THE_REVIEW);
		firstTimeCheck = false;
		return true;
	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	@Nullable
	public Review getReview() {
		final User author = getSelectedAuthor();
		final BasicProject project = getSelectedProject();

		if (project == null || project.getKey() == null) {
			return null;
		}

		Review review = new Review(ReviewType.SNIPPET, taskRepository.getUrl(), project.getKey(), author, null);

		if (titleText != null) {
			review.setName(titleText.getText());
			review.setSummary(titleText.getText());
		}

		review.setCreator(CrucibleUiUtil.getCurrentCachedUser(taskRepository));

		return review;
	}

	private User getSelectedAuthor() {
		User author = (authorComboViewer != null) ? (User) ((IStructuredSelection) authorComboViewer.getSelection()).getFirstElement()
				: null;
		return author;
	}

	@Nullable
	BasicProject getSelectedProject() {
		if (projectsComboViewer == null) {
			return null;
		}
		Object firstElement = ((IStructuredSelection) projectsComboViewer.getSelection()).getFirstElement();
		if (firstElement != null) {
			return (BasicProject) firstElement;
		}
		return null;
	}

}
