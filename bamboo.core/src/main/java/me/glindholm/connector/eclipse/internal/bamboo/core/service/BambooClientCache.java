package me.glindholm.connector.eclipse.internal.bamboo.core.service;

import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClient;
import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClientData;

public class BambooClientCache {
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BambooClientCache [data=").append(data).append("]");
        return builder.toString();
    }

    private volatile BambooClientData data;

    private final BambooClient bambooClient;

    public BambooClientCache(final BambooClient bambooClient) {
        this.bambooClient = bambooClient;
        data = new BambooClientData();
    }

    public BambooClientData getData() {
        return data;
    }

    public void setData(final BambooClientData data) {
        this.data = data;
    }

}
