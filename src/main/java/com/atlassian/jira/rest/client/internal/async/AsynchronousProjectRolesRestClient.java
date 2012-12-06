package com.atlassian.jira.rest.client.internal.async;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.ProjectRolesRestClient;
import com.atlassian.jira.rest.client.domain.BasicProjectRole;
import com.atlassian.jira.rest.client.domain.ProjectRole;
import com.atlassian.jira.rest.client.internal.json.BasicProjectRoleJsonParser;
import com.atlassian.jira.rest.client.internal.json.ProjectRoleJsonParser;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collection;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0
 */
public class AsynchronousProjectRolesRestClient extends AbstractAsynchronousRestClient implements ProjectRolesRestClient {

    private final ProjectRoleJsonParser projectRoleJsonParser;
    private final BasicProjectRoleJsonParser basicRoleJsonParser;

    public AsynchronousProjectRolesRestClient(final HttpClient client, final URI serverUri) {
        super(client);
        this.projectRoleJsonParser = new ProjectRoleJsonParser(serverUri);
        this.basicRoleJsonParser = new BasicProjectRoleJsonParser();
    }

    @Override
    public Promise<ProjectRole> getRole(URI uri) {
        return getAndParse(uri, projectRoleJsonParser);
    }

    @Override
    public Promise<ProjectRole> getRole(final URI projectUri, final Long roleId) {
        final URI roleUri = UriBuilder
                .fromUri(projectUri)
                .path("role")
                .path(String.valueOf(roleId))
                .build();
        return getAndParse(roleUri, projectRoleJsonParser);
    }

    @Override
    public Promise<Iterable<ProjectRole>> getRoles(final URI projectUri) {
        final URI rolesUris = UriBuilder
                .fromUri(projectUri)
                .path("role")
                .build();
        final Promise<Collection<BasicProjectRole>> basicProjectRoles = getAndParse(rolesUris, basicRoleJsonParser);

        return Promises.promise(Iterables.transform(basicProjectRoles.claim(), new Function<BasicProjectRole, ProjectRole>() {
            @Override
            public ProjectRole apply(final BasicProjectRole basicProjectRole) {
                return getRole(basicProjectRole.getSelf()).claim();
            }
        }));
    }
}
