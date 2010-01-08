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

package com.atlassian.connector.eclipse.fisheye.ui.preferences;

import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SourceRepostioryMappingEditor {

	private final Composite parent;
	private final TableViewer tableViewer;
	private final ArrayList<FishEyeMappingConfiguration> urlToRepositories = MiscUtil.buildArrayList();
	private final List<ModifyListener> modifyListeners = MiscUtil.buildArrayList();

	public SourceRepostioryMappingEditor(Composite parent, TaskRepository repository) {
		this(parent, repository, SWT.NONE);
	}

	public void setRepositoryMappings(Collection<FishEyeMappingConfiguration> mapping) {
		this.urlToRepositories.clear();
		this.urlToRepositories.addAll(mapping);
		tableViewer.setInput(this.urlToRepositories);
	}

	public SourceRepostioryMappingEditor(Composite ancestor, final TaskRepository repository, int style) {
		parent = new Composite(ancestor, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);

		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableViewer.getControl());
		Composite panel = new Composite(parent, SWT.NONE);
		RowLayout panelLayout = new RowLayout(SWT.VERTICAL);
		panelLayout.fill = true;
		panel.setLayout(panelLayout);

		GridDataFactory.fillDefaults().grab(false, false).applyTo(panel);
		final Button addButton = new Button(panel, SWT.PUSH);
		addButton.setText("Add...");
		final Button editButton = new Button(panel, SWT.PUSH);
		editButton.setText("Edit...");
		final Button removeButton = new Button(panel, SWT.PUSH);
		removeButton.setText("Remove");

		final String[] titles = { "SCM Path", "Server", "Repository" };
		int[] bounds = { 300, 150, 100 };
		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
			column.getColumn().setText(titles[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
		}

		tableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return urlToRepositories.toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		tableViewer.setLabelProvider(new TableLabelProvider());

		Table tableControl = tableViewer.getTable();
		tableControl.setHeaderVisible(true);
		tableControl.setLinesVisible(true);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
					editButton.setEnabled(ssel.size() == 1);
					removeButton.setEnabled(ssel.size() > 0);
				}
			}
		});
		tableViewer.setInput(urlToRepositories);
		tableViewer.setSelection(null);
		addButton.setFocus();
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AddOrEditFishEyeMappingDialog dialog = new AddOrEditFishEyeMappingDialog(getControl().getShell(), repository, null, null);
				if (dialog.open() == Window.OK) {
					final FishEyeMappingConfiguration cfg = new FishEyeMappingConfiguration(dialog.getTaskRepository(),
							dialog.getScmPath(), dialog.getSourceRepository());
					if (cfg != null) {
						addOrEditMapping(cfg, null);
					}
				}
			}

		});
		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleEditMapping(tableViewer.getSelection());
			}
		});

		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection ssel = (IStructuredSelection) tableViewer.getSelection();
				for (Iterator<?> it = ssel.iterator(); it.hasNext();) {
					urlToRepositories.remove(it.next());
				}
				tableViewer.refresh();
				fireChangeListeners();
			}
		});

		tableViewer.getControl().setSize(tableViewer.getControl().computeSize(SWT.DEFAULT, 200));
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleEditMapping(event.getSelection());
			}
		});
	}

	private void handleEditMapping(ISelection aSelection) {
		if (aSelection instanceof IStructuredSelection) {
			Object selection = ((IStructuredSelection) aSelection).getFirstElement();
			if (selection instanceof FishEyeMappingConfiguration) {
				FishEyeMappingConfiguration oldMapping = (FishEyeMappingConfiguration) selection;
				AddOrEditFishEyeMappingDialog dialog = new AddOrEditFishEyeMappingDialog(getControl().getShell(),
						oldMapping.getTaskRepository(), oldMapping.getScmPath(), oldMapping.getFishEyeRepo());
				if (dialog.open() == Window.OK) {
					final FishEyeMappingConfiguration newMapping = new FishEyeMappingConfiguration(dialog.getTaskRepository(),
							dialog.getScmPath(), dialog.getSourceRepository());
					if (newMapping != null) {
						addOrEditMapping(newMapping, oldMapping);
					}
				}
			}
		}
	}

	public Control getControl() {
		return parent;
	}

	public Collection<FishEyeMappingConfiguration> getMapping() {
		return urlToRepositories;
	}

	public void addOrEditMapping(FishEyeMappingConfiguration mapping, FishEyeMappingConfiguration oldMapping) {
		if (oldMapping != null) {
			urlToRepositories.remove(oldMapping);
		}
		for (int i = 0, s = urlToRepositories.size(); i < s; ++i) {
			FishEyeMappingConfiguration m = urlToRepositories.get(i);
			if (m.getTaskRepository().equals(mapping.getTaskRepository())
					&& m.getScmPath().equals(mapping.getScmPath())) {
				urlToRepositories.set(i, mapping);
				return;
			}
		}
		this.urlToRepositories.add(mapping);
		this.tableViewer.refresh();
		fireChangeListeners();
	}

	public synchronized void addModifyListener(@NotNull ModifyListener iChangeListener) {
		modifyListeners.add(iChangeListener);
	}

	public synchronized void removeModifyListener(@NotNull ModifyListener iChangeListener) {
		int idx = modifyListeners.indexOf(iChangeListener);
		if (idx != -1) {
			modifyListeners.remove(idx);
		}
	}

	protected synchronized void fireChangeListeners() {
		for (ModifyListener l : modifyListeners) {
			Event event = new Event();
			event.widget = tableViewer.getControl();
			l.modifyText(new ModifyEvent(event));
		}
	}
}
