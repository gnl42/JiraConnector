/*
 * Copyright (C) 2010 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.restjavaclient.IntegrationTestUtil;
import com.atlassian.jira.restjavaclient.domain.Authentication;
import com.atlassian.jira.restjavaclient.json.AuthenticationJsonParser;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import junit.framework.TestCase;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import javax.ws.rs.core.Cookie;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class MySimpleTest  {

    public void test1() {
        // I just want this to work
//        System.out.println(administration.getJiraHomeDirectory());


    }

    @Test
    public void testEmpty() throws JSONException, URISyntaxException {
        // for the sake of mvn integration-test
//        final ApacheHttpClient httpClient = ApacheHttpClient.create();
//        final URI uri = new URI("http://localhost:8090/jira/rest/auth/latest");
//        final WebResource sessionResource = httpClient.resource(IntegrationTestUtil.concat(uri, "/session"));
//        JSONObject json = new JSONObject();
//        json.put("username", "admin");
//        json.put("password", "admin");
//        final JSONObject resJs = sessionResource.post(JSONObject.class, json);
//        AuthenticationJsonParser parser = new AuthenticationJsonParser();
//        final Authentication authentication = parser.parse(resJs);
//        System.out.println(authentication);
//        final WebResource.Builder builder = sessionResource.cookie(new Cookie(authentication.getSession().getName(), authentication.getSession().getValue()));
//        final JSONObject jsonObject = builder.get(JSONObject.class);
//        System.out.println(jsonObject);
    }

}