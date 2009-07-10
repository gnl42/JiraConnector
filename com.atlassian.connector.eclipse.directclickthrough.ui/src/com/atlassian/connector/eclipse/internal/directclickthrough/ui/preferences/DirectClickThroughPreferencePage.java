package com.atlassian.connector.eclipse.internal.directclickthrough.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.atlassian.connector.eclipse.internal.directclickthrough.ui.DirectClickThroughUiPlugin;
import com.atlassian.connector.eclipse.internal.directclickthrough.ui.IDirectClickThroughPreferenceConstants;

public class DirectClickThroughPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final int PORT_RANGE_MAX = 0xFFFF-1;
	private static final int PORT_RANGE_MIN = 1024;
	private Button enableClickThrough;
	private Text port;

	public DirectClickThroughPreferencePage() {
		super("Direct Click Through Preferences");
		setDescription("Direct Click Through allows you to easily open selected items in your IDE.\n"
				+ "You can browse your FishEye, Crucible, Bamboo or JIRA and navigate to your IDE\n"
				+ "with one click and open selected item automatically. This feature opens a locally\n"
				+ "available TCP/IP port, only localhost can access it.");
	}
	
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return DirectClickThroughUiPlugin.getDefault().getPreferenceStore();
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);
		createConfigurationGroup(container);
		
		updateConfigurationGroupEnablements();
		return container;
	}

	private void createConfigurationGroup(Composite container) {
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText("Configure Direct Click Through");
		GridLayout gridLayout = new GridLayout(1, false);
		group.setLayout(gridLayout);

		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		enableClickThrough = new Button(group, SWT.CHECK);
		enableClickThrough.setText("Enable Direct Click Through");
		enableClickThrough.setSelection(getPreferenceStore().getBoolean(
				IDirectClickThroughPreferenceConstants.ENABLED));
		enableClickThrough.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateConfigurationGroupEnablements();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Composite portComposite = new Composite(group, SWT.NULL);
		gridLayout = new GridLayout(4, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		portComposite.setLayout(gridLayout);

		Label label = new Label(portComposite, SWT.NONE);
		label.setText("Let Eclipse listen on following TCP/IP port:");

		port = new Text(portComposite, SWT.BORDER | SWT.RIGHT);
		GridData gridData = new GridData();
		gridData.widthHint = 50;
		port.setLayoutData(gridData);
		port.setText(getPreferenceStore().getString(IDirectClickThroughPreferenceConstants.PORT_NUMBER));
		port.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateConfigurationGroupEnablements();
			}
		});
	}

	public void init(IWorkbench workbench) {
	}

	private void updateConfigurationGroupEnablements() {
		String errorMessage = null;

		if (enableClickThrough.getSelection()) {
			try {
				int number = Integer.parseInt(port.getText());
				if (number <= PORT_RANGE_MIN || number >= PORT_RANGE_MAX) {
					errorMessage = NLS.bind("Post number must be between {0} and {1}",
							PORT_RANGE_MIN, PORT_RANGE_MAX);
				}
			} catch (NumberFormatException e) {
				errorMessage = "Port must be a number";
			}
		}

		setErrorMessage(errorMessage);
		setValid(errorMessage == null);

		port.setEnabled(enableClickThrough.getSelection());
	}
}
