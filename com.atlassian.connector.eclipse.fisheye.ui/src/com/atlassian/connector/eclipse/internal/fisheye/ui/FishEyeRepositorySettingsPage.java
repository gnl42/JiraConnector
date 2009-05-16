package com.atlassian.connector.eclipse.internal.fisheye.ui;

import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Composite;

import java.net.MalformedURLException;
import java.net.URL;

public class FishEyeRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private class FishEyeValidator extends Validator {

		public FishEyeValidator(TaskRepository repository) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			// TODO Auto-generated method stub

		}

	}

//	private class BambooValidator extends Validator {
//
//		private final TaskRepository taskRepository;
//
//		public BambooValidator(TaskRepository taskRepository) {
//			this.taskRepository = taskRepository;
//		}
//
//		@Override
//		public void run(IProgressMonitor monitor) throws CoreException {
//			BambooClientManager clientManager = BambooCorePlugin.getRepositoryConnector().getClientManager();
//			BambooClient client = null;
//			try {
//				client = clientManager.createTempClient(taskRepository, new BambooClientData());
//				client.validate(monitor, taskRepository);
//			} finally {
//				if (client != null) {
//					clientManager.deleteTempClient(client);
//				}
//			}
//		}
//	}

	private static final int FISHEYE_REPO_VIEWER_HEIGHT = 100;

	private CheckboxTreeViewer planViewer;

	private boolean validSettings;

	private boolean initialized;

	public FishEyeRepositorySettingsPage(TaskRepository taskRepository) {
		super("FishEye Repository Settings", "Enter FishEye server information", taskRepository);
		setNeedsHttpAuth(true);
		setNeedsEncoding(false);
		setNeedsAnonymousLogin(false);
		setNeedsAdvanced(false);
	}

	@Override
	public void applyTo(final TaskRepository repository) {
		this.repository = applyToValidate(repository);
	}

	/**
	 * Helper method for distinguishing between hitting Finish and Validate (because Validation leads to calling applyTo
	 * in the superclass)
	 */
	public TaskRepository applyToValidate(TaskRepository repository) {
		super.applyTo(repository);
		return repository;
	}

	@Override
	public TaskRepository createTaskRepository() {
		TaskRepository repository = new TaskRepository(connector.getConnectorKind(), getRepositoryUrl());
		return applyToValidate(repository);
	}

	@Override
	protected void createAdditionalControls(Composite parent) {
		addRepositoryTemplatesToServerUrlCombo();
	}

	@Override
	protected void createContributionControls(Composite parent) {

//		Label label = new Label(parent, SWT.LEFT);
//		label.setText("@todo: Under construction");
//		return labe

	}

	@Override
	public String getConnectorKind() {
		return FishEyeCorePlugin.CONNECTOR_KIND;
	}

	@Override
	protected Validator getValidator(TaskRepository repository) {
		return new FishEyeValidator(repository);
	}

	@Override
	protected boolean isValidUrl(String name) {
		if (name.startsWith(URL_PREFIX_HTTPS) || name.startsWith(URL_PREFIX_HTTP)) {
			try {
				new URL(name);
				return true;
			} catch (MalformedURLException e) {
				// ignore
			}
		}
		return false;
	}

	@Override
	protected void applyValidatorResult(Validator validator) {
		this.validSettings = validator != null && validator.getStatus() == Status.OK_STATUS;
		super.applyValidatorResult(validator);
	}

	@Override
	protected void validateSettings() {
		super.validateSettings();
//		if (validSettings) {
////			refreshBuildPlans();
//		}
	}

	@Override
	protected void repositoryTemplateSelected(RepositoryTemplate template) {
		repositoryLabelEditor.setStringValue(template.label);
		setUrl(template.repositoryUrl);
		getContainer().updateButtons();
	}

}
