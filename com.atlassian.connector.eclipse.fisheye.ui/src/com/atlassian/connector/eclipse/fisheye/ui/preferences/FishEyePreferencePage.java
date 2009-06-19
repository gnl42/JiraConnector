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

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeUiPlugin;
import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
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
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */

public class FishEyePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private List<FishEyeMappingConfiguration> mapping;

	public static final String ID = "com.atlassian.connector.eclipse.fisheye.ui.preferences.FishEyePreferencePage";

	private TableViewer tableViewer;

	public FishEyePreferencePage() {
		super("FishEye Preferences");
		setDescription("Add, remove or edit FishEye mapping configuration.\n"
				+ "Mapping your local SCM repositories to a Fisheye Server Repository "
				+ "is necessary for looking up files in Fisheye correctly.");
		noDefaultAndApplyButton();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	public void applyData(Object data) {
		if (data instanceof FishEyePreferenceContextData) {
			final FishEyePreferenceContextData initialData = (FishEyePreferenceContextData) data;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					AddOrEditFishEyeMappingDialog dialog = new AddOrEditFishEyeMappingDialog(getShell(),
							new FishEyeMappingConfiguration(initialData.getScmPath(), null, null), getFishEyeServers(),
							FishEyeCorePlugin.getDefault().getRepositoryConnector().getClientManager(), true);
					if (dialog.open() == Window.OK) {
						final FishEyeMappingConfiguration cfg = dialog.getCfg();
						if (cfg != null) {
							mapping.add(cfg);
							tableViewer.refresh();
						}
					}
				}
			});
		}
	}

	private List<TaskRepository> getFishEyeServers() {
		List<TaskRepository> fishEyeLikeRepos = MiscUtil.buildArrayList();
		final List<TaskRepository> allRepositories = TasksUi.getRepositoryManager().getAllRepositories();
		for (TaskRepository taskRepository : allRepositories) {
			if (taskRepository.getConnectorKind().equals(FishEyeCorePlugin.CONNECTOR_KIND)
					|| CrucibleRepositoryConnector.isFishEye(taskRepository)) {
				fishEyeLikeRepos.add(taskRepository);
			}
		}
		return fishEyeLikeRepos;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}

	@Override
	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(ancestor);

		Composite parent = new Composite(ancestor, SWT.NONE);
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

		final List<FishEyeMappingConfiguration> mappingLive = FishEyeUiPlugin.getDefault()
				.getFishEyeSettingsManager()
				.getMappings();

		mapping = MiscUtil.buildArrayList(mappingLive.size());
		for (FishEyeMappingConfiguration cfg : mappingLive) {
			mapping.add(cfg.getClone());
		}

		final String[] titles = { "SCM Path", "FishEye Server", "FishEye Repository" };
		int[] bounds = { 100, 200, 100 };
		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
			column.getColumn().setText(titles[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
		}

		tableViewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				return mapping.toArray();
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
		tableViewer.setInput(mapping);
		tableViewer.setSelection(null);
		addButton.setFocus();
		addButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				AddOrEditFishEyeMappingDialog dialog = new AddOrEditFishEyeMappingDialog(getShell(),
						getFishEyeServers(), FishEyeCorePlugin.getDefault().getRepositoryConnector().getClientManager());
				if (dialog.open() == Window.OK) {
					final FishEyeMappingConfiguration cfg = dialog.getCfg();
					if (cfg != null) {
						mapping.add(cfg);
						tableViewer.refresh();
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
					mapping.remove(it.next());
				}
				tableViewer.refresh();
			}
		});

		tableViewer.getControl().setSize(tableViewer.getControl().computeSize(SWT.DEFAULT, 200));
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleEditMapping(event.getSelection());
			}
		});

		return ancestor;
	}

	private void handleEditMapping(ISelection aSelection) {
		if (aSelection instanceof IStructuredSelection) {
			Object selection = ((IStructuredSelection) aSelection).getFirstElement();
			if (selection instanceof FishEyeMappingConfiguration) {
				FishEyeMappingConfiguration mappingCfg = (FishEyeMappingConfiguration) selection;
				AddOrEditFishEyeMappingDialog dialog = new AddOrEditFishEyeMappingDialog(getShell(), mappingCfg,
						getFishEyeServers(), FishEyeCorePlugin.getDefault().getRepositoryConnector().getClientManager());
				if (dialog.open() == Window.OK) {
					final FishEyeMappingConfiguration cfg = dialog.getCfg();
					if (cfg != null) {
						int index = mapping.indexOf(mappingCfg);
						assert index >= 0;
						mapping.set(index, cfg);
						tableViewer.refresh();
					}
				}
			}
		}
	}

	@Override
	public boolean performOk() {
		try {
			FishEyeUiPlugin.getDefault().getFishEyeSettingsManager().setMappings(mapping);
			FishEyeUiPlugin.getDefault().getFishEyeSettingsManager().save();
		} catch (IOException e) {
			ErrorDialog.openError(getShell(), AtlassianUiPlugin.PRODUCT_NAME,
					"Error while saving FishEye mapping configuration", new Status(IStatus.ERROR,
							FishEyeUiPlugin.PLUGIN_ID, e.getMessage(), e));
			return false;
		}
		return super.performOk();
	}
}