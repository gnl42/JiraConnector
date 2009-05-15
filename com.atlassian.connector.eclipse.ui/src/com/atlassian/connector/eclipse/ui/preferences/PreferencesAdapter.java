/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Atlassian - getting rid of JDT dependency and incorporated into Eclipse Connector + added save support
 *     
 *******************************************************************************/
package com.atlassian.connector.eclipse.ui.preferences;

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.io.IOException;

/**
 * Adapts {@link org.eclipse.core.runtime.Preferences} to {@link org.eclipse.jface.preference.IPreferenceStore}.
 * 
 * This is almost a clone of org.eclipse.jdt.internal.ui.text.PreferencesAdapter, which unfortunately depend on JDT
 */
public class PreferencesAdapter implements IPreferenceStore, IPersistentPreferenceStore {

	public interface SaveHandler {
		void doSave();
	}

	/**
	 * Property change listener. Listens for events of type
	 * {@link org.eclipse.core.runtime.Preferences.PropertyChangeEvent} and fires a
	 * {@link org.eclipse.jface.util.PropertyChangeEvent} on the adapter with arguments from the received event.
	 */
	private class PropertyChangeListener implements Preferences.IPropertyChangeListener {

		public void propertyChange(Preferences.PropertyChangeEvent event) {
			firePropertyChangeEvent(event.getProperty(), event.getOldValue(), event.getNewValue());
		}
	}

	/** Listeners on the adapter */
	private final ListenerList fListeners = new ListenerList(ListenerList.IDENTITY);

	/** Listener on the adapted Preferences */
	private final PropertyChangeListener fListener = new PropertyChangeListener();

	/** Adapted Preferences */
	private final Preferences fPreferences;

	/** True iff no events should be forwarded */
	private boolean fSilent;

	private final SaveHandler saveHandler;

	/**
	 * Initialize with the given Preferences.
	 * 
	 * @param preferences
	 *            The preferences to wrap.
	 */
	public PreferencesAdapter(Preferences preferences, SaveHandler saveHandler) {
		if (saveHandler == null) {
			throw new IllegalArgumentException("SaveHandler cannot be null");
		}
		fPreferences = preferences;
		this.saveHandler = saveHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (fListeners.size() == 0) {
			fPreferences.addPropertyChangeListener(fListener);
		}
		fListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);
		if (fListeners.size() == 0) {
			fPreferences.removePropertyChangeListener(fListener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(String name) {
		return fPreferences.contains(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		if (!fSilent) {
			final PropertyChangeEvent event = new PropertyChangeEvent(this, name, oldValue, newValue);
			Object[] listeners = fListeners.getListeners();
			for (Object listener2 : listeners) {
				final IPropertyChangeListener listener = (IPropertyChangeListener) listener2;
				Runnable runnable = new Runnable() {
					public void run() {
						listener.propertyChange(event);
					}
				};

				if (Display.getCurrent() != null) {
					runnable.run();
				} else {
					// Post runnable into UI thread
					Shell shell = AtlassianUiPlugin.getActiveWorkbenchShell();
					Display display;
					if (shell != null) {
						display = shell.getDisplay();
					} else {
						display = Display.getDefault();
					}
					display.asyncExec(runnable);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBoolean(String name) {
		return fPreferences.getBoolean(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getDefaultBoolean(String name) {
		return fPreferences.getDefaultBoolean(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public double getDefaultDouble(String name) {
		return fPreferences.getDefaultDouble(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public float getDefaultFloat(String name) {
		return fPreferences.getDefaultFloat(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getDefaultInt(String name) {
		return fPreferences.getDefaultInt(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getDefaultLong(String name) {
		return fPreferences.getDefaultLong(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDefaultString(String name) {
		return fPreferences.getDefaultString(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public double getDouble(String name) {
		return fPreferences.getDouble(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public float getFloat(String name) {
		return fPreferences.getFloat(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInt(String name) {
		return fPreferences.getInt(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getLong(String name) {
		return fPreferences.getLong(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getString(String name) {
		return fPreferences.getString(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDefault(String name) {
		return fPreferences.isDefault(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean needsSaving() {
		return fPreferences.needsSaving();
	}

	/**
	 * {@inheritDoc}
	 */
	public void putValue(String name, String value) {
		try {
			fSilent = true;
			fPreferences.setValue(name, value);
		} finally {
			fSilent = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, double value) {
		fPreferences.setDefault(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, float value) {
		fPreferences.setDefault(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, int value) {
		fPreferences.setDefault(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, long value) {
		fPreferences.setDefault(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, String defaultObject) {
		fPreferences.setDefault(name, defaultObject);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, boolean value) {
		fPreferences.setDefault(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setToDefault(String name) {
		fPreferences.setToDefault(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, double value) {
		fPreferences.setValue(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, float value) {
		fPreferences.setValue(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, int value) {
		fPreferences.setValue(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, long value) {
		fPreferences.setValue(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, String value) {
		fPreferences.setValue(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, boolean value) {
		fPreferences.setValue(name, value);
	}

	public void save() throws IOException {
		saveHandler.doSave();

	}
}
