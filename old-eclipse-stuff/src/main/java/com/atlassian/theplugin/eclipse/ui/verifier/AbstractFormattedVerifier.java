/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.verifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.widgets.Control;

/**
 * Abstract verifier implementation, that provides formatted message support
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractFormattedVerifier extends AbstractVerifier {
	public static final String FIELD_NAME = "$FIELD_NAME$";
	protected Map<String, String> placeHolders;

	public AbstractFormattedVerifier(String fieldName) {
		super();
		this.placeHolders = new HashMap<String, String>();
		this.setPlaceHolder(AbstractFormattedVerifier.FIELD_NAME, fieldName);
	}

	public void setPlaceHolder(String placeHolder, String value) {
		this.placeHolders.put(placeHolder, value);
	}

	public String getPlaceHolder(String placeHolder) {
		return (String) this.placeHolders.get(placeHolder);
	}

	protected String getErrorMessage(Control input) {
		return this.getFormattedMessage(this.getErrorMessageImpl(input));
	}

	protected String getWarningMessage(Control input) {
		return this.getFormattedMessage(this.getWarningMessageImpl(input));
	}

	protected abstract String getErrorMessageImpl(Control input);

	protected abstract String getWarningMessageImpl(Control input);

	protected String getFormattedMessage(String message) {
		if (message != null) {
			for (Iterator<Map.Entry<String, String>> it = this.placeHolders
					.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>) it
						.next();
				String key = (String) entry.getKey();
				String value = entry.getValue() == null ? "" : entry.getValue()
						.toString();
				int idx = message.indexOf(key);
				if (idx != -1) {
					message = message.substring(0, idx) + value
							+ message.substring(idx + key.length());
				}
			}
		}
		return message;
	}

}
