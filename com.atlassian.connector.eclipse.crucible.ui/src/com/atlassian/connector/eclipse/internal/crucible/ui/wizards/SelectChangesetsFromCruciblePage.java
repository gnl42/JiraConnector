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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.commons.fisheye.FishEyeServerFacade2;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleClientManager;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.TaskRepositoryUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleRemoteOperation;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeClientManager;
import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeClient;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeRemoteOperation;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeImages;
import com.atlassian.connector.eclipse.team.ui.ICustomChangesetLogEntry;
import com.atlassian.connector.eclipse.ui.viewers.ArrayTreeContentProvider;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.changes.Change;
import com.atlassian.theplugin.commons.crucible.api.model.changes.Changes;
import com.atlassian.theplugin.commons.crucible.api.model.changes.Revision;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.fisheye.api.model.changeset.Changeset;
import com.atlassian.theplugin.commons.fisheye.api.model.changeset.ChangesetIdList;
import com.atlassian.theplugin.commons.fisheye.api.model.changeset.FileRevisionKey;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * Page for selecting changeset for the new review
 * 
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 */
public class SelectChangesetsFromCruciblePage extends AbstractCrucibleWizardPage {

	private static final int LIMIT = 25;

	private static final String EMPTY_NODE = "No changesets available.";

	private final class GetChangesetsRunnable implements IRunnableWithProgress {
		private final int numberToRetrieve;

		private final IStatus[] status;

		private final Repository repository;

