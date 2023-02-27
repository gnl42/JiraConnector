package me.glindholm.connector.eclipse.internal.bamboo.core.service.rest;

import java.io.IOException;

import me.glindholm.bamboo.api.BuildApi;
import me.glindholm.bamboo.api.CoreApi;
import me.glindholm.bamboo.api.DefaultApi;
import me.glindholm.bamboo.api.DeploymentApi;
import me.glindholm.bamboo.api.PermissionsApi;
import me.glindholm.bamboo.api.ResourceApi;
import me.glindholm.bamboo.api.UserManagementApi;
import me.glindholm.bamboo.invoker.ApiClient;
import me.glindholm.connector.eclipse.internal.bamboo.core.rest.client.api.BambooRestClient;

public class BambooRestClientFactory {
    public static final BambooRestClient newClient(final ApiClient apiClient, final String url, final String userName) {
        final BuildApi buildApi = new BuildApi(apiClient);
        final CoreApi coreApi = new CoreApi(apiClient);
        final DefaultApi defaultApi = new DefaultApi(apiClient);
        final DeploymentApi deploymentApi = new DeploymentApi(apiClient);
        final PermissionsApi permissionsApi = new PermissionsApi(apiClient);
        final ResourceApi resourceApi = new ResourceApi(apiClient);
        final UserManagementApi userApi = new UserManagementApi(apiClient);

        return new BambooRestClient() {

            @Override
            public BuildApi getBuildApi() {
                return buildApi;
            }

            @Override
            public CoreApi getCoreApi() {
                return coreApi;
            }

            @Override
            public DefaultApi getDefaultApi() {
                return defaultApi;
            }

            @Override
            public DeploymentApi getDeploymentApi() {
                return deploymentApi;
            }

            @Override
            public PermissionsApi getPermissionsApi() {
                return permissionsApi;
            }

            @Override
            public ResourceApi getResourceApi() {
                return resourceApi;
            }

            @Override
            public UserManagementApi getUserApi() {
                return userApi;
            }

            @Override
            public ApiClient getApiClient() {
                return apiClient;
            }

            @Override
            public void close() throws IOException {
            }

            @Override
            public String getServerUrl() {
                return url;
            }

            @Override
            public String getUsername() {
                return userName;
            }
        };
    }

}
