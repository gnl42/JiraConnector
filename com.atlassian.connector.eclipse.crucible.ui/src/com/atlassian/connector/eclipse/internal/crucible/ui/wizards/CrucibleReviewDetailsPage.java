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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleProjectsLabelProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserLabelProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.ReviewersSelectionTreePart;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Page for entering details for the new crucible review
 * 
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 */
public class CrucibleReviewDetailsPage extends WizardPage {
	private static final String ENTER_THE_DETAILS_OF_THE_REVIEW = "Enter the details of the review.";

	private final TaskRepository taskRepository;

	private ComboViewer authorComboViewer;

	private ComboViewer projectsComboViewer;

	private ComboViewer moderatorComboViewer;

	private Text titleText;

	private Text objectivesText;

	private ReviewersSelectionTreePart reviewersSelectionTreePart;

	private Button anyoneCanJoin;

	private Button startReview;

	private boolean firstTimeCheck = true;

	private final boolean addComment;

	private Text commentText;

	public CrucibleReviewDetailsPage(TaskRepository repository) {
		this(repository, false);
	}

	public CrucibleReviewDetailsPage(TaskRepository repository, boolean addComment) {
		super("crucibleDetails"); //$NON-NLS-1$
		this.addComment = addComment;
		Assert.isNotNull(repository);
		setTitle("New Crucible Review");
		setDescription(ENTER_THE_DETAILS_OF_THE_REVIEW);
		this.taskRepository = repository;
	}

