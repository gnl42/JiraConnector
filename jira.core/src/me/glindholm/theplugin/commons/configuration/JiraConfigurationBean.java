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

package me.glindholm.theplugin.commons.configuration;

public class JiraConfigurationBean {

    private static final int JIRA_DEFAULT_ISSUE_PAGE_SIZE = 25;

    private int pageSize = JIRA_DEFAULT_ISSUE_PAGE_SIZE;
    private boolean synchronizeWithIntelliJTasks = false;
    private boolean showIssueTooltips = true;

    private static final int HASHCODE_MAGIC = 31;

    public JiraConfigurationBean() {
    }

    public JiraConfigurationBean(final JiraConfigurationBean cfg) {
        pageSize = cfg.getPageSize();
        synchronizeWithIntelliJTasks = cfg.synchronizeWithIntelliJTasks;
        showIssueTooltips = cfg.showIssueTooltips;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isShowIssueTooltips() {
        return showIssueTooltips;
    }

    public void setShowIssueTooltips(final boolean showIssueTooltips) {
        this.showIssueTooltips = showIssueTooltips;
    }

    public boolean isSynchronizeWithIntelliJTasks() {
        return synchronizeWithIntelliJTasks;
    }

    public void setSynchronizeWithIntelliJTasks(final boolean synchronizeWithIntelliJTasks) {
        this.synchronizeWithIntelliJTasks = synchronizeWithIntelliJTasks;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final JiraConfigurationBean that = (JiraConfigurationBean) o;

        if (pageSize != that.pageSize || synchronizeWithIntelliJTasks != that.synchronizeWithIntelliJTasks || showIssueTooltips != that.showIssueTooltips) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = pageSize;
        result = 31 * result + (synchronizeWithIntelliJTasks ? 1 : 0);
        result = 31 * result + (showIssueTooltips ? 1 : 0);
        return result;
    }
}