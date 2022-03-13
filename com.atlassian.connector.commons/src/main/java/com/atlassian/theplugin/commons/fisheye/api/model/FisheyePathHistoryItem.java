package com.atlassian.theplugin.commons.fisheye.api.model;

import org.jdom.Element;

/**
 * User: kalamon
 * Date: 2009-12-15
 * Time: 20:28:36
 *
 * More stuff should be added to this class on an "as needed" basis
 * - REST response gives us much more than we have here
 */
public class FisheyePathHistoryItem {
    private String path;
    private String rev;
    private String ancestor;
    private String author;

    public FisheyePathHistoryItem(Element element) {
        rev = element.getAttributeValue("rev");
        ancestor = element.getAttributeValue("ancestor");
        path = element.getAttributeValue("path");
        author = element.getAttributeValue("author");
    }

    public String getPath() {
        return path;
    }

    public String getRev() {
        return rev;
    }

    public String getAncestor() {
        return ancestor;
    }

    public String getAuthor() {
        return author;
    }
}
