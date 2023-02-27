package me.glindholm.connector.eclipse.internal.bamboo.core.rest.client.api;

import java.io.Closeable;
import java.io.IOException;

import me.glindholm.bamboo.api.BuildApi;
import me.glindholm.bamboo.api.CoreApi;
import me.glindholm.bamboo.api.DefaultApi;
import me.glindholm.bamboo.api.DeploymentApi;
import me.glindholm.bamboo.api.PermissionsApi;
import me.glindholm.bamboo.api.ResourceApi;
import me.glindholm.bamboo.api.UserManagementApi;
import me.glindholm.bamboo.invoker.ApiClient;

public interface BambooRestClient extends Closeable {
    String getServerUrl();

    String getUsername();

    BuildApi getBuildApi();

    CoreApi getCoreApi();

    DefaultApi getDefaultApi();

    DeploymentApi getDeploymentApi();

    PermissionsApi getPermissionsApi();

    ResourceApi getResourceApi();

    UserManagementApi getUserApi();

    ApiClient getApiClient();

    @Override
    void close() throws IOException;

}
