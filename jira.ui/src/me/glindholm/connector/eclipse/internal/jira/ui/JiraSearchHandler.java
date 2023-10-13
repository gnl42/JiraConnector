/*******************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.ui;

import java.util.StringTokenizer;

import org.eclipse.mylyn.internal.tasks.core.AbstractSearchHandler;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;

/**
 * Stack Trace duplicate detector
 *
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class JiraSearchHandler extends AbstractSearchHandler {

    private static final int MAX_LENGTH = 950;

    @Override
    public String getConnectorKind() {
        return JiraCorePlugin.CONNECTOR_KIND;
    }

    @Override
    public boolean queryForText(final TaskRepository taskRepository, final IRepositoryQuery query, final TaskData taskData,
            final String searchString) {
        final String preparedSearchString = prepareJqlSearchString(searchString);
        final FilterDefinition filter = new FilterDefinition();
        filter.setContentFilter(new ContentFilter(preparedSearchString, false, true, false, true));
        JiraUtil.setQuery(taskRepository, query, filter);
        return true;
    }

    public static String prepareSearchString(final String searchString) {
        final StringBuilder sb = new StringBuilder(MAX_LENGTH);
        // on JIRA 4+ you cannot search for :
        // on older JIRAs your search fails if the query is too long
        // because I don't have a progress monitor here, I can not easily fetch JIRA version (it may be not yet cached)
        // so I assume the worst and apply the solution (not perfect but good enough) which should
        // work with all supported JIRA versions
        final StringTokenizer t = new StringTokenizer(searchString, " :\n\t()$"); //$NON-NLS-1$
        while (t.hasMoreTokens() && sb.length() < MAX_LENGTH - 20) {
            if (sb.length() > 0) {
                sb.append(" AND "); //$NON-NLS-1$
            }
            final int remaining = MAX_LENGTH - sb.length();
            final String token = t.nextToken();
            if (token.length() > remaining) {
                sb.append(token.substring(0, remaining));
                sb.append("*"); //$NON-NLS-1$
            } else {
                sb.append(token);
            }
        }
        return sb.toString();
    }

    public static String prepareJqlSearchString(final String input) {

        final StringBuilder sb = new StringBuilder(MAX_LENGTH);

        final StringTokenizer t = new StringTokenizer(input, "\n\t"); //$NON-NLS-1$

        while (t.hasMoreTokens() && sb.length() < MAX_LENGTH - 50) {
            final int remaining = MAX_LENGTH - sb.length();

            if (sb.length() > 0) {
                sb.append("*"); //$NON-NLS-1$
            }

            final String token = t.nextToken();

            if (token.length() > remaining) {
                sb.append(token.substring(0, remaining));
                sb.append("*"); //$NON-NLS-1$
            } else {
                sb.append(token);
            }
        }

        sb.insert(0, "\\\""); //$NON-NLS-1$
        sb.append("\\\""); //$NON-NLS-1$

        final String output = sb.toString().replaceAll("([():])", "\\\\\\\\$1"); //$NON-NLS-1$//$NON-NLS-2$

        return output;
    }
}
