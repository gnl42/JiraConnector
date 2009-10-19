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
import com.atlassian.connector.eclipse.internal.crucible.ui.dialogs.ComboViewerSelectionDialog;
import com.atlassian.connector.eclipse.ui.team.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.ui.team.RepositoryInfo;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
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
public class CrucibleAddChangesetsPage extends WizardPage {

	private static final int LIMIT = 25;

	private static final String EMPTY_NODE = "No changesets available.";

	private class ChangesetLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			if (element == null) {
				return null;
			}
			if (element instanceof RepositoryInfo) {
				return CommonImages.getImage(CrucibleImages.REPOSITORY);
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
			if (element instanceof RepositoryInfo) {
				return ((RepositoryInfo) element).getScmPath();
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

		private Map<RepositoryInfo, SortedSet<ICustomChangesetLogEntry>> logEntries;

		public Object[] getChildren(Object parentElement) {
			if (logEntries == null || parentElement == null) {
				return new Object[0];
			}
			if (parentElement instanceof RepositoryInfo) {
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

		@SuppressWarnings("unchecked")
		public Object getParent(Object element) {
			if (logEntries == null) {
				return null;
			}
			if (element instanceof Map || element instanceof RepositoryInfo) {
				//root, repository URLs
				return null;
			} else if (element instanceof ICustomChangesetLogEntry) {
				//changeset elements
				return ((ICustomChangesetLogEntry) element).getRepository();
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		public boolean hasChildren(Object element) {
			if (logEntries == null) {
				return false;
			}
			if (element instanceof Map) {
				//root, repository URLs
				return logEntries.size() > 0;
			} else if (element instanceof RepositoryInfo) {
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

		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof Map) {
				logEntries = (Map<RepositoryInfo, SortedSet<ICustomChangesetLogEntry>>) newInput;
			}
		}

	}

	private final Map<RepositoryInfo, SortedSet<ICustomChangesetLogEntry>> availableLogEntries;

	private final Map<RepositoryInfo, SortedSet<ICustomChangesetLogEntry>> selectedLogEntries;

	private TreeViewer availableTreeViewer;

	private TreeViewer selectedTreeViewer;

	private TableViewer repositoriesMappingViewer;

	private final TaskRepository taskRepository;

	private Set<Repository> cachedRepositories;

	private final Map<String, String> repositoryMappings;

	private final Map<RepositoryInfo, ComboViewer> mappingCombos;

	private Button addButton;

	private Button removeButton;

	private MenuItem removeChangesetMenuItem;

	private MenuItem getNextRevisionsMenuItem;

	private MenuItem addChangesetMenuItem;

	private final ReviewWizard wizard;

	public CrucibleAddChangesetsPage(@NotNull TaskRepository repository, @NotNull ReviewWizard wizard) {
		this(repository, new TreeSet<ICustomChangesetLogEntry>(), wizard);
	}

	public CrucibleAddChangesetsPage(@NotNull TaskRepository repository,
			@Nullable SortedSet<ICustomChangesetLogEntry> logEntries, @NotNull ReviewWizard wizard) {
		super("crucibleChangesets"); //$NON-NLS-1$
		setTitle("Select Changesets");
		setDescription("Select the changesets that should be included in the review.");
		this.availableLogEntries = new HashMap<RepositoryInfo, SortedSet<ICustomChangesetLogEntry>>();
		this.selectedLogEntries = new HashMap<RepositoryInfo, SortedSet<ICustomChangesetLogEntry>>();
		this.repositoryMappings = TaskRepositoryUtil.getScmRepositoryMappings(repository);
		this.mappingCombos = new HashMap<RepositoryInfo, ComboViewer>();
		this.taskRepository = repository;

		if (logEntries != null && logEntries.size() > 0) {
			this.selectedLogEntries.put(logEntries.first().getRepository(), logEntries);
			this.repositoryMappings.put(logEntries.first().getRepository().getScmPath(), null);
		}

		this.wizard = wizard;
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(5, 5).create());

		Label label = new Label(composite, SWT.NONE);
		label.setText("Select changesets from your repositories:");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		new Label(composite, SWT.NONE).setText("Changesets selected for the review:");

		createLeftViewer(composite);

		createButtonComp(composite);

		createRightViewer(composite);

		createRepositoryMappingComp(composite);

		Button updateData = new Button(composite, SWT.PUSH);
		updateData.setText("Update Repository Data");
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(updateData);
		updateData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				wizard.updateCache(CrucibleAddChangesetsPage.this);
				cachedRepositories = null;
			}
		});

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	private void createRepositoryMappingComp(Composite composite) {
		final Composite mappingComposite = new Composite(composite, SWT.NONE);

		GridDataFactory.fillDefaults().span(3, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(mappingComposite);
		mappingComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		final Table table = new Table(mappingComposite, SWT.BORDER);
		table.setHeaderVisible(true);

		GridDataFactory.fillDefaults().hint(1000, 100).grab(true, true).applyTo(table);
		repositoriesMappingViewer = new TableViewer(table);
		repositoriesMappingViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Map<?, ?>) {
					return ((Map<?, ?>) inputElement).keySet().toArray();
				}
				return new Object[0];
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		repositoriesMappingViewer.setInput(selectedLogEntries);

		final TableViewerColumn column1 = new TableViewerColumn(repositoriesMappingViewer, SWT.NONE);
		column1.getColumn().setText("Local Repository");
		column1.getColumn().setWidth(500);
		column1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RepositoryInfo) {
					return ((RepositoryInfo) element).getScmPath();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof RepositoryInfo) {
					return CommonImages.getImage(CrucibleImages.REPOSITORY);
				}
				return null;
			}
		});

		final TableViewerColumn column2 = new TableViewerColumn(repositoriesMappingViewer, SWT.NONE);
		column2.getColumn().setText("Crucible Repository");
		column2.getColumn().setWidth(200);
		column2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof RepositoryInfo) {
					String mapping = repositoryMappings.get(((RepositoryInfo) element).getScmPath());
					if (mapping != null) {
						return mapping;
					}
				}
				return "";
			}

			@Override
			public Image getImage(Object element) {
				return null;
			}
		});

		final Button editButton = new Button(mappingComposite, SWT.PUSH);
		editButton.setText("Edit...");
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectRepositoryMapping();
			}
		});
		editButton.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(editButton);

		table.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				selectRepositoryMapping();
			}

			public void widgetSelected(SelectionEvent e) {
				ISelection selection = repositoriesMappingViewer.getSelection();
				if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1) {
					editButton.setEnabled(true);
				} else {
					editButton.setEnabled(false);
				}
			}

		});
	}

	private void selectRepositoryMapping() {
		ISelection selection = repositoriesMappingViewer.getSelection();
		if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1) {
			String scmPath = ((RepositoryInfo) ((IStructuredSelection) selection).getFirstElement()).getScmPath();
			if (cachedRepositories == null) {
				cachedRepositories = CrucibleUiUtil.getCachedRepositories(taskRepository);
			}
			ComboViewerSelectionDialog dialog = new ComboViewerSelectionDialog(repositoriesMappingViewer.getTable()
					.getShell(), "Map Local to Crucible Repository", "Map \"" + scmPath + "\" to: ", cachedRepositories);
			int returnCode = dialog.open();
			if (returnCode == IDialogConstants.OK_ID) {
				Repository crucibleRepository = dialog.getSelection();
				repositoryMappings.put(scmPath, crucibleRepository.getName());
				repositoriesMappingViewer.setInput(selectedLogEntries);
			}
		}
		validatePage();
	}

	/*
	 * checks if page is complete updates the buttons
	 */
	private void validatePage() {
		setErrorMessage(null);

		//check if all custom repositories are mapped to crucible repositories
		boolean allFine = true;
		for (String scmPath : repositoryMappings.keySet()) {
			if (repositoryMappings.get(scmPath) == null) {
				setErrorMessage("One or more local repositories are not mapped to Crucible repositories.");
				allFine = false;
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
					Set<RepositoryInfo> alreadyDone = new TreeSet<RepositoryInfo>();
					while (iterator.hasNext()) {
						Object element = iterator.next();
						RepositoryInfo repository = null;
						if (element instanceof ICustomChangesetLogEntry) {
							repository = ((ICustomChangesetLogEntry) element).getRepository();
						} else if (element instanceof RepositoryInfo) {
							repository = (RepositoryInfo) element;
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
					for (RepositoryInfo repository : availableLogEntries.keySet()) {
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
				if (object instanceof RepositoryInfo) {
					SortedSet<ICustomChangesetLogEntry> logEntries = availableLogEntries.get(object);
					if (logEntries == null) {
						updateChangesets((RepositoryInfo) object, LIMIT);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								// expand tree after filling
								availableTreeViewer.expandToLevel(object, 1);
							}
						});
					}
				}
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
				if (element instanceof RepositoryInfo) {
					return false;
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean hasAlreadyChosenChangesetSelected(TreeSelection selection) {
		for (RepositoryInfo repository : selectedLogEntries.keySet()) {
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
				Collection<RepositoryInfo> repositories = TeamUiUtils.getRepositories(monitor);
				if (repositories != null) {
					for (RepositoryInfo repository : repositories) {
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
			setErrorMessage("Error while retrieving repositories. See error log for details.");
			StatusHandler.log(status);
		}
	}

	private void updateChangesets(final RepositoryInfo repository, final int numberToRetrieve) {
		final MultiStatus status = new MultiStatus(CrucibleUiPlugin.PLUGIN_ID, IStatus.WARNING,
				"Error while retrieving changesets", null);

		IRunnableWithProgress getChangesets = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					SortedSet<ICustomChangesetLogEntry> retrieved = repository.getTeamResourceConnector()
							.getLatestChangesets(repository.getScmPath(), numberToRetrieve, monitor);

					if (availableLogEntries.containsKey(repository) && availableLogEntries.get(repository) != null) {
						availableLogEntries.get(repository).addAll(retrieved);
					} else {
						availableLogEntries.put(repository, retrieved);
					}
				} catch (CoreException e) {
					status.add(e.getStatus());
				}
			}
		};

		try {
			setErrorMessage(null);
			getContainer().run(true, true, getChangesets); // blocking operation
		} catch (Exception e) {
			status.add(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, "Failed to retrieve revisions", e));
		}

		if (availableLogEntries != null && availableLogEntries.get(repository) != null) {
			availableTreeViewer.setInput(availableLogEntries);
		}

		if (status.getChildren().length > 0 && status.getSeverity() == IStatus.ERROR) { //only log errors, swallow warnings
			setErrorMessage("Error while retrieving changesets. See error log for details.");
			StatusHandler.log(status);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && (availableLogEntries.isEmpty() || !CrucibleUiUtil.hasCachedData(taskRepository))) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (availableLogEntries.isEmpty()) {
						updateRepositories();
						selectedTreeViewer.setInput(selectedLogEntries);
						validatePage();
					}
					if (!CrucibleUiUtil.hasCachedData(taskRepository)) {
						wizard.updateCache(CrucibleAddChangesetsPage.this);
					}
				}
			});
		}
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
			Set<RepositoryInfo> expandedRepositories = new HashSet<RepositoryInfo>();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof ICustomChangesetLogEntry) {
					RepositoryInfo repository = ((ICustomChangesetLogEntry) element).getRepository();
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
						if (!repositoryMappings.containsKey(repository.getScmPath())) {
							repositoryMappings.put(repository.getScmPath(), null);
						}
					} else {
						selectedLogEntries.remove(repository);
						//if its the last of that repo, remove it from mapping
						if (!selectedLogEntries.containsKey(repository)) {
							repositoryMappings.remove(repository.getScmPath());
							ComboViewer viewer = mappingCombos.remove(repository);
							if (viewer != null) {
								viewer.getCombo().dispose();
							}
						}
					}
					expandedRepositories.add(repository);
				}
			}
			selectedTreeViewer.setInput(selectedLogEntries);
			repositoriesMappingViewer.setInput(selectedLogEntries);
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
				} else if (allowChangesetsSelection && element instanceof RepositoryInfo) {
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

	public Map<RepositoryInfo, SortedSet<ICustomChangesetLogEntry>> getSelectedChangesets() {
		return selectedLogEntries;
	}

	public Map<String, String> getRepositoryMappings() {
		return repositoryMappings;
	}

	@Override
	public void setPageComplete(boolean complete) {
		TaskRepositoryUtil.setScmRepositoryMappings(taskRepository, repositoryMappings);
		super.setPageComplete(complete);
	}
}