	@Override
	public void setVisible(final boolean visible) {
		// check if cached data is available, if not, start background process to fetch it
		if (visible) {
			if (!CrucibleUiUtil.hasCachedData(taskRepository)) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						CrucibleUiUtil.updateTaskRepositoryCache(taskRepository, getContainer(),
								CrucibleReviewDetailsPage.this);
						setInputAndInitialSelections();
						reviewersSelectionTreePart.setAllReviewers(CrucibleUiUtil
								.getAllCachedUsersAsReviewers(taskRepository));
					}
				});
			} else {
				// preselect
				setInputAndInitialSelections();
				reviewersSelectionTreePart.setAllReviewers(CrucibleUiUtil.getAllCachedUsersAsReviewers(taskRepository));
			}
		}
		super.setVisible(visible);
	}

	private void setInputAndInitialSelections() {
		updateInput();

		Set<CrucibleProject> cachedProjects = CrucibleUiUtil.getCachedProjects(taskRepository);

		CrucibleProject lastSelectedProject = CrucibleRepositoryConnector.getLastSelectedProject(taskRepository,
				cachedProjects);
		if (lastSelectedProject != null) {
			projectsComboViewer.setSelection(new StructuredSelection(lastSelectedProject));
		} else {
			if (projectsComboViewer.getElementAt(0) != null) {
				projectsComboViewer.setSelection(new StructuredSelection(projectsComboViewer.getElementAt(0)));
			}
		}

		User currentUser = CrucibleUiUtil.getCurrentCachedUser(taskRepository);
		if (currentUser != null) {
			moderatorComboViewer.setSelection(new StructuredSelection(currentUser));
			authorComboViewer.setSelection(new StructuredSelection(currentUser));
		} else {
			if (moderatorComboViewer.getElementAt(0) != null) {
				moderatorComboViewer.setSelection(new StructuredSelection(moderatorComboViewer.getElementAt(0)));
			}
			if (authorComboViewer.getElementAt(0) != null) {
				authorComboViewer.setSelection(new StructuredSelection(authorComboViewer.getElementAt(0)));
			}
		}

		// restore checkboxes selection
		anyoneCanJoin.setSelection(CrucibleRepositoryConnector.getAllowAnyoneOption(taskRepository));
		startReview.setSelection(CrucibleRepositoryConnector.getStartReviewOption(taskRepository));
	}

	private void updateInput() {
		Set<CrucibleProject> cachedProjects = CrucibleUiUtil.getCachedProjects(taskRepository);
		projectsComboViewer.setInput(cachedProjects);

		Set<User> cachedUsers = CrucibleUiUtil.getCachedUsers(taskRepository);
		moderatorComboViewer.setInput(cachedUsers);
		authorComboViewer.setInput(cachedUsers);
	}

	private void updateInputAndRestoreSelections() {
		CrucibleProject previousProject = (CrucibleProject) ((IStructuredSelection) projectsComboViewer.getSelection())
				.getFirstElement();
		User previousModerator = (User) ((IStructuredSelection) moderatorComboViewer.getSelection()).getFirstElement();
		User previousAuthor = (User) ((IStructuredSelection) authorComboViewer.getSelection()).getFirstElement();

		updateInput();

		if (previousProject != null) {
			projectsComboViewer.setSelection(new StructuredSelection(previousProject));
		}

		if (previousModerator != null) {
			moderatorComboViewer.setSelection(new StructuredSelection(previousModerator));
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
				Object firstElement = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (firstElement != null) {
					getWizard().getContainer().updateButtons();
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(projectsComboViewer.getCombo());

		new Label(composite, SWT.NONE).setText("Moderator:");
		moderatorComboViewer = new ComboViewer(composite);
		moderatorComboViewer.setLabelProvider(new CrucibleUserLabelProvider());
		moderatorComboViewer.setContentProvider(new ArrayContentProvider());
		moderatorComboViewer.setComparator(new ViewerComparator());
		moderatorComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object firstElement = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (firstElement != null) {
					getWizard().getContainer().updateButtons();
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(moderatorComboViewer.getCombo());

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

		Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).span(6, 1).applyTo(label);

		label = new Label(composite, SWT.NONE);
		label.setText("Objectives:");

		GridDataFactory.fillDefaults().span(4, 1).applyTo(label);

		label = new Label(composite, SWT.NONE);
		label.setText("Reviewers:");
		GridDataFactory.fillDefaults().span(2, 1).indent(5, SWT.DEFAULT).applyTo(label);

		Composite textAreacomposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginLeft = 0;
		layout.marginTop = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 10;
		textAreacomposite.setLayout(layout);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		gd.horizontalIndent = 0;
		textAreacomposite.setLayoutData(gd);
		GridDataFactory.fillDefaults().grab(true, true).hint(480, 200).span(4, 1).applyTo(textAreacomposite);

		objectivesText = new Text(textAreacomposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, true).hint(480, 200).applyTo(objectivesText);

		if (addComment) {
			label = new Label(textAreacomposite, SWT.NONE);
			label.setText("Selection Comment:");
			GridDataFactory.fillDefaults().applyTo(label);
			commentText = new Text(textAreacomposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
			GridDataFactory.fillDefaults().grab(true, true).hint(480, 200).applyTo(commentText);
		}

		reviewersSelectionTreePart = new ReviewersSelectionTreePart(Collections.<Reviewer> emptySet(),
				CrucibleUiUtil.getAllCachedUsersAsReviewers(taskRepository));
		Composite reviewersComp = reviewersSelectionTreePart.createControl(composite);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).hint(SWT.DEFAULT, 150).applyTo(reviewersComp);
		reviewersSelectionTreePart.setCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				getWizard().getContainer().updateButtons();
			}
		});

		Button updateData = new Button(composite, SWT.PUSH);
		updateData.setText("Update Repository Data");
		GridDataFactory.fillDefaults().span(4, 2).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(updateData);
		updateData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CrucibleUiUtil.updateTaskRepositoryCache(taskRepository, getContainer(), CrucibleReviewDetailsPage.this);
				updateInputAndRestoreSelections();
				reviewersSelectionTreePart.setAllReviewers(CrucibleUiUtil.getAllCachedUsersAsReviewers(taskRepository));
			}
		});

		anyoneCanJoin = new Button(composite, SWT.CHECK);
		anyoneCanJoin.setText("Allow anyone to join");
		GridDataFactory.fillDefaults().indent(5, SWT.DEFAULT).span(2, 1).applyTo(anyoneCanJoin);

		startReview = new Button(composite, SWT.CHECK);
		startReview.setText("Start review immediately (if permitted)");
		GridDataFactory.fillDefaults().span(2, 1).align(SWT.BEGINNING, SWT.BEGINNING).indent(5, SWT.DEFAULT).applyTo(
				startReview);

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	@Override
	public boolean isPageComplete() {
		Review review = getReview();
		return review != null && hasRequiredFields(review) && hasValidReviewers(review);
	}

	private boolean hasRequiredFields(Review newReview) {
		setErrorMessage(null);
		String newMessage = null;

		if (getSelectedProject() == null) {
			newMessage = "Select a project";
		}
		if (newReview.getModerator() == null) {
			newMessage = "Select a moderator";
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

	private boolean hasValidReviewers(Review newReview) {
		setErrorMessage(null);
		for (Reviewer reviewer : reviewersSelectionTreePart.getSelectedReviewers()) {
			if (newReview.getAuthor().getUsername().equals(reviewer.getUsername())) {
				setErrorMessage("The author might not be a reviewer as well.");
				return false;
			}
			if (newReview.getModerator().getUsername().equals(reviewer.getUsername())) {
				setErrorMessage("The moderator might not be a reviewer as well.");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	@Nullable
	public Review getReview() {
		final User author = getSelectedAuthor();
		final CrucibleProject project = getSelectedProject();

		if (project == null || project.getKey() == null) {
			return null;
		}

		Review review = new Review(taskRepository.getUrl(), project.getKey(), author, null);

		if (titleText != null) {
			review.setName(titleText.getText());
			review.setSummary(titleText.getText());
		}

		if (objectivesText != null) {
			review.setDescription(objectivesText.getText());
		}

		if (reviewersSelectionTreePart != null) {
			review.setReviewers(reviewersSelectionTreePart.getSelectedReviewers());
		}

		if (anyoneCanJoin != null) {
			review.setAllowReviewerToJoin(anyoneCanJoin.getEnabled());
		}
		review.setCreator(CrucibleUiUtil.getCurrentCachedUser(taskRepository));

		if (moderatorComboViewer != null) {
			review.setModerator((User) ((IStructuredSelection) moderatorComboViewer.getSelection()).getFirstElement());
		}

		return review;
	}

	private User getSelectedAuthor() {
		User author = (authorComboViewer != null)
				? (User) ((IStructuredSelection) authorComboViewer.getSelection()).getFirstElement()
				: null;
		return author;
	}

	Set<Reviewer> getReviewers() {
		return reviewersSelectionTreePart.getSelectedReviewers();
	}

	boolean isStartReviewImmediately() {
		return startReview.getSelection();
	}

	@Nullable
	CrucibleProject getSelectedProject() {
		if (projectsComboViewer == null) {
			return null;
		}
		Object firstElement = ((IStructuredSelection) projectsComboViewer.getSelection()).getFirstElement();
		if (firstElement != null) {
			return (CrucibleProject) firstElement;
		}
		return null;
	}

	boolean isAllowAnyoneToJoin() {
		return anyoneCanJoin.getSelection();
	}

	public String getComment() {
		return commentText.getText();
	}

}
