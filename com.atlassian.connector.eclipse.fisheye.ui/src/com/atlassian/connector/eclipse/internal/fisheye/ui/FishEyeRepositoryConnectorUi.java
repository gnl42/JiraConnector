package com.atlassian.connector.eclipse.internal.fisheye.ui;

import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.TaskRepositoryLocationUiFactory;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;

public class FishEyeRepositoryConnectorUi extends AbstractRepositoryConnectorUi {

	public FishEyeRepositoryConnectorUi() {
		FishEyeCorePlugin.getDefault().getRepositoryConnector().getClientManager().setTaskRepositoryLocationFactory(
				new TaskRepositoryLocationUiFactory());
	}

	@Override
	public String getConnectorKind() {
		return FishEyeCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public IWizard getNewTaskWizard(TaskRepository taskRepository, ITaskMapping selection) {
		return null;
	}

	@Override
	public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
		return null;
	}

	@Override
	public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
		return new FishEyeRepositorySettingsPage(taskRepository);
	}

	@Override
	public boolean hasSearchPage() {
		return false;
	}

	@Override
	public String getTaskKindLabel(ITask task) {
		return "N/A";
	}

	@Override
	public String getAccountCreationUrl(TaskRepository taskRepository) {
		return taskRepository.getRepositoryUrl() + "/login/signup.do"; //$NON-NLS-1$
	}

	@Override
	public String getAccountManagementUrl(TaskRepository taskRepository) {
		return taskRepository.getRepositoryUrl() + "/profile/editDisplaySettings-default.do"; //$NON-NLS-1$
	}

}
