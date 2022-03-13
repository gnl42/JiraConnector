/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.commons;

import java.awt.*;

public abstract class UiTaskAdapter implements UiTask {

	private final String actionName;
	private final Component component;

	public UiTaskAdapter(final String actionName, final Component component) {
		this.actionName = actionName;
		this.component = component;
	}

	public void onSuccess() {
	}

	public void onError() {
	}

	public String getLastAction() {
		return actionName;
	}

	public Component getComponent() {
		return component;
	}
}
