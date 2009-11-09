package com.atlassian.connector.eclipse.bamboo.ui.preferences;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooConstants;
import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.ui.preferences.EclipsePreferencesAdapter;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("restriction")
public class BambooPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final int MIN_REFRESH_RATE = 1;

	private static final int MAX_REFRESH_RATE = 10000;

	public BambooPreferencePage() {
		super(GRID);
		setDescription("Atlassian Bamboo Settings");
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return new EclipsePreferencesAdapter(new InstanceScope(), BambooCorePlugin.PLUGIN_ID);
	}

	public void createFieldEditors() {
		final IntegerFieldEditor refreshRate = new IntegerFieldEditor(BambooConstants.PREFERENCE_REFRESH_INTERVAL,
				"Auto &Refresh Interval Rate (minutes)", getFieldEditorParent(), 4);
		refreshRate.setValidRange(MIN_REFRESH_RATE, MAX_REFRESH_RATE);
		addField(refreshRate);
		addField(new BooleanFieldEditor(BambooConstants.PREFERENCE_AUTO_REFRESH, "Refresh &Automatically",
				getFieldEditorParent()));
		final LocalBooleanFieldEditor playSoundCheckboxEditor = new LocalBooleanFieldEditor(
				BambooConstants.PREFERENCE_PLAY_SOUND, "Play sound when build failed", getFieldEditorParent());
		addField(playSoundCheckboxEditor);
		final FileFieldEditor soundFileEditor = new FileFieldEditor(BambooConstants.PREFERENCE_BUILD_SOUND,
				"Select sound to play: ", getFieldEditorParent());
		soundFileEditor.setEnabled(false, getFieldEditorParent());
		String[] ext = { "*.wav" };
		soundFileEditor.setFileExtensions(ext);
		addField(soundFileEditor);

		playSoundCheckboxEditor.addChangeListener(new LocalSelectionListener() {
			public void selectionChanged() {
				if (!playSoundCheckboxEditor.getBooleanValue()) {
					soundFileEditor.setEnabled(false, getFieldEditorParent());
				} else {
					soundFileEditor.setEnabled(true, getFieldEditorParent());
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	public interface LocalSelectionListener {
		void selectionChanged();
	}

	private static final class LocalBooleanFieldEditor extends BooleanFieldEditor implements SelectionListener {

		private final Composite parent;

		private final Collection<LocalSelectionListener> listeners = new ArrayList<LocalSelectionListener>();

		public LocalBooleanFieldEditor(String string, String string2, Composite fieldEditorParent) {
			super(string, string2, fieldEditorParent);
			parent = fieldEditorParent;
			super.getChangeControl(parent).addSelectionListener(this);
		}

		public void addChangeListener(LocalSelectionListener listener) {
			listeners.add(listener);
		}

		@Override
		protected void doLoad() {
			super.doLoad();
			notifyListeners();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			// ignore
		}

		public void widgetSelected(SelectionEvent e) {
			notifyListeners();
		}

		private void notifyListeners() {
			for (LocalSelectionListener listener : listeners) {
				listener.selectionChanged();
			}
		}
	}

}