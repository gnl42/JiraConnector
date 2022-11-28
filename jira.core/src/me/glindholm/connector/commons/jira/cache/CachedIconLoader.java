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

package me.glindholm.connector.commons.jira.cache;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public final class CachedIconLoader {
    private static Map<String, Icon> icons = new HashMap<>();
    private static Map<String, Icon> disabledIcons = new HashMap<>();

    private CachedIconLoader() {
    }

    public static Icon getDisabledIcon(final String urlString) {
        return disabledIcons.get(urlString);
    }

    private static void addDisabledIcon(final String urlString, final Icon icon) {
        disabledIcons.put(urlString, icon);
    }

    private static Icon generateDisabledIcon(final Icon icon) {
        return new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) icon).getImage()));
    }

    private static void maybeGenerateDisabledIcon(final String urlString, final Icon icon) {
        if (disabledIcons.containsKey(urlString) || icon == null) {
            return;
        }
        addDisabledIcon(urlString, generateDisabledIcon(icon));
    }

    public static Icon getIcon(final URL url) {
        if (url != null) {
            final String key = url.toString();
            if (!icons.containsKey(key)) {
                final Icon i = new ImageIcon(url);
                icons.put(key, i);
                maybeGenerateDisabledIcon(key, i);
            }
            return icons.get(key);
        } else {
            return null;
        }
    }

    public static Icon getIcon(final String urlString) {
        if (urlString != null) {
            if (!icons.containsKey(urlString)) {
                loadIcon(urlString);
            }
            return icons.get(urlString);
        } else {
            return null;
        }
    }

    public static void loadIcon(final String urlString) {
        if (urlString != null && !icons.containsKey(urlString)) {
            try {

                final URL url = new URL(urlString);
                final Icon i = new ImageIcon(url);
                icons.put(urlString, i);
                maybeGenerateDisabledIcon(urlString, i);
            } catch (final MalformedURLException e) {
                //logger.warn("Cannot load icon: [" + urlString + "]", e);
            }
        }
    }

}
