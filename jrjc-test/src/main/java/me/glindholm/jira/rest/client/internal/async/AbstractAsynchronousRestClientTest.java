/*
 * Copyright (C) 2010-2012 Atlassian
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

package me.glindholm.jira.rest.client.internal.async;

import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterators;

import me.glindholm.jira.rest.client.api.domain.util.ErrorCollection;
import me.glindholm.jira.rest.client.internal.json.ResourceUtil;

public class AbstractAsynchronousRestClientTest {

    private static final int BAD_REQUEST = 400;

    @Test
    public void testExtractErrors() throws JSONException {
        final String str = ResourceUtil.getStringFromResource("/json/error/valid.json");
        final List<ErrorCollection> errors = AbstractAsynchronousRestClient.extractErrors(BAD_REQUEST, str);
        final ErrorCollection errorList = Iterators.getOnlyElement(errors.iterator());
        Assert.assertThat(errorList.getErrors().values(), IsIterableContainingInAnyOrder.containsInAnyOrder("abcfsd"));
    }

    @Test
    public void testExtractErrors2() throws JSONException {
        final String str = ResourceUtil.getStringFromResource("/json/error/valid2.json");
        final List<ErrorCollection> errors = AbstractAsynchronousRestClient.extractErrors(BAD_REQUEST, str);
        final ErrorCollection errorList = Iterators.getOnlyElement(errors.iterator());
        Assert.assertThat(errorList.getErrorMessages(), IsIterableContainingInAnyOrder.containsInAnyOrder("a", "b", "xxx"));
    }

    @Test
    public void testExtractErrors3() throws JSONException {
        final String str = ResourceUtil.getStringFromResource("/json/error/valid3.json");
        final List<ErrorCollection> errors = AbstractAsynchronousRestClient.extractErrors(BAD_REQUEST, str);
        final ErrorCollection errorList = Iterators.getOnlyElement(errors.iterator());
        Assert.assertThat(errorList.getErrors().values(), IsIterableContainingInAnyOrder.containsInAnyOrder("aa", "bb"));
    }

    @Test
    public void testExtractErrors4() throws JSONException {
        final String str = ResourceUtil.getStringFromResource("/json/error/valid4.json");
        final List<ErrorCollection> errors = AbstractAsynchronousRestClient.extractErrors(BAD_REQUEST, str);
        final ErrorCollection errorList = Iterators.getOnlyElement(errors.iterator());

        Assert.assertThat(errorList.getErrorMessages(), IsIterableContainingInAnyOrder.containsInAnyOrder("a", "b"));
        Assert.assertEquals(errorList.getErrors().get("a"), "y");
        Assert.assertEquals(errorList.getErrors().get("c"), "z");
    }

}
