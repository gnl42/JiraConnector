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

import com.atlassian.connector.eclipse.internal.crucible.core.TaskRepositoryUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeImages;
import com.atlassian.connector.eclipse.team.ui.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector;
import com.atlassian.connector.eclipse.team.ui.ITeamUiResourceConnector2;
import com.atlassian.connector.eclipse.team.ui.ScmRepository;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.navigator.ResourceComparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Page for selecting changeset for the new review
 * 
 * @author Thomas Ehrnhoefer
 */
public class SelectScmChangesetsPage extends AbstractCrucibleWizardPage {

	private static final int LIMIT = 25;

	private static final String EMPTY_NODE = "No changesets available.";

	private class ChangesetLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			if (element == null) {
				return null;
			}
			if (element instanceof ScmRepository) {
				return FishEyeImages.getImage(FishEyeImages.REPOSITORY);
			} else if (element instanceof ICustomChangesetLogEntry) {
				return CommonImages.getImage(CrucibleImages.CHANGESET);
			} else if (element == EMPTY_NODE) {
				return null;
			} else if (element instanceof String) {
				return CommonImages.getImage(CrucibleImages.FILE);
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element == null) {
				return "";
			}
			if (element instanceof ScmRepository) {
				return ((ScmRepository) element).getScmPath();
			} else if (element instanceof ICustomChangesetLogEntry) {
				ICustomChangesetLogEntry logEntry = (ICustomChangesetLogEntry) element;
				return logEntry.getRevision() + " [" + logEntry.getAuthor() + "] - " + logEntry.getComment();
			} else if (element == EMPTY_NODE) {
				return EMPTY_NODE;
			} else if (element instanceof String) {
				return (String) element;
			}
			return "";
		}
	}

	private class ChangesetContentProvider implements ITreeContentProvider {

		private Map<ScmRepository, SortedSet<ICustomChangesetLogEntry>> logEntries;

		public Object[] getChildren(Object parentElement) {
			if (logEntries == null || parentElement == null) {
				return new Object[0];
			}
			if (parentElement instanceof ScmRepository) {
				//root, repository URLs
				if (logEntries.get(parentElement) == null || logEntries.get(parentElement).size() == 0) {
					//if no retrieved changeset, create fake node for lazy loading
					return new String[] { EMPTY_NODE };
				}
				return logEntries.get(parentElement).toArray();
			} else if (parentElement instanceof ICustomChangesetLogEntry) {
				//changeset files
				return ((ICustomChangesetLogEntry) parentElement).getChangedFiles();
			}
			return new Object[0];
		}

		@SuppressWarnings("rawtypes")
		public Object getParent(Object element) {
			if (logEntries == null) {
				return null;
			}
			if (element instanceof Map || element instanceof ScmRepository) {
				//root, repository URLs
				return null;
			} else if (element instanceof ICustomChangesetLogEntry) {
				//changeset elements
				return ((ICustomChangesetLogEntry) element).getRepository();
			}
			return null;
		}

		@SuppressWarnings("rawtypes")
		public boolean hasChildren(Object element) {
			if (logEntries == null) {
				return false;
			}
			if (element instanceof Map) {
				//root, repository URLs
				return logEntries.size() > 0;
			} else if (element instanceof ScmRepository) {
				//change sets for a repository
//				return logEntries.get(element).size() > 0;
				return true;
			} else if (element instanceof ICustomChangesetLogEntry) {
				//changeset elements
				return ((ICustomChangesetLogEntry) element).getChangedFiles().length > 0;
			}
			return false;
		}

		/**
		 * @return array of map keys (Repository URLs)
		 */
		public Object[] getElements(Object inputElement) {
			if (logEntries == null) {
				return new Object[0];
			}
			//repositories 
			return logEntries.keySet().toArray();
		}

		public void dispose() {
			// ignore

		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof Map) {
				logEntries = (Map<ScmRepository, SortedSet<ICustomChangesetLogEntry>>) newInput;
			}
		}

	}

	private final Map<ScmRepository, SortedSet<ICustomChangesetLogEntry>> availableLogEntries;

	private final Map<ScmRepository, SortedSet<ICustomChangesetLogEntry>> selectedLogEntries;

	private TreeViewer availableTreeViewer;

	private TreeViewer selectedTreeViewer;

	private Button addButton;

	private Button removeButton;

	private MenuItem removeChangesetMenuItem;

	private MenuItem getNextRevisionsMenuItem;

	private MenuItem addChangesetMenuItem;

	private final TaskRepository taskRepository;

	private DefineRepositoryMappingButton mappingButton;

	public SelectScmChangesetsPage(@NotNull TaskRepository repository) {
		this(repository, new TreeSet<ICustomChangesetLogEntry>());
	}

	public SelectScmChangesetsPage(@NotNull TaskRepository repository,
			@Nullable SortedSet<ICustomChangesetLogEntry> logEntries) {
		super("crucibleChangesets"); //$NON-NLS-1$
		setTitle("Select Changesets");
		setDescription("Select the changesets that should be included in the review.");

		this.taskRepository = repository;
		this.availableLogEntries = new HashMap<ScmRepository, SortedSet<ICustomChangesetLogEntry>>();
		this.selectedLogEntries = new HashMap<ScmRepository, SortedSet<ICustomChangesetLogEntry>>();

		if (logEntries != null && logEntries.size() > 0) {
			this.selectedLogEntries.put(logEntries.first().getRepository(), logEntries);
		}
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(5, 5).create());

		Label label = new Label(composite, SWT.NONE);
		label.setText("Select changesets from your repositories:");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		new Label(composite, SWT.NONE).setText("Changesets selected for the review:");

		createLeftViewer(composite);

		createButtonComp(composite);

		createRightViewer(composite);

		mappingButton = new DefineRepositoryMappingButton(this, composite, getTaskRepository());

		Control button = mappingButton.getControl();
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(button);

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	/*
	 * checks if page is complete updates the buttons
	 */
	public void validatePage() {
		setErrorMessage(null);

		// Check if all custom repositories are mapped to Crucible source repositories
		boolean allFine = true;
		for (Set<ICustomChangesetLogEntry> entries : selectedLogEntries.values()) {
			for (ICustomChangesetLogEntry entry : entries) {
				String[] files = entry.getChangedFiles();
				if (files == null || files.length == 0) {
					continue;
				}
				for (String file : files) {
					String scmPath = entry.getRepository().getRootPath() + '/' + file;
					Map.Entry<String, String> sourceRepository = TaskRepositoryUtil.getMatchingSourceRepository(
							TaskRepositoryUtil.getScmRepositoryMappings(getTaskRepository()), scmPath);

					if (sourceRepository == null) {
						mappingButton.setMissingMapping(entry.getRepository().getScmPath());
						setErrorMessage(NLS.bind("SCM repository path {0} is not mapped to Crucible repository.",
								entry.getRepository().getScmPath()));
						allFine = false;
						break;
					}
				}
				if (!allFine) {
					break;
				}
			}
			if (!allFine) {
				break;
			}
		}
		setPageComplete(allFine);
		getContainer().updateButtons();
	}

	private void createLeftViewer(Composite parent) {
		Tree tree = new Tree(parent, SWT.MULTI | SWT.BORDER);
		availableTreeViewer = new TreeViewer(tree);

		GridDataFactory.fillDefaults().grab(true, true).hint(300, 220).applyTo(tree);
		availableTreeViewer.setLabelProvider(new ChangesetLabelProvider());
		availableTreeViewer.setContentProvider(new ChangesetContentProvider());
		availableTreeViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
		availableTreeViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof ICustomChangesetLogEntry && e2 instanceof ICustomChangesetLogEntry) {
					return ((ICustomChangesetLogEntry) e2).getDate().compareTo(
							((ICustomChangesetLogEntry) e1).getDate());
				}
				return super.compare(viewer, e1, e2);
			}
		});
		availableTreeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		final Menu contextMenuSource = new Menu(getShell(), SWT.POP_UP);
		tree.setMenu(contextMenuSource);
		addChangesetMenuItem = new MenuItem(contextMenuSource, SWT.PUSH);
		addChangesetMenuItem.setText("Add to Review");

		new MenuItem(contextMenuSource, SWT.SEPARATOR);

		addChangesetMenuItem.setEnabled(false);
		getNextRevisionsMenuItem = new MenuItem(contextMenuSource, SWT.PUSH);
		getNextRevisionsMenuItem.setText(String.format("Get %d More Revisions", LIMIT));
		getNextRevisionsMenuItem.setEnabled(false);
		getNextRevisionsMenuItem.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeSelection selection = getTreeSelection(availableTreeViewer);
				if (selection != null) {
					Iterator<Object> iterator = (selection).iterator();
					Set<ScmRepository> alreadyDone = new TreeSet<ScmRepository>();
					while (iterator.hasNext()) {
						Object element = iterator.next();
						ScmRepository repository = null;
						if (element instanceof ICustomChangesetLogEntry) {
							repository = ((ICustomChangesetLogEntry) element).getRepository();
						} else if (element instanceof ScmRepository) {
							repository = (ScmRepository) element;
						}
						if (repository != null && !alreadyDone.contains(repository)) {
							SortedSet<ICustomChangesetLogEntry> logEntries = availableLogEntries.get(repository);
							int currentNumberOfEntries = logEntries == null ? 0 : logEntries.size();
							updateChangesets(repository, currentNumberOfEntries + LIMIT);
							alreadyDone.add(repository);
						}
					}
				} else {
					//update all
					for (ScmRepository repository : availableLogEntries.keySet()) {
						SortedSet<ICustomChangesetLogEntry> logEntries = availableLogEntries.get(repository);
						int currentNumberOfEntries = logEntries == null ? 0 : logEntries.size();
						updateChangesets(repository, currentNumberOfEntries + LIMIT);
					}
				}
			}
		});
		addChangesetMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addChangesets();
				updateButtonEnablement();
			}
		});
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonEnablement();
			}
		});
		availableTreeViewer.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
				// ignore
			}

			public void treeExpanded(TreeExpansionEvent event) {
				// first time of expanding: retrieve first 10 changesets
				final Object object = event.getElement();
				if (object instanceof ScmRepository) {
					refreshRepository((ScmRepository) object);
				}

			}
		});
		availableTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object object = selection.getFirstElement();
				if (availableTreeViewer.isExpandable(object)) {
					if (!availableTreeViewer.getExpandedState(object) && object instanceof ScmRepository) {
						refreshRepository((ScmRepository) object);
						return;
					}
					availableTreeViewer.setExpandedState(object, !availableTreeViewer.getExpandedState(object));
				}
			}
		});
	}

	private void refreshRepository(final ScmRepository object) {
		SortedSet<ICustomChangesetLogEntry> logEntries = availableLogEntries.get(object);
		if (logEntries == null) {
			updateChangesets(object, LIMIT);
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				// expand tree after filling
				availableTreeViewer.expandToLevel(object, 1);
			}
		});
	}

	private void createButtonComp(Composite composite) {
		Composite buttonComp = new Composite(composite, SWT.NONE);
		buttonComp.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());
		GridDataFactory.fillDefaults().grab(false, true).applyTo(buttonComp);

		addButton = new Button(buttonComp, SWT.PUSH);
		addButton.setText("Add -->");
		addButton.setToolTipText("Add all selected changesets");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addChangesets();
				updateButtonEnablement();
			}
		});
		addButton.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(addButton);

		removeButton = new Button(buttonComp, SWT.PUSH);
		removeButton.setText("<-- Remove");
		removeButton.setToolTipText("Remove all selected changesets");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeChangesets();
				updateButtonEnablement();
			}

		});
		removeButton.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(removeButton);
	}

	private void createRightViewer(Composite composite) {
		Tree tree = new Tree(composite, SWT.MULTI | SWT.BORDER);
		selectedTreeViewer = new TreeViewer(tree);

		GridDataFactory.fillDefaults().grab(true, true).hint(300, 220).applyTo(tree);
		//GridDataFactory.fillDefaults().grab(true, true).hint(300, SWT.DEFAULT).applyTo(tree);
		selectedTreeViewer.setLabelProvider(new ChangesetLabelProvider());
		selectedTreeViewer.setContentProvider(new ChangesetContentProvider());
		selectedTreeViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
		final Menu contextMenuSource = new Menu(getShell(), SWT.POP_UP);
		tree.setMenu(contextMenuSource);
		removeChangesetMenuItem = new MenuItem(contextMenuSource, SWT.PUSH);
		removeChangesetMenuItem.setText("Remove from Review");
		removeChangesetMenuItem.setEnabled(false);
		removeChangesetMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeChangesets();
				updateButtonEnablement();
			}
		});

		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonEnablement();
			}
		});
	}

	private void updateButtonEnablement() {
		//right viewer
		TreeSelection selection = validateTreeSelection(selectedTreeViewer, false);
		removeButton.setEnabled(selection != null && !selection.isEmpty());
		removeChangesetMenuItem.setEnabled(selection != null && !selection.isEmpty());
		//left viewer
		selection = validateTreeSelection(availableTreeViewer, true);
		boolean changesetsOnly = hasChangesetsOnly(selection);
		addButton.setEnabled(selection != null && !selection.isEmpty() && !hasAlreadyChosenChangesetSelected(selection)
				&& changesetsOnly);
		addChangesetMenuItem.setEnabled(selection != null && !selection.isEmpty()
				&& !hasAlreadyChosenChangesetSelected(selection) && changesetsOnly);
		getNextRevisionsMenuItem.setEnabled(selection != null && !selection.isEmpty());
	}

	@SuppressWarnings("unchecked")
	private boolean hasChangesetsOnly(TreeSelection selection) {
		if (selection != null) {
			Iterator<Object> iterator = (selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof ScmRepository) {
					return false;
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean hasAlreadyChosenChangesetSelected(TreeSelection selection) {
		for (ScmRepository repository : selectedLogEntries.keySet()) {
			Iterator<Object> iterator = (selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof ICustomChangesetLogEntry) {
					if (selectedLogEntries.get(repository).contains(element)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void updateRepositories() {
		final MultiStatus status = new MultiStatus(CrucibleUiPlugin.PLUGIN_ID, IStatus.WARNING,
				"Error while retrieving repositories", null);

		IRunnableWithProgress getRepositories = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				Collection<ScmRepository> repositories = TeamUiUtils.getRepositories(monitor);
				if (repositories != null) {
					for (ScmRepository repository : repositories) {
						availableLogEntries.put(repository, null);
					}
				}
			}
		};

		try {
			setErrorMessage(null);
			getContainer().run(true, true, getRepositories); // blocking operation
		} catch (Exception e) {
			status.add(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, "Failed to retrieve repositories", e));
		}

		if (availableLogEntries != null) {
			availableTreeViewer.setInput(availableLogEntries);
		}

		if (status.getChildren().length > 0 && status.getSeverity() == IStatus.ERROR) { //only log errors, swallow warnings
			setErrorMessage("Error while retrieving repositories. See Error Log for details.");
			StatusHandler.log(status);
		}
	}

	private void updateChangesets(final ScmRepository repository, final int numberToRetrieve) {
		final IStatus[] status = { Status.OK_STATUS };

		IRunnableWithProgress getChangesets = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					ITeamUiResourceConnector tc = repository.getTeamResourceConnector();
					if (tc instanceof ITeamUiResourceConnector2) {
						SortedSet<ICustomChangesetLogEntry> retrieved = ((ITeamUiResourceConnector2) tc).getLatestChangesets(
								repository.getScmPath(), numberToRetrieve, monitor);

						if (availableLogEntries.containsKey(repository) && availableLogEntries.get(repository) != null) {
							availableLogEntries.get(repository).addAll(retrieved);
						} else {
							availableLogEntries.put(repository, retrieved);
						}
					} else {
						status[0] = new Status(IStatus.ERROR, CrucibleUiPlugin.PLUGIN_ID,
								"This repository is managed by SCM integration that's compatible only with Crucible 2.x");
					}
				} catch (CoreException e) {
					status[0] = e.getStatus();
				}
			}
		};

		try {
			setErrorMessage(null);
			getContainer().run(false, true, getChangesets); // blocking operation
		} catch (Exception e) {
			status[0] = new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, "Failed to retrieve changesets", e);
		}

		if (availableLogEntries != null && availableLogEntries.get(repository) != null) {
			availableTreeViewer.setInput(availableLogEntries);
		}

		if (status[0].getSeverity() == IStatus.ERROR) { //only log errors, swallow warnings
			setErrorMessage(String.format("Error while retrieving changesets (%s). See Error Log for details.",
					status[0].getMessage()));
			StatusHandler.log(status[0]);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && (availableLogEntries.isEmpty() || !CrucibleUiUtil.hasCachedData(getTaskRepository()))) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (availableLogEntries.isEmpty()) {
						updateRepositories();
						selectedTreeViewer.setInput(selectedLogEntries);
						validatePage();
					}
				}
			});
		}
	}

	private TaskRepository getTaskRepository() {
		return taskRepository;
	}

	private void addChangesets() {
		TreeSelection selection = getTreeSelection(availableTreeViewer);
		addOrRemoveChangesets(selection, true);
	}

	private void removeChangesets() {
		TreeSelection selection = getTreeSelection(selectedTreeViewer);
		addOrRemoveChangesets(selection, false);
	}

	@SuppressWarnings("unchecked")
	private void addOrRemoveChangesets(TreeSelection selection, boolean add) {
		if (selection != null) {
			Iterator<Object> iterator = (selection).iterator();
			Set<ScmRepository> expandedRepositories = new HashSet<ScmRepository>();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof ICustomChangesetLogEntry) {
					ScmRepository repository = ((ICustomChangesetLogEntry) element).getRepository();
					SortedSet<ICustomChangesetLogEntry> changesets = selectedLogEntries.get(repository);
					if (changesets == null) {
						changesets = new TreeSet<ICustomChangesetLogEntry>();
					}
					if (add) {
						changesets.add((ICustomChangesetLogEntry) element);
					} else {
						changesets.remove(element);
					}
					if (changesets.size() > 0) {
						selectedLogEntries.put(repository, changesets);
					} else {
						selectedLogEntries.remove(repository);
					}
					expandedRepositories.add(repository);
				}
			}
			selectedTreeViewer.setInput(selectedLogEntries);
			selectedTreeViewer.setExpandedElements(expandedRepositories.toArray());
		}
		validatePage();
	}

	@SuppressWarnings("unchecked")
	private TreeSelection validateTreeSelection(TreeViewer treeViewer, boolean allowChangesetsSelection) {
		TreeSelection selection = getTreeSelection(treeViewer);
		if (selection != null) {
			ArrayList<TreePath> validSelections = new ArrayList<TreePath>();
			Iterator<Object> iterator = (selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof ICustomChangesetLogEntry) {
					validSelections.add((selection).getPathsFor(element)[0]);
				} else if (allowChangesetsSelection && element instanceof ScmRepository) {
					validSelections.add((selection).getPathsFor(element)[0]);
				}
			}
			//set new selection
			TreeSelection newSelection = new TreeSelection(
					validSelections.toArray(new TreePath[validSelections.size()]), (selection).getElementComparer());
			treeViewer.setSelection(newSelection);
		} else {
			treeViewer.setSelection(new TreeSelection());
		}
		return getTreeSelection(treeViewer);
	}

	private TreeSelection getTreeSelection(TreeViewer treeViewer) {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof TreeSelection) {
			return (TreeSelection) selection;
		}
		return null;
	}

	public Map<String, Set<String>> getSelectedChangesets() {
		Map<String, Set<String>> result = MiscUtil.buildHashMap();

		for (SortedSet<ICustomChangesetLogEntry> entries : selectedLogEntries.values()) {
			for (ICustomChangesetLogEntry entry : entries) {
				String[] files = entry.getChangedFiles();
				if (files == null || files.length == 0) {
					continue;
				}
				for (String file : files) {
					Map.Entry<String, String> sourceRepository = TaskRepositoryUtil.getMatchingSourceRepository(
							TaskRepositoryUtil.getScmRepositoryMappings(getTaskRepository()), entry.getRepository()
									.getRootPath()
									+ '/' + file);
					if (sourceRepository != null) {
						if (!result.containsKey(sourceRepository.getValue())) {
							result.put(sourceRepository.getValue(), new HashSet<String>());
						}
						result.get(sourceRepository.getValue()).add(entry.getRevision());
					}
				}
			}
		}
		return result;
	}

}
