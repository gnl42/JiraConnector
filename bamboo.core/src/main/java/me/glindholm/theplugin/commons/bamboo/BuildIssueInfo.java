package me.glindholm.theplugin.commons.bamboo;

import javax.annotation.Nonnull;

/**
 * User: kalamon
 * Date: Aug 18, 2009
 * Time: 3:31:13 PM
 */
public class BuildIssueInfo implements BuildIssue {

    private final String issueKey;
    private final String issueUrl;

    public BuildIssueInfo(@Nonnull String issueKey, @Nonnull String issueUrl) {
        this.issueKey = issueKey;
        this.issueUrl = issueUrl;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getIssueUrl() {
        return issueUrl;
    }

    public String getServerUrl() {
        int keyPos = issueUrl.indexOf("/browse/" + issueKey);
        if (keyPos == -1) {
            return null;
        }
        return issueUrl.substring(0, keyPos);
    }
}
