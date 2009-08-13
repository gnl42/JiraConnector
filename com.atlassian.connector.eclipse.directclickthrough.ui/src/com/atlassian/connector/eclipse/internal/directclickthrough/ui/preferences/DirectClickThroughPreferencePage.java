package com.atlassian.connector.eclipse.internal.directclickthrough.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.atlassian.connector.eclipse.internal.directclickthrough.ui.DirectClickThroughUiPlugin;
import com.atlassian.connector.eclipse.internal.directclickthrough.ui.IDirectClickThroughPreferenceConstants;

public class DirectClickThroughPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private static final int PORT_RANGE_MAX = 0xFFFF-1;
	private static final int PORT_RANGE_MIN = 1024;

	public DirectClickThroughPreferencePage() {
		super(GRID);
		setTitle("Direct Click Through Preferences");
		setDescription("Direct Click Through allows you to easily open selected items in your IDE. "
				+ "You can browse your FishEye, Crucible, Bamboo or JIRA and navigate to your IDE "
				+ "with one click and open selected item automatically. This feature opens a locally "
				+ "available TCP/IP port, only localhost can access it.");
	}
	
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return DirectClickThroughUiPlugin.getDefault().getPreferenceStore();
	}
	
	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(IDirectClickThroughPreferenceConstants.ENABLED, "Enabled Direct Click Through",
				getFieldEditorParent()));
		
		final IntegerFieldEditor port = new IntegerFieldEditor(IDirectClickThroughPreferenceConstants.PORT_NUMBER,
				"Let Eclipse listen on following TCP/IP port", getFieldEditorParent(), 5);
		
		port.setValidRange(PORT_RANGE_MIN, PORT_RANGE_MAX);
		addField(port);
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

}