		private GetChangesetsRunnable(int numberToRetrieve, IStatus[] status, Repository repository) {
			this.numberToRetrieve = numberToRetrieve;
			this.status = status;
			this.repository = repository;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			try {
				if (repository.getType().equals(Repository.PLUGIN_TYPE)) {
					SubMonitor submonitor = SubMonitor.convert(monitor, "Gettings changesets from Crucible", 1);
					CrucibleClientManager mgr = CrucibleCorePlugin.getRepositoryConnector().getClientManager();
					CrucibleClient client = mgr.getClient(getTaskRepository());
					Changes changes = client.execute(new CrucibleRemoteOperation<Changes>(submonitor.newChild(1),
							getTaskRepository()) {
						@Override
						public Changes run(CrucibleServerFacade2 server, ConnectionCfg serverCfg,
								IProgressMonitor monitor) throws RemoteApiException, ServerPasswordNotProvidedException {
							return server.getSession(serverCfg).getChanges(repository.getName(), null, false, null,
									false, Integer.valueOf(numberToRetrieve));
						}
					});
					availableChanges.put(repository, changes);
				} else {
					SubMonitor submonitor = SubMonitor.convert(monitor, "Getting changesets from FishEye", 2);
					// that's FishEye repository
					FishEyeClientManager mgr = FishEyeCorePlugin.getDefault()
							.getRepositoryConnector()
							.getClientManager();
					FishEyeClient client = mgr.getClient(getTaskRepository());
					final ChangesetIdList csids = client.execute(new FishEyeRemoteOperation<ChangesetIdList>(
							submonitor.newChild(1), getTaskRepository()) {
						@Override
						public ChangesetIdList run(FishEyeServerFacade2 server, ConnectionCfg serverCfg,
								IProgressMonitor monitor) throws RemoteApiException, ServerPasswordNotProvidedException {
							FishEyeSession session = server.getSession(serverCfg);
							session.login(serverCfg.getUsername(), serverCfg.getPassword().toCharArray());

							return session.getChangesetList(repository.getName(), null, null, null,
									Integer.valueOf(numberToRetrieve));
						}
					});

					List<Changeset> changesets = client.execute(new FishEyeRemoteOperation<List<Changeset>>(
							submonitor.newChild(1), getTaskRepository()) {
						@Override
						public List<Changeset> run(FishEyeServerFacade2 server, ConnectionCfg serverCfg,
								IProgressMonitor monitor) throws RemoteApiException, ServerPasswordNotProvidedException {
							SubMonitor submonitor = SubMonitor.convert(monitor, csids.getCsids().size());
							FishEyeSession session = server.getSession(serverCfg);
							session.login(serverCfg.getUsername(), serverCfg.getPassword().toCharArray());

							List<Changeset> result = MiscUtil.buildArrayList();
							for (String csid : csids.getCsids()) {
								result.add(session.getChangeset(repository.getName(), csid));
								submonitor.worked(1);
							}
							return result;
						}
					});

					List<Change> changes = MiscUtil.buildArrayList();
					for (Changeset cs : changesets) {
						List<Revision> revisions = MiscUtil.buildArrayList();
						for (FileRevisionKey rev : cs.getRevisionKeys()) {
							revisions.add(new Revision(rev.getRev(), rev.getPath(), null));
						}
						changes.add(new Change(cs.getAuthor(), cs.getDate(), cs.getCsid(), null, cs.getComment(),
								revisions));
					}
					availableChanges.put(repository, new Changes(false, false, changes));
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						availableTreeViewer.refresh(repository);
					}
				});
			} catch (CoreException e) {
				status[0] = e.getStatus();
			}
		}
	}

	private static class ChangesetLabelProvider extends LabelProvider {
		private static final int COMMENT_PREVIEW_LENGTH = 50;

		@Override
		public Image getImage(Object element) {
			if (element == null) {
				return null;
			}
			if (element instanceof Repository) {
				return FishEyeImages.getImage(FishEyeImages.REPOSITORY);
			} else if (element instanceof Change) {
				return CommonImages.getImage(CrucibleImages.CHANGESET);
			} else if (element == EMPTY_NODE) {
				return null;
			} else if (element instanceof String) {
				return CommonImages.getImage(CrucibleImages.FILE);
			}
			return super.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof Repository) {
				return ((Repository) element).getName();
			}
			if (element instanceof Change) {
				return changeLabel((Change) element);
			}
			if (element instanceof Revision) {
				return ((Revision) element).getPath();
			}
			return super.getText(element);
		}

		public static String changeLabel(Change element) {
			String shortComment = (element).getComment();
			if (shortComment.length() > COMMENT_PREVIEW_LENGTH) {
				shortComment = shortComment.substring(0, COMMENT_PREVIEW_LENGTH) + "...";
			}
			return (element).getCsid() + " [" + (element).getAuthor() + "] - " + shortComment.replace("\n", " ");
		}
	}

	private class ChangesetContentProvider extends ArrayTreeContentProvider {
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Repository) {
				Changes changes = availableChanges.get(parentElement);
				if (changes == null || changes.getChanges().size() == 0) {
					return new String[] { EMPTY_NODE };
				}
				return changes.getChanges().toArray();
			}
			if (parentElement instanceof Change) {
				return ((Change) parentElement).getRevisions().toArray();
			}
			return super.getChildren(parentElement);
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof Repository || element instanceof Change) {
				return true;
			}
			return super.hasChildren(element);
		}

	}

	private final Map<Repository, Changes> availableChanges = MiscUtil.buildHashMap();

	private final Map<Repository, Set<Change>> selectedChanges = MiscUtil.buildHashMap();

	private TreeViewer availableTreeViewer;

	private TreeViewer selectedTreeViewer;

	private Button addButton;

	private Button removeButton;

	private MenuItem removeChangesetMenuItem;

	private MenuItem getNextRevisionsMenuItem;

	private MenuItem addChangesetMenuItem;

	private final TaskRepository taskRepository;

	public SelectChangesetsFromCruciblePage(@NotNull TaskRepository repository) {
		this(repository, new TreeSet<ICustomChangesetLogEntry>());
	}

	public SelectChangesetsFromCruciblePage(@NotNull TaskRepository repository,
			@Nullable SortedSet<ICustomChangesetLogEntry> logEntries) {
		super("crucibleChangesets"); //$NON-NLS-1$
		setTitle("Select Changesets from Crucible");
		setDescription("Select the changesets that should be included in the review. Changesets information is provided by Crucible.");

		this.taskRepository = repository;

		if (logEntries != null && logEntries.size() > 0) {
			Map<String, String> mappings = TaskRepositoryUtil.getScmRepositoryMappings(repository);
			for (ICustomChangesetLogEntry logEntry : logEntries) {
				Entry<String, String> mapping = TaskRepositoryUtil.getMatchingSourceRepository(mappings,
						logEntry.getRepository().getScmPath());
				if (mappings != null) {
					if (!selectedChanges.containsKey(mapping.getValue())) {
						selectedChanges.put(new Repository(mapping.getValue(), null, true), new HashSet<Change>());
					}
					selectedChanges.get(mapping.getValue()).add(
							new Change(logEntry.getAuthor(), logEntry.getDate(), logEntry.getRevision(), null,
									logEntry.getComment(), null));
				}
			}
		}
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(5, 5).create());

		Label label = new Label(composite, SWT.NONE);
		label.setText("Select changesets from your repositories:");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		new Label(composite, SWT.NONE).setText("Changesets selected for the review:");

		createChangesViewer(composite);

		createButtonComp(composite);

		createSelectedChangesViewer(composite);

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	/*
	 * checks if page is complete updates the buttons
	 */
	public void validatePage() {
		setErrorMessage(null);

		setPageComplete(true);
		getContainer().updateButtons();
	}

	private void createChangesViewer(Composite parent) {
		Tree tree = new Tree(parent, SWT.MULTI | SWT.BORDER);
		availableTreeViewer = new TreeViewer(tree);

		GridDataFactory.fillDefaults().grab(true, true).hint(300, 220).applyTo(tree);
		availableTreeViewer.setLabelProvider(new ChangesetLabelProvider());
		availableTreeViewer.setContentProvider(new ChangesetContentProvider());
		availableTreeViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
		availableTreeViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof Change && e2 instanceof Change) {
					return ((Change) e2).getDate().compareTo(((Change) e1).getDate());
				}
				return super.compare(viewer, e1, e2);
			}
		});

		tree.setMenu(createChangesContextMenu());

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
				if (object instanceof Repository) {
					refreshRepository((Repository) object);
				}

			}
		});
		availableTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object object = selection.getFirstElement();
				if (availableTreeViewer.isExpandable(object)) {
					if (!availableTreeViewer.getExpandedState(object) && object instanceof Repository) {
						refreshRepository((Repository) object);
						return;
					}
					availableTreeViewer.setExpandedState(object, !availableTreeViewer.getExpandedState(object));
				}
			}
		});
	}

	private Menu createChangesContextMenu() {
		final Menu contextMenuSource = new Menu(getShell(), SWT.POP_UP);

		addChangesetMenuItem = new MenuItem(contextMenuSource, SWT.PUSH);
		addChangesetMenuItem.setText("Add to Review");

		new MenuItem(contextMenuSource, SWT.SEPARATOR);

		addChangesetMenuItem.setEnabled(false);
		getNextRevisionsMenuItem = new MenuItem(contextMenuSource, SWT.PUSH);
		getNextRevisionsMenuItem.setText(String.format("Get %d More Revisions", LIMIT));
		getNextRevisionsMenuItem.setEnabled(false);
		getNextRevisionsMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeSelection selection = getTreeSelection(availableTreeViewer);
				if (selection != null && selection.getPaths() != null) {
					for (TreePath path : selection.getPaths()) {
						Repository repository = (Repository) path.getFirstSegment();
						if (repository != null) {
							Changes changes = availableChanges.get(repository);
							int currentNumberOfEntries = changes == null ? 0 : changes.getChanges().size();
							updateChangesets(repository, currentNumberOfEntries + LIMIT);
						}
					}
				} else {
					// update all
					for (Repository repository : availableChanges.keySet()) {
						Changes changes = availableChanges.get(repository);
						int currentNumberOfEntries = changes == null ? 0 : changes.getChanges().size();
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
		return contextMenuSource;
	}

	private void refreshRepository(final Repository object) {
		Changes changes = availableChanges.get(object);
		if (changes == null) {
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

	private void createSelectedChangesViewer(Composite composite) {
		Tree tree = new Tree(composite, SWT.MULTI | SWT.BORDER);
		selectedTreeViewer = new TreeViewer(tree);

		GridDataFactory.fillDefaults().grab(true, true).hint(300, 220).applyTo(tree);
		selectedTreeViewer.setLabelProvider(new ChangesetLabelProvider());
		selectedTreeViewer.setContentProvider(new ITreeContentProvider() {
			private Map<String, Set<String>> currentMap;

			public Object[] getChildren(Object parentElement) {
				return currentMap.get(parentElement).toArray();
			}

			public Object[] getElements(Object inputElement) {
				return currentMap.keySet().toArray();
			}

			public boolean hasChildren(Object element) {
				return currentMap.containsKey(element);
			}

			@SuppressWarnings("unchecked")
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				this.currentMap = (Map<String, Set<String>>) newInput;
			}

			public Object getParent(Object element) {
				return null;
			}

			public void dispose() {
			}
		});
		selectedTreeViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));

		tree.setMenu(createSelectedChangesMenu());
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonEnablement();
			}
		});
	}

	private Menu createSelectedChangesMenu() {
		final Menu contextMenuSource = new Menu(getShell(), SWT.POP_UP);

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
		return contextMenuSource;
	}

	private void updateButtonEnablement() {
		// right viewer
		TreeSelection selection = validateTreeSelection(selectedTreeViewer);
		removeButton.setEnabled(selection != null && !selection.isEmpty());
		removeChangesetMenuItem.setEnabled(selection != null && !selection.isEmpty());

		// left viewer
		selection = validateTreeSelection(availableTreeViewer);
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
				if (element instanceof Repository) {
					return false;
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean hasAlreadyChosenChangesetSelected(TreeSelection selection) {
		for (Repository repository : selectedChanges.keySet()) {
			Iterator<Object> iterator = (selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof Change) {
					if (selectedChanges.get(repository).contains(element)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void downloadRepositoriesFromCrucible(boolean force) {
		if (force || CrucibleUiUtil.getCachedRepositories(getTaskRepository()).size() == 0) {
			CrucibleUiUtil.updateTaskRepositoryCache(getTaskRepository(), getContainer(), this);
		}

		availableTreeViewer.setInput(CrucibleUiUtil.getCachedRepositories(getTaskRepository()));
	}

	private void updateChangesets(final Repository repository, final int numberToRetrieve) {
		final IStatus[] status = { Status.OK_STATUS };

		IRunnableWithProgress getChangesets = new GetChangesetsRunnable(numberToRetrieve, status, repository);

		try {
			setErrorMessage(null);
			getContainer().run(false, true, getChangesets); // blocking operation
		} catch (Exception e) {
			status[0] = new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, e.getMessage(), e);
		}

		if (!status[0].isOK()) { //only log errors, swallow warnings
			setErrorMessage(String.format("Error while retrieving changesets (%s). See Error Log for details.",
					status[0].getMessage()));
			StatusHandler.log(status[0]);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && (availableChanges.isEmpty() || !CrucibleUiUtil.hasCachedData(getTaskRepository()))) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (availableChanges.isEmpty()) {
						downloadRepositoriesFromCrucible(false);
						selectedTreeViewer.setInput(selectedChanges);
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
		if (selection != null && selection.getPaths() != null) {
			Object[] expanded = selectedTreeViewer.getExpandedElements();

			for (TreePath path : selection.getPaths()) {
				if (path.getSegmentCount() < 2) {
					continue;
				}

				Repository repository = (Repository) path.getFirstSegment();
				Change change = (Change) path.getSegment(1);

				if (!selectedChanges.containsKey(repository)) {
					selectedChanges.put(repository, new HashSet<Change>());
				}
				selectedChanges.get(repository).add(change);
			}

			selectedTreeViewer.setInput(selectedChanges);
			selectedTreeViewer.setExpandedElements(expanded);
		}
		validatePage();
	}

	private void removeChangesets() {
		TreeSelection selection = getTreeSelection(selectedTreeViewer);
		if (selection != null && selection.getPaths() != null) {
			Object[] expanded = selectedTreeViewer.getExpandedElements();

			for (TreePath path : selection.getPaths()) {
				Repository repository = (Repository) path.getFirstSegment();
				Change change = path.getSegmentCount() > 1 ? (Change) path.getSegment(1) : null;

				if (selectedChanges.containsKey(repository)) {
					if (change != null) {
						Set<Change> csids = selectedChanges.get(repository);
						csids.remove(change);
						if (csids.size() == 0) {
							selectedChanges.remove(repository);
						}
					} else {
						selectedChanges.remove(repository);
					}
				}
			}

			selectedTreeViewer.setInput(selectedChanges);
			selectedTreeViewer.setExpandedElements(expanded);
		}
		validatePage();
	}

	@SuppressWarnings("unchecked")
	private TreeSelection validateTreeSelection(TreeViewer treeViewer) {
		TreeSelection selection = getTreeSelection(treeViewer);
		if (selection != null) {
			ArrayList<TreePath> validSelections = new ArrayList<TreePath>();
			Iterator<Object> iterator = (selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof Change) {
					validSelections.add((selection).getPathsFor(element)[0]);
				} else if (element instanceof Repository) {
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
		for (Map.Entry<Repository, Set<Change>> changes : selectedChanges.entrySet()) {
			Set<String> csids = MiscUtil.buildHashSet();
			for (Change change : changes.getValue()) {
				csids.add(change.getCsid());
			}
			result.put(changes.getKey().getName(), csids);
		}
		return result;
	}
}
