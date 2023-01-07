package me.glindholm.theplugin.commons.bamboo;

import org.eclipse.jdt.annotation.NonNull;

/**
 * User: kalamon Date: Aug 18, 2009 Time: 3:31:13 PM
 */
public class BuildIssueInfo implements BuildIssue {

    private final String issueKey;
    private final String issueUrl;

    public BuildIssueInfo(@NonNull final String issueKey, @NonNull final String issueUrl) {
        this.issueKey = issueKey;
        this.issueUrl = issueUrl;
    }

    @Override
    public String getIssueKey() {
        return issueKey;
    }

    @Override
    public String getIssueUrl() {
        return issueUrl;
    }

    @Override
    public String getServerUrl() {
        final int keyPos = issueUrl.indexOf("/browse/" + issueKey);
        if (keyPos == -1) {
            return null;
        }
        return issueUrl.substring(0, keyPos);
    }
}
