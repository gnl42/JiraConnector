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
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Page for adding files to the review
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleAddFilesPage extends WizardPage {

	private class GetMultipleRevisionsRunnable implements IRunnableWithProgress {
		private Map<IFile, SortedSet<Long>> revisions;

		private CoreException exception;

		private final java.util.List<IFile> files;

		public GetMultipleRevisionsRunnable(java.util.List<IFile> files) {
			this.files = files;
		}

		public Map<IFile, SortedSet<Long>> getRevisions() {
			return revisions;
		}

		public CoreException getException() {
			return exception;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			try {
				revisions = TeamUiUtils.getRevisionsForFiles(files, monitor);
			} catch (CoreException e) {
				exception = e;
			}
		}
	}

	private class GetRevisionsRunnable implements IRunnableWithProgress {
		private SortedSet<Long> revisions;

		private CoreException exception;

		private final IFile file;

		public GetRevisionsRunnable(IFile file) {
			this.file = file;
		}

		public SortedSet<Long> getRevisions() {
			return revisions;
		}

		public CoreException getException() {
			return exception;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			try {
				revisions = TeamUiUtils.getRevisionsForFile(file, monitor);
			} catch (CoreException e) {
				exception = e;
			}
		}
	}

	private TreeViewer treeViewer;

	private final Map<IFile, SortedSet<Long>> selectedFiles = new HashMap<IFile, SortedSet<Long>>();

	private final Map<IFile, Long> selectedCompareRevisions = new HashMap<IFile, Long>();

	private final Map<IFile, Long> selectedFileRevisions = new HashMap<IFile, Long>();

	private final Map<IFile, SortedSet<Long>> revisionsCache = new HashMap<IFile, SortedSet<Long>>();

	private ListViewer selectedFilesViewer;

	private List selectedFilesList;

	private ComboViewer revisionComboViewer;

	private ComboViewer diffComboViewer;

	public CrucibleAddFilesPage() {
		super("crucibleFiles");
		setTitle("Add Files to Review");
		setDescription("Select the files you want to have reviewed.");
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());

		Label label = new Label(composite, SWT.NONE);
		label.setText("Select files from your workspace:");

		new Label(composite, SWT.NONE).setText("");

		new Label(composite, SWT.NONE).setText("Files selected for the review:");

		GridDataFactory.fillDefaults().applyTo(label);
		Tree tree = new Tree(composite, SWT.MULTI | SWT.BORDER);
		treeViewer = new TreeViewer(tree);

		GridDataFactory.fillDefaults().grab(true, true).span(1, 2).hint(300, SWT.DEFAULT).applyTo(tree);
		treeViewer.setLabelProvider(new WorkbenchLabelProvider());
		treeViewer.setContentProvider(new WorkbenchContentProvider());
		treeViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
		treeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		final Menu contextMenuSource = new Menu(getShell(), SWT.POP_UP);
		tree.setMenu(contextMenuSource);
		MenuItem addFile = new MenuItem(contextMenuSource, SWT.PUSH);
		addFile.setText("Add");
		addFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addFiles();
			}
		});

		createButtonComp(composite);

		createSelectedFileComp(composite);

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	private void createSelectedFileComp(Composite composite) {
		selectedFilesList = new List(composite, SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).hint(300, SWT.DEFAULT).applyTo(selectedFilesList);
		selectedFilesViewer = new ListViewer(selectedFilesList);
		selectedFilesViewer.setContentProvider(new ITreeContentProvider() {
			public Object[] getChildren(Object parentElement) {
				return null;
			}

			public Object getParent(Object element) {
				return null;
			}

			public boolean hasChildren(Object element) {
				return false;
			}

			public Object[] getElements(Object inputElement) {
				return selectedFiles.keySet().toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		selectedFilesViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IFile) {
					return ((IFile) element).getFullPath().toString();
				} else {
					return super.getText(element);
				}
			}
		});
		selectedFilesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ArrayList<IFile> selectedFilesRight = getSelectedFilesTarget();
				if (selectedFilesRight.size() == 1) {
					updateRevisionsControls();
				}
			}
		});
		final Menu contextMenuTarget = new Menu(getShell(), SWT.POP_UP);
		selectedFilesList.setMenu(contextMenuTarget);
		MenuItem removeFile = new MenuItem(contextMenuTarget, SWT.PUSH);
		removeFile.setText("Remove");
		removeFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeFiles();
			}
		});
		MenuItem selectRevision = new MenuItem(contextMenuTarget, SWT.PUSH);
		selectRevision.setText("Update Revisions");
		selectRevision.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				java.util.List<IFile> selected = getSelectedFilesTarget();
				if (selected.size() > 0) {
					final IFile file = selected.get(0);
					updateRevision(file);
				}
			}
		});
		selectedFilesList.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				contextMenuTarget.dispose();
			}
		});

		createRevisionsComp(composite);
	}

	private void createRevisionsComp(Composite composite) {
		Composite revisionsComp = new Composite(composite, SWT.NONE);
		revisionsComp.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(revisionsComp);

		new Label(revisionsComp, SWT.NONE).setText("Revision:");

		CCombo revisionCombo = new CCombo(revisionsComp, SWT.BORDER);
		revisionComboViewer = new ComboViewer(revisionCombo);
		revisionComboViewer.setContentProvider(new IStructuredContentProvider() {

			public void dispose() {
				// ignore
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// ignore
			}

			@SuppressWarnings("unchecked")
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof SortedSet) {
					return ((SortedSet) inputElement).toArray();
				}
				return new Object[0];
			}

		});
		revisionComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Long) {
					return element.toString();
				}
				return super.getText(element);
			}
		});
		revisionComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleDiffSelection(null, revisionComboViewer, selectedFileRevisions);
			}
		});

		final Button compareButton = new Button(revisionsComp, SWT.CHECK);
		compareButton.setText("Compare with Revision:");
		compareButton.setSelection(false);
		compareButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				diffComboViewer.getCCombo().setEnabled(compareButton.getSelection());
				handleDiffSelection(compareButton, diffComboViewer, selectedCompareRevisions);
			}
		});

		CCombo diffCombo = new CCombo(revisionsComp, SWT.BORDER);
		diffCombo.setEnabled(false);
		diffComboViewer = new ComboViewer(diffCombo);
		diffComboViewer.setContentProvider(new IStructuredContentProvider() {

			public void dispose() {
				// ignore
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// ignore
			}

			@SuppressWarnings("unchecked")
			public Object[] getElements(Object inputElement) {
				// ignore
				if (inputElement instanceof SortedSet) {
					return ((SortedSet) inputElement).toArray();
				}
				return new Object[0];
			}

		});
		diffComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Long) {
					return element.toString();
				}
				return super.getText(element);
			}
		});
		diffComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleDiffSelection(compareButton, diffComboViewer, selectedCompareRevisions);
			}
		});

	}

	private void createButtonComp(Composite composite) {
		Composite buttonComp = new Composite(composite, SWT.NONE);
		buttonComp.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());
		GridDataFactory.fillDefaults().span(1, 2).grab(false, true).applyTo(buttonComp);

		Button addButton = new Button(buttonComp, SWT.PUSH);
		addButton.setText("Add -->");
		addButton.setToolTipText("Add all selected files");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addFiles();
			}
		});
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(addButton);

		Button removeButton = new Button(buttonComp, SWT.PUSH);
		removeButton.setText("<-- Remove");
		removeButton.setToolTipText("Remove all selected files");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeFiles();
			}

		});
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(removeButton);
	}

	private void updateRevisionsControls() {
		java.util.List<IFile> selectedTargetFiles = getSelectedFilesTarget();
		if (selectedTargetFiles.size() == 1) {
			IFile selection = selectedTargetFiles.get(0);
			SortedSet<Long> revisions = selectedFiles.get(selection);
			if (revisions != null) {
				revisionComboViewer.setInput(selectedFiles.get(selection));
				diffComboViewer.setInput(selectedFiles.get(selection));
				//select head
				if (revisions.size() > 0) {
					Long selectedRevision = selectedFileRevisions.get(selection);
					if (selectedRevision == null) {
						selectedRevision = revisions.last();
					}
					revisionComboViewer.setSelection(new StructuredSelection(selectedRevision));
					selectedRevision = selectedCompareRevisions.get(selection);
					if (selectedRevision == null) {
						selectedRevision = revisions.last();
					}
					diffComboViewer.setSelection(new StructuredSelection(selectedRevision));
				}
			}
		}
	}

	@Override
	public boolean isPageComplete() {
		return true;
	}

	private ArrayList<IFile> getSelectedFilesSource() {
		ArrayList<IFile> selected = new ArrayList<IFile>();
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			Iterator<?> it = ((IStructuredSelection) selection).iterator();
			while (it.hasNext()) {
				Object element = it.next();
				if (element instanceof IFile) {
					selected.add((IFile) element);
				}
			}
		}
		return selected;
	}

	private ArrayList<IFile> getSelectedFilesTarget() {
		ArrayList<IFile> selected = new ArrayList<IFile>();
		ISelection selection = selectedFilesViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			Iterator<?> it = ((IStructuredSelection) selection).iterator();
			while (it.hasNext()) {
				Object element = it.next();
				if (element instanceof IFile) {
					selected.add((IFile) element);
				}
			}
		}
		return selected;
	}

	private Long getSelectedRevision(ComboViewer viewer) {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			if (((IStructuredSelection) selection).getFirstElement() instanceof Long) {
				return (Long) ((IStructuredSelection) selection).getFirstElement();
			}
		}
		return -1L;
	}

	private void addFiles() {
		ArrayList<IFile> selectedFilesSource = getSelectedFilesSource();
		for (IFile selected : selectedFilesSource) {
			selectedFiles.put(selected, new TreeSet<Long>());
		}
		selectedFilesViewer.setInput(selectedFiles.keySet());
		updateRevision(selectedFilesSource);
	}

	private void removeFiles() {
		for (IFile selected : getSelectedFilesSource()) {
			selectedFiles.remove(selected);
		}
		selectedFilesViewer.setInput(selectedFiles.keySet());
	}

	private void updateRevision(final java.util.List<IFile> files) {
		//check if some files are already cached
		java.util.List<IFile> filesToUpdate = new ArrayList<IFile>();
		for (IFile file : files) {
			if (revisionsCache.containsKey(file) && revisionsCache.get(file) != null) {
				selectedFiles.put(file, revisionsCache.get(file));
			} else {
				filesToUpdate.add(file);
			}
		}
		if (filesToUpdate.size() > 0) {
			GetMultipleRevisionsRunnable revisionsRunnable = new GetMultipleRevisionsRunnable(filesToUpdate);
			Exception exception;
			try {
				getContainer().run(true, false, revisionsRunnable);
				exception = revisionsRunnable.getException();
			} catch (Exception ex) {
				exception = ex;
			}
			Map<IFile, SortedSet<Long>> revisions = revisionsRunnable.getRevisions();
			if (exception != null || revisions.size() == 0) {
				setErrorMessage("Could not retrieve revisions for selected file. See error log for details");
				StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID,
						"Failed to retrieve revisions", exception));
				return;
			}

			selectedFiles.putAll(revisions);
			revisionsCache.putAll(revisions);
		}
		//fill revision control
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				updateRevisionsControls();
			}
		});
	}

	private void updateRevision(final IFile file) {
		GetRevisionsRunnable revisionsRunnable = new GetRevisionsRunnable(file);
		Exception exception;
		try {
			getContainer().run(true, false, revisionsRunnable);
			exception = revisionsRunnable.getException();
		} catch (Exception ex) {
			exception = ex;
		}
		SortedSet<Long> revisions = revisionsRunnable.getRevisions();
		if (exception != null || revisions.size() == 0) {
			setErrorMessage("Could not retrieve revisions for selected file. See error log for details");
			StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, "Failed to retrieve revisions",
					exception));
			return;
		}

		selectedFiles.put(file, revisions);
		revisionsCache.put(file, revisions);
		//fill revision control
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				updateRevisionsControls();
			}
		});
	}

	private void handleDiffSelection(final Button compareButton, ComboViewer viewer, Map<IFile, Long> map) {
		java.util.List<IFile> selectedFile = getSelectedFilesTarget();
		if ((compareButton == null || compareButton.getSelection()) && viewer.getCCombo().getSelectionIndex() != -1) {
			Long selectedRev = getSelectedRevision(viewer);
			if (selectedFile.size() == 1 && selectedRev > -1L) {
				map.put(selectedFile.get(0), selectedRev);
			}
		}
		if (compareButton != null && !compareButton.getSelection()) {
			map.remove(selectedFile);
		}
	}

}
