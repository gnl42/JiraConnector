/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.core.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import me.glindholm.connector.eclipse.internal.jira.core.service.rest.JiraRestFields;

/**
 * @author Brock Janiczak
 * @author Jacek Jaroczynski
 */
public class JiraServerInfo implements Serializable {
    private static final long serialVersionUID = 6822423951913725245L;

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    private String baseUrl;

    private Instant buildDate;

    private String buildNumber;

    private String edition;

    private JiraServerVersion version;

    private String characterEncoding;

    private String webBaseUrl;

    private transient boolean insecureRedirect;

    private transient JiraStatistics statistics;

    public JiraServerInfo() {
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Instant getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(final Instant buildDate) {
        this.buildDate = buildDate;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(final String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(final String edition) {
        this.edition = edition;
    }

    public JiraServerVersion getVersion() {
        return version;
    }

    public void setVersion(final JiraServerVersion version) {
        this.version = version;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(final String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public String getWebBaseUrl() {
        return webBaseUrl;
    }

    public void setWebBaseUrl(final String webBaseUrl) {
        this.webBaseUrl = webBaseUrl;
    }

    public boolean isInsecureRedirect() {
        return insecureRedirect;
    }

    public void setInsecureRedirect(final boolean insecureRedirect) {
        this.insecureRedirect = insecureRedirect;
    }

    public synchronized JiraStatistics getStatistics() {
        if (statistics == null) {
            statistics = new JiraStatistics();
        }
        return statistics;
    }

    public String getAccountTag() {
        if (version.isGreaterThanOrEquals(JiraServerVersion.JIRA_10_0)) {
            return JiraRestFields.ACCOUNT_ID;
        } else {
            return JiraRestFields.NAME;
        }
    }

    /**
     * Search url changed with 1001.0 release
     * <a href="https://developer.atlassian.com/changelog/#CHANGE-2046">jql path change</a>
     * @return true if new jql path should be used
     */
	public boolean isNewJqlNeeded() {
		return version.isGreaterThanOrEquals(JiraServerVersion.JIRA_1001_0);
	}

    @Override
    public String toString() {
        final String dateStr = dateFormat.format(buildDate);
        return baseUrl + " - Jira " + version + "#" + buildNumber + " (" + dateStr + ")";
    }

}
