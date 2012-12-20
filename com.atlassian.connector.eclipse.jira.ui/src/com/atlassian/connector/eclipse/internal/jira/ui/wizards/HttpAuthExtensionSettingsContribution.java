package com.atlassian.connector.eclipse.internal.jira.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractTaskRepositoryPageContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author Jacek Jaroczynski
 */
public class HttpAuthExtensionSettingsContribution extends AbstractTaskRepositoryPageContribution {

	public HttpAuthExtensionSettingsContribution() {
		super(org.eclipse.mylyn.internal.tasks.ui.wizards.Messages.AbstractRepositorySettingsPage_Http_Authentication,
				"");
	}

	@Override
	public Control createControl(Composite parentControl) {
		Composite parent = new Composite(parentControl, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		parent.setLayout(layout);

		Label label = new Label(parent, SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, true).hint(500, SWT.DEFAULT).applyTo(label);
		label.setText(Messages.HttpAuthExtensionSettingsContribution_help_message);

		return parent;
	}

	@Override
	public boolean isPageComplete() {
		return true;
	}

	@Override
	public boolean canFlipToNextPage() {
		return true;
	}

	@Override
	public IStatus validate() {
		return null;
	}

	@Override
	public void applyTo(TaskRepository repository) {
	}

}
