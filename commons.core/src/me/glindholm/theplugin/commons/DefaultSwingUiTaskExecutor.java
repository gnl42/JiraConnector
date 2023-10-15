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

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class DefaultSwingUiTaskExecutor implements UiTaskExecutor {
    @Override
    public void execute(final UiTask uiTask) {
        new Thread(() -> {
            try {
                uiTask.run();
            } catch (final Exception e) {
                // noinspection CallToPrintStackTrace
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    uiTask.onError();
                    JOptionPane.showMessageDialog(uiTask.getComponent(), "Error while " + uiTask.getLastAction(), "Error", JOptionPane.ERROR_MESSAGE);
                });
                return;
            }
            SwingUtilities.invokeLater(() -> {
                try {
                    uiTask.onSuccess();
                } catch (final Exception e) {
                    // noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(uiTask.getComponent(), "Error while " + uiTask.getLastAction(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }
}
