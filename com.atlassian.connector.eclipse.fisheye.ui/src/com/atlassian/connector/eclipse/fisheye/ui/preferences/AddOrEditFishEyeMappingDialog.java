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

import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeImages;
import com.atlassian.connector.eclipse.internal.fisheye.ui.FishEyeUiPlugin;
import com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.SWT;
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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;

public class AddOrEditFishEyeMappingDialog extends ProgressDialog {

	private final class FishEyeServerButtonHandler extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			final ListDialog ld = new ListDialog(getShell());
			ld.setAddCancelButton(true);
			ld.setContentProvider(new ArrayContentProvider());
			ld.setLabelProvider(new LabelProvider() {

				public Image getImage(Object element) {
					if (element instanceof TaskRepository) {
						TaskRepository taskRepo = (TaskRepository) element;
						if (taskRepo.getConnectorKind().equals(FishEyeCorePlugin.CONNECTOR_KIND)) {
							return FishEyeImages.getImage(FishEyeImages.FISHEYE_ICON);
						}
					}
					return null;
				}

				public String getText(Object element) {
					if (element instanceof TaskRepository) {
						TaskRepository taskRepo = (TaskRepository) element;
						return taskRepo.getRepositoryLabel() + " (" + taskRepo.getRepositoryUrl() + ")";
					}
					return element.toString();
				}

			});
			Set<TaskRepository> fishEyeTaskRepos = TasksUi.getRepositoryManager().getRepositories(
					FishEyeCorePlugin.CONNECTOR_KIND);
			if (fishEyeTaskRepos != null) {
				ld.setInput(fishEyeTaskRepos.toArray());
				ld.setTitle("Select FishEye Repository");
				if (ld.open() == Window.OK) {
					final Object[] result = ld.getResult();
					if (result != null && result.length > 0 /*&& result[0] instanceof*/) {
						fishEyeServerEdit.setText(result[0].toString());
					}
				}
			}

		}
	}

	private FishEyeMappingConfiguration cfg;

	private Text scmPathEdit;

	private Text fishEyeServerEdit;

	private Text fishEyeRepoEdit;

	protected AddOrEditFishEyeMappingDialog(Shell parentShell, FishEyeMappingConfiguration initialConfiguration) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		cfg = initialConfiguration;
	}

	protected AddOrEditFishEyeMappingDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	public FishEyeMappingConfiguration getCfg() {
		return cfg;
	}

//	@Override
//	protected Control createDialogArea(Composite ancestor) {
//	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		return label;
	}

	@Override
	protected void okPressed() {
		cfg = new FishEyeMappingConfiguration(scmPathEdit.getText(), fishEyeServerEdit.getText(),
				fishEyeRepoEdit.getText());
		super.okPressed();
	}

	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		new AddOrEditFishEyeMappingDialog(shell).open();
//		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(shell);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableViewer.getControl());
	}

	private Collection<String> repositories;

	private final IRunnableWithProgress runnable = new IRunnableWithProgress() {

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Collecting data about workspace SCM repositories", IProgressMonitor.UNKNOWN);
			repositories = TeamUiUtils.getRepositories(monitor);
			monitor.done();
//			Display.getDefault().asyncExec(new Runnable() {
//				public void run() {
//					progressMonitorDialog.close();
//				}
//			});
		}

	};

	@Override
	protected Control createPageControls(Composite parent) {
//		Composite parent = (Composite) super.createDialogArea(ancestor);
		getShell().setText("Add FishEye Mapping");
		setTitle("SCM to FishEye Mapping");
		setMessage("Define how locally checked-out projects map to FishEye server and repository");
		GridLayout layout = new GridLayout(3, false);
		layout.makeColumnsEqualWidth = false;
		parent.setLayout(layout);
		createLabel(parent, "SCM Path");
		scmPathEdit = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).hint(400, SWT.DEFAULT).applyTo(scmPathEdit);
		final Button scmButton = new Button(parent, SWT.PUSH);
		scmButton.setText("...");
		scmButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				final ListDialog ld = new ListDialog(getShell());
				ld.setAddCancelButton(true);
				ld.setContentProvider(new ArrayContentProvider());
				ld.setLabelProvider(new LabelProvider());
				if (repositories != null) {
					ld.setInput(repositories.toArray());
					ld.setTitle("Select SCM Repository");
					if (ld.open() == Window.OK) {
						final Object[] result = ld.getResult();
						if (result != null && result.length > 0) {
							scmPathEdit.setText(result[0].toString());
						}
					}
				}

			}

		});
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(scmButton);
		createLabel(parent, "FishEye Server URL");
		fishEyeServerEdit = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(fishEyeServerEdit);

		final Button fishEyeServerButton = new Button(parent, SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(fishEyeServerButton);
		fishEyeServerButton.setText("...");
		fishEyeServerButton.addSelectionListener(new FishEyeServerButtonHandler());

		createLabel(parent, "FishEye Repository");
		fishEyeRepoEdit = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(fishEyeRepoEdit);
		final ComboViewer fishEyeServerCombo = new ComboViewer(new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY));
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(fishEyeServerCombo.getControl());

		//		for (TaskRepository taskRepository : repositories) {
//			fishEyeServerCombo.add(taskRepository.getRepositoryLabel() + " (" + taskRepository.getUrl() + ")");
//
//		}
//
//		final ComboViewer fishEyeRepoCombo = new ComboViewer(new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY));
//		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(fishEyeRepoCombo.getControl());
//		fishEyeRepoCombo.setContentProvider(new ArrayContentProvider());
//
//		for (TaskRepository taskRepository : repositories) {
//			fishEyeServerCombo.add(taskRepository.getRepositoryLabel() + " (" + taskRepository.getUrl() + ")");
//
//		}
//
//		fishEyeServerCombo.addSelectionChangedListener(new ISelectionChangedListener() {
//
//			public void selectionChanged(SelectionChangedEvent event) {
//				if (event.getSelection() instanceof IStructuredSelection) {
//					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
//					fishEyeRepoCombo.getControl().setEnabled(false);
//					try {
//						run(true, true, new IRunnableWithProgress() {
//
//							public void run(IProgressMonitor monitor) throws InvocationTargetException,
//									InterruptedException {
//								Thread.sleep(1000);
//								Display.getDefault().asyncExec(new Runnable() {
//									public void run() {
//										if (new Random().nextBoolean()) {
//											fishEyeRepoCombo.setInput(new String[] { "afds", "xxxx" });
//										} else {
//											fishEyeRepoCombo.setInput(new String[] { "2afds", "2xxxx" });
//										}
//									}
//
//								});
//							}
//
//						});
//					} catch (InvocationTargetException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					fishEyeRepoCombo.getControl().setEnabled(true);
//				}
//			}
//
//		});

		getShell().addShellListener(new ShellAdapter() {

			public void shellActivated(ShellEvent event) {
				try {
					run(true, false, runnable);
//					progressMonitorDialog.run(true, false, runnable);
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
				}
			}

		});

		if (cfg != null) {
			scmPathEdit.setText(cfg.getScmPath());
			fishEyeServerEdit.setText(cfg.getFishEyeServer());
			fishEyeRepoEdit.setText(cfg.getFishEyeRepo());
		}

		return parent;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

}
