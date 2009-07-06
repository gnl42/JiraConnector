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

package com.atlassian.connector.eclipse.internal.bamboo.ui.actions;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.theplugin.commons.util.MiscUtil;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import java.util.Map;

public class RepositoryConfigurationAction extends Action implements IMenuCreator {
	private static final String ADD_TASK_REPOSITORY_COMMAND = "org.eclipse.mylyn.tasks.ui.command.addTaskRepository";

	private Menu menu;

	public RepositoryConfigurationAction() {
		initialize();
	}

	private void initialize() {
		setText("Add Bamboo Repository...");
		setImageDescriptor(BambooImages.ADD_REPOSITORY);
	}

	@Override
	public void run() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				openNewRepositoryWizard();
			}
		});
	}

	public void dispose() {
		if (menu != null) {
			menu.dispose();
			menu = null;
		}
	}

	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		addActions();
		return menu;
	}

	private void openNewRepositoryWizard() {
		final ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(
				ICommandService.class);
		final IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(
				IHandlerService.class);

		final Command addTaskRepositoryCommand = commandService.getCommand(ADD_TASK_REPOSITORY_COMMAND);
		final Map<String, String> parameters = MiscUtil.buildHashMap();
		parameters.put("connectorKind", BambooCorePlugin.CONNECTOR_KIND);

		try {
			addTaskRepositoryCommand.executeWithChecks(new ExecutionEvent(addTaskRepositoryCommand, parameters, null,
					handlerService.getCurrentState()));
		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, BambooCorePlugin.PLUGIN_ID, NLS.bind(
					"Failed to execute {0} command.", ADD_TASK_REPOSITORY_COMMAND), e));
		}

		/*
		NewRepositoryWizard repositoryWizard = new NewRepositoryWizard(BambooCorePlugin.CONNECTOR_KIND);

		WizardDialog repositoryDialog = new TaskRepositoryWizardDialog(null, repositoryWizard);
		repositoryDialog.create();
		repositoryDialog.getShell().setText("Add New Bamboo Repository...");
		repositoryDialog.setBlockOnOpen(true);
		repositoryDialog.open();
		*/
	}

	private void addActions() {
		// add repository action
		Action addRepositoryAction = new Action() {
			@Override
			public void run() {
				openNewRepositoryWizard();
			}
		};
		ActionContributionItem addRepoACI = new ActionContributionItem(addRepositoryAction);
		addRepositoryAction.setText("Add New Repository...");
		addRepositoryAction.setImageDescriptor(BambooImages.ADD_REPOSITORY);
		addRepoACI.fill(menu, -1);

		boolean separatorAdded = false;

		//open repository configuration action
		for (final TaskRepository repository : TasksUi.getRepositoryManager().getRepositories(
				BambooCorePlugin.CONNECTOR_KIND)) {
			if (!separatorAdded) {
				new Separator().fill(menu, -1);
				separatorAdded = true;
			}
			Action openRepositoryConfigurationAction = new Action() {
				@Override
				public void run() {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							TasksUiUtil.openEditRepositoryWizard(repository);
						}
					});
				}
			};
			ActionContributionItem openRepoConfigACI = new ActionContributionItem(openRepositoryConfigurationAction);
			openRepositoryConfigurationAction.setText(NLS.bind("Properties for {0}...", repository.getRepositoryLabel()));
			openRepoConfigACI.fill(menu, -1);
		}

		new Separator().fill(menu, -1);

		//goto repository action
		Action gotoTaskRepositoryViewAction = new Action() {
			@Override
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
									ITasksUiConstants.ID_VIEW_TASKS);
						} catch (PartInitException e) {
							StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
									"Failed to show Repositories View"));
						}
					}
				});
			}
		};
		ActionContributionItem gotoRepoViewACI = new ActionContributionItem(gotoTaskRepositoryViewAction);
		gotoTaskRepositoryViewAction.setText("Show Repositories View");
		gotoTaskRepositoryViewAction.setImageDescriptor(BambooImages.REPOSITORIES);
		gotoRepoViewACI.fill(menu, -1);
	}

	public Menu getMenu(Menu parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		addActions();
		return menu;
	}
}
