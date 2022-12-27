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
package me.glindholm.theplugin.commons;

import javax.swing.*;

public class DefaultSwingUiTaskExecutor implements UiTaskExecutor {
	public void execute(final UiTask uiTask) {
		new Thread(new Runnable() {
			public void run() {
				try {
					uiTask.run();
				} catch (Exception e) {
					//noinspection CallToPrintStackTrace
					e.printStackTrace();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							uiTask.onError();
							JOptionPane.showMessageDialog(uiTask.getComponent(), "Error while " + uiTask.getLastAction(),
								"Error", JOptionPane.ERROR_MESSAGE);
						}
					});	
					return;
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							uiTask.onSuccess();
						} catch (Exception e) {
							//noinspection CallToPrintStackTrace
							e.printStackTrace();
							JOptionPane.showMessageDialog(uiTask.getComponent(), "Error while " + uiTask.getLastAction(),
								"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
			}
		}).start();
	}
}
