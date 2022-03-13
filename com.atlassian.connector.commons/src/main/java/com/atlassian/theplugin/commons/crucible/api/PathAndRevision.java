package com.atlassian.theplugin.commons.crucible.api;

import java.util.ArrayList;
import java.util.List;

/**
 * User: kalamon
 * Date: 2009-11-20
 * Time: 13:24:41
 */
public class PathAndRevision {
    private String path;
    private List<String> revisions;

    public PathAndRevision() {
        revisions = new ArrayList<String>();
    }

    public PathAndRevision(String path, List<String> revisions) {
        this.path = path;
        this.revisions = revisions;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<String> revisions) {
        this.revisions = revisions;
    }
}
