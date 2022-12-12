package me.glindholm.jira.rest.client.api.domain;

import java.net.URI;
import java.util.Objects;

public class RemotelinkObject {
    private final URI url;
    private final String title;
    private final String summary;
    private final RemotelinkIcon icon;
    private final RemotelinkStatus status;

    public RemotelinkObject(final URI url, final String title, final String summary, final RemotelinkIcon icon, final RemotelinkStatus status) {
        this.url = url;
        this.title = title;
        this.summary = summary;
        this.icon = icon;
        this.status = status;
    }

    public URI getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public RemotelinkIcon getIcon() {
        return icon;
    }

    public RemotelinkStatus getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(icon, status, summary, title, url);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RemotelinkObject)) {
            return false;
        }
        final RemotelinkObject other = (RemotelinkObject) obj;
        return Objects.equals(icon, other.icon) && Objects.equals(status, other.status) && Objects.equals(summary, other.summary)
                && Objects.equals(title, other.title) && Objects.equals(url, other.url);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("RemotelinkObject [url=").append(url).append(", title=").append(title).append(", summary=").append(summary).append(", icon=")
                .append(icon).append(", status=").append(status).append("]");
        return builder.toString();
    }
}
