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

import com.atlassian.connector.eclipse.fisheye.ui.FishEyeUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleClientManager;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleCorePlugin;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;
import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeClientManager;
import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.FishEyeClientData;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.IClientDataProvider;
import com.atlassian.connector.eclipse.internal.fisheye.core.client.IUpdateRepositoryData;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeImages;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeUiPlugin;
import com.atlassian.connector.eclipse.team.ui.ScmRepository;
import com.atlassian.connector.eclipse.team.ui.TeamUiUtils;
import com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class AddOrEditFishEyeMappingDialog extends ProgressDialog {

	private final class UpdateRepositoryDataRunnable implements IRunnableWithProgress {

			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				final Object client;

				if (taskRepository.getConnectorKind().equals(FishEyeCorePlugin.CONNECTOR_KIND)) {
					client = fishEyeClientManager.getClient(taskRepository);
				} else {
					client = crucibleClientManager.getClient(taskRepository);
				}

				try {
					if (taskRepository.getConnectorKind().equals(FishEyeCorePlugin.CONNECTOR_KIND)) {
						((IUpdateRepositoryData) client).updateRepositoryData(monitor, taskRepository);
					} else {
						((IUpdateRepositoryData) client).updateRepositoryData(monitor, taskRepository);
					}

				} catch (final CoreException e) {
					StatusHandler.log(new Status(IStatus.ERROR, FishEyeUiPlugin.PLUGIN_ID, e.getMessage(), e));
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (!monitor.isCanceled()) {
								setErrorMessage(e.getMessage());
							}
						}
					});
					return;
				}

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (!monitor.isCanceled()) {
							final ISelection oldSelection = sourceRepositoryCombo.getSelection();
							sourceRepositoryCombo.setInput(getSortedRepositories(getRepositoriesFromClient((IClientDataProvider) client)));
							sourceRepositoryCombo.setSelection(oldSelection);
							setErrorMessage(null);
						}
					}
				});

			}

	}

	private final class ScmButtonSelectionListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			final ListDialog ld = new ListDialog(getShell());
			ld.setAddCancelButton(true);
			ld.setContentProvider(new ArrayContentProvider());
			ld.setLabelProvider(new LabelProvider() {
				@Override
				public Image getImage(Object element) {
					return FishEyeImages.getImage(FishEyeImages.REPOSITORY);
				}
			});
			if (scmRepositories != null) {
				ld.setInput(scmRepositories.toArray());
				ld.setTitle("Select SCM Repository");
				ld.setMessage("Select SCM repository in this workspace for this FishEye mapping.\n"
						+ "You can adjust it afterwards to narrow it down to the more specific path.");
				for (ScmRepository repositoryInfo : scmRepositories) {
					if (scmPathEdit.getText().equals(repositoryInfo.getScmPath())) {
						ld.setInitialSelections(new Object[] { repositoryInfo });
					}
				}
				if (ld.open() == Window.OK) {
					final Object[] result = ld.getResult();
					if (result != null && result.length > 0) {
						if (result[0] instanceof ScmRepository) {
							scmPathEdit.setText(((ScmRepository) result[0]).getScmPath());
						}
					}
				}
			}
		}
	}

	private final class OnShowHandler extends ShellAdapter {
		@Override
		public void shellActivated(ShellEvent event) {
			if (scmRepositories != null) {
				return;
			}
			try {
				run(true, false, new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Collecting data about workspace SCM repositories", IProgressMonitor.UNKNOWN);
						scmRepositories = TeamUiUtils.getRepositories(monitor);
						monitor.done();

						if (scmRepositories == null || scmRepositories.size() == 0) {
							getShell().getDisplay().asyncExec(new Runnable() {
								public void run() {
									setErrorMessage("You need to define workspace"
											+ " SCM repositories to enable FishEye mapping");
									scmButton.setEnabled(false);
								}
							});
						}
					}
				});
			} catch (InvocationTargetException e) {
				StatusHandler.log(new Status(IStatus.ERROR, FishEyeUiPlugin.PLUGIN_ID,
						"Cannot collect data about workspace SCM repositories", e.getCause()));
				setErrorMessage("Cannot collect data about workspace SCM repositories");
				return;
			} catch (InterruptedException e) {
				StatusHandler.log(new Status(IStatus.ERROR, FishEyeUiPlugin.PLUGIN_ID,
						"Cannot collect data about workspace SCM repositories", e));
				setErrorMessage("Cannot collect data about workspace SCM repositories");
				return;
			} finally {
				updateOkButtonState();
			}
		}
	}

	private ComboViewer taskRepositoryCombo;

	private ComboViewer sourceRepositoryCombo;

	private Button okButton;

	private Button scmButton;

	private Text scmPathEdit;

	private final Set<TaskRepository> taskRepositories = MiscUtil.buildHashSet();

	private final FishEyeClientManager fishEyeClientManager;

	private final CrucibleClientManager crucibleClientManager;

	private Collection<ScmRepository> scmRepositories;

	private Button updateServerDataButton;

	private final boolean isAddMode;

	private String scmPath;

	private String sourceRepository;

	private TaskRepository taskRepository;

	private boolean taskRepositoryEnabled = true;

	public boolean isTaskRepositoryEnabled() {
		return taskRepositoryEnabled;
	}

	public void setTaskRepositoryEnabled(boolean taskRepositoryEnabled) {
		this.taskRepositoryEnabled = taskRepositoryEnabled;
	}

	/**
	 * Creates dialog in "add" mode
	 */
	public AddOrEditFishEyeMappingDialog(Shell parentShell, String scmPath) {
		this(parentShell, null, scmPath, null);
	}

	/**
	 * Creates dialog in "add" or "edit" mode with initial selection
	 */
	public AddOrEditFishEyeMappingDialog(Shell parentShell, TaskRepository taskRepository, String scmPath,
			String sourceRepository) {
		super(parentShell);
		setTitleImage(FishEyeImages.getImage(FishEyeImages.FISHEYE_WIZ_BAN_ICON));
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.isAddMode = sourceRepository == null || taskRepository == null;

		for(TaskRepository tr : FishEyeUiUtil.getFishEyeAndCrucibleServers()) {
			if (!tr.isOffline()) {
				taskRepositories.add(tr);
			}
		}

		this.scmPath = scmPath;
		this.sourceRepository = sourceRepository;
		this.taskRepository = taskRepository;
		this.fishEyeClientManager = FishEyeCorePlugin.getDefault().getRepositoryConnector().getClientManager();
		CrucibleCorePlugin.getDefault();
		this.crucibleClientManager = CrucibleCorePlugin.getRepositoryConnector().getClientManager();
	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		return label;
	}

	@Override
	protected void okPressed() {
		final TaskRepository server = getTaskRepository();
		final String repo = getSourceRepository();
		if (server == null || repo == null) {
			return;
		}
		super.okPressed();
	}

	@Nullable
	public String getScmPath() {
		return scmPath;
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	@Nullable
	public String getSourceRepository() {
		return sourceRepository;
	}

	@Override
	protected Control createPageControls(Composite parent) {
		getShell().setText((isAddMode ? "Add" : "Edit") + " Mapping");
		setTitle("SCM to FishEye/Crucible Mapping");
		setMessage("Define how locally checked-out projects map to FishEye server and repository");
		GridLayout layout = new GridLayout(3, false);
		layout.makeColumnsEqualWidth = false;
		parent.setLayout(layout);
		createLabel(parent, "SCM Path:");
		scmPathEdit = new Text(parent, SWT.SINGLE | SWT.BORDER);
		scmPathEdit.setToolTipText("Locally used path (URL) to your source code versioning repository"
				+ " - currently only SVN is supported");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).hint(400, SWT.DEFAULT).applyTo(scmPathEdit);
		scmButton = new Button(parent, SWT.PUSH);
		scmButton.setText("...");
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(scmButton);
		scmButton.addSelectionListener(new ScmButtonSelectionListener());

		createLabel(parent, "Server:");
		taskRepositoryCombo = new ComboViewer(new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY));
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(taskRepositoryCombo.getControl());

		createLabel(parent, "Source Repository:");
		sourceRepositoryCombo = new ComboViewer(new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY));
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(sourceRepositoryCombo.getControl());
		sourceRepositoryCombo.setContentProvider(new ArrayContentProvider());
		sourceRepositoryCombo.setLabelProvider(new LabelProvider());

		updateServerDataButton = new Button(parent, SWT.PUSH);
		updateServerDataButton.setText("Update Repository Data");
		GridDataFactory.fillDefaults().grab(false, false).span(3, 1).align(SWT.RIGHT, SWT.FILL).applyTo(
				updateServerDataButton);

		taskRepositoryCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof TaskRepository) {
					TaskRepository taskRepository = (TaskRepository) element;
					return taskRepository.getRepositoryLabel() + " (" + taskRepository.getUrl() + ")";
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				return FishEyeImages.getImage(FishEyeImages.FISHEYE_ICON);
			}

		});
		taskRepositoryCombo.setContentProvider(new ArrayContentProvider());
		taskRepositoryCombo.setInput(taskRepositories.toArray());

		scmPathEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				scmPath = scmPathEdit.getText();
				updateOkButtonState();
			}
		});

		taskRepositoryCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					if (selection.getFirstElement() instanceof TaskRepository) {
						taskRepository = (TaskRepository) selection.getFirstElement();
						final IClientDataProvider client = taskRepository.getConnectorKind().equals(FishEyeCorePlugin.CONNECTOR_KIND) ?
							fishEyeClientManager.getClient(taskRepository) : crucibleClientManager.getClient(taskRepository);
						if (!client.hasRepositoryData()) {
							updateServerData(taskRepository);
						} else {
							sourceRepositoryCombo.setInput(getSortedRepositories(getRepositoriesFromClient(client)));
						}
					}
				}
				updateOkButtonState();
				updateServerRelatedControls();
			}
		});

		sourceRepositoryCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (sourceRepositoryCombo.getSelection() instanceof IStructuredSelection) {
					Object first = ((IStructuredSelection) sourceRepositoryCombo.getSelection()).getFirstElement();
					if (first != null && first instanceof String) {
						sourceRepository = (String) first;
					}
				}
				updateOkButtonState();
			}
		});

		updateServerDataButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (taskRepositoryCombo.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) taskRepositoryCombo.getSelection();
					if (selection.getFirstElement() instanceof TaskRepository) {
						final TaskRepository taskRepository = (TaskRepository) selection.getFirstElement();
						updateServerData(taskRepository);
					}
				}
				updateOkButtonState();
			}
		});

		if (scmPath != null) {
			scmPathEdit.setText(scmPath);
		}

		if (taskRepository != null) {
			taskRepositoryCombo.setSelection(new StructuredSelection(taskRepository));
		}

		if (sourceRepository != null) {
			sourceRepositoryCombo.setSelection(new StructuredSelection(sourceRepository));
		}

		getShell().addShellListener(new OnShowHandler());

		if (taskRepositories == null || taskRepositories.size() == 0) {
			taskRepositoryCombo.getControl().setEnabled(taskRepositories.size() > 0 && taskRepositoryEnabled);
			setMessage("Mapping cannot be defined. FishEye or Crucible server must be defined first.", IMessageProvider.WARNING);
		}

		updateServerRelatedControls();
		return parent;
	}

	private void updateOkButtonState() {
		boolean isEnabled = (getSourceRepository() != null && getTaskRepository() != null && scmPathEdit.getText()
				.length() > 0);
		if (okButton != null) {
			okButton.setEnabled(isEnabled);
		}
	}

	private void updateServerRelatedControls() {
		updateServerDataButton.setEnabled(getTaskRepository() != null);
		taskRepositoryCombo.getControl().setEnabled(taskRepositoryEnabled);
		sourceRepositoryCombo.getControl().setEnabled(getTaskRepository() != null);
	}

	private void updateServerData(final TaskRepository taskRepository) {
		try {
			sourceRepositoryCombo.getControl().setEnabled(false);
			taskRepositoryCombo.getControl().setEnabled(false);
			final Button button = getButton(IDialogConstants.OK_ID);
			if (button != null) {
				button.setEnabled(false);
			}
			//	getButton(IDialogConstants.OK_ID).setEnabled(false);
			run(true, true, new UpdateRepositoryDataRunnable());
		} catch (InvocationTargetException e) {
			if (e.getCause() != null) {
				setErrorMessage(e.getCause().getMessage());
			}
			StatusHandler.log(new Status(IStatus.ERROR, FishEyeUiPlugin.PLUGIN_ID, e.getMessage(), e));
		} catch(InterruptedException e) {
			// job interrupted by user so ignore it
		} catch (Exception e) {
			setErrorMessage(e.getMessage() != null ? e.getMessage() : "Exception:  " + e.getClass().getName());
			StatusHandler.log(new Status(IStatus.ERROR, FishEyeUiPlugin.PLUGIN_ID, "Failed to update task repository details", e));
		} finally {
			sourceRepositoryCombo.getControl().setEnabled(true);
			taskRepositoryCombo.getControl().setEnabled(taskRepositoryEnabled);
		}

	}

	@NotNull
	private String[] getSortedRepositories(Collection<String> unsortedRepos) {
		if (unsortedRepos == null) {
			return new String[0];
		}
		final String[] tmp = unsortedRepos.toArray(new String[0]);
		Arrays.sort(tmp);
		return tmp;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		updateOkButtonState();
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected Collection<String> getRepositoriesFromClient(IClientDataProvider client) {
		Collection<String> repositories = MiscUtil.buildArrayList();
		Object clientData = client.getClientData();
		if (clientData instanceof FishEyeClientData) {
			repositories.addAll(((FishEyeClientData) clientData).getCachedRepositories());
		} else if (clientData instanceof CrucibleClientData) {
			for(Repository repo : ((CrucibleClientData) clientData).getCachedRepositories()) {
				repositories.add(repo.getName());
			}
		}
		return repositories;
	}

}
