/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.monitor.usage;

import com.atlassian.connector.eclipse.monitor.usage.AbstractStudyBackgroundPage;
import com.atlassian.connector.eclipse.monitor.usage.AbstractStudyQuestionnairePage;

public class FormParameters {

	private String customizingPlugin;

	private String description;

	private String title;

	private boolean useContactField;

	private AbstractStudyQuestionnairePage questionnairePage;

	private AbstractStudyBackgroundPage backgroundPage;

	private String formsConsent;

	private long transmitPromptPeriod;

	public void setCustomizingPlugin(String name) {
		this.customizingPlugin = name;
	}

	public String getCustomizingPlugin() {
		return customizingPlugin;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setUseContactField(boolean useContactField) {
		this.useContactField = useContactField;
	}

	public void setQuestionnairePage(AbstractStudyQuestionnairePage page) {
		this.questionnairePage = page;
	}

	public void setBackgroundPage(AbstractStudyBackgroundPage page) {
		this.backgroundPage = page;
	}

	public void setFormsConsent(String formsConsent) {
		this.formsConsent = formsConsent;
	}

	public void setTransmitPromptPeriod(long transmitPromptPeriod) {
		this.transmitPromptPeriod = transmitPromptPeriod;
	}

}
