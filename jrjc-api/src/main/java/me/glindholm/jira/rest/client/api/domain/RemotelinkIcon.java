package me.glindholm.jira.rest.client.api.domain;

import java.net.URI;
import java.util.Objects;

public class RemotelinkIcon {
    public static final String S16_16 = "16x16";

    private final URI url16_16;
    private final String title;
    private final URI link;

    public RemotelinkIcon(final URI url, final String title, final URI link) {
        url16_16 = url;
        this.title = title;
        this.link = link;
    }

    public static String getS1616() {
        return S16_16;
    }

    public URI getUrl16_16() {
        return url16_16;
    }

    public String getTitle() {
        return title;
    }

    public URI getLink() {
        return link;
    }

    @Override
    public int hashCode() {
        return Objects.hash(link, title, url16_16);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final RemotelinkIcon other)) {
            return false;
        }
        return Objects.equals(link, other.link) && Objects.equals(title, other.title) && Objects.equals(url16_16, other.url16_16);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("RemotelinkIcon [url16_16=").append(url16_16).append(", title=").append(title).append(", link=").append(link).append("]");
        return builder.toString();
    }
}
