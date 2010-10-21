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

package com.atlassian.jira.rest.client.internal.jersey;

import com.atlassian.jira.rest.client.IterableMatcher;
import com.atlassian.jira.rest.client.internal.json.ResourceUtil;
import org.codehaus.jettison.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class AbstractJerseyRestClientTest {
	@Test
	public void testExtractErrors() throws JSONException {
		final String str = ResourceUtil.getStringFromResource("/json/error/valid.json");
		final Collection<String> stringCollection = AbstractJerseyRestClient.extractErrors(str);
		Assert.assertThat(stringCollection, IterableMatcher.hasOnlyElements("abcfsd"));
	}

	@Test
	public void testExtractErrors2() throws JSONException {
		final String str = ResourceUtil.getStringFromResource("/json/error/valid2.json");
		final Collection<String> stringCollection = AbstractJerseyRestClient.extractErrors(str);
		Assert.assertThat(stringCollection, IterableMatcher.hasOnlyElements("a", "b", "xxx"));
	}

	@Test
	public void testExtractErrors3() throws JSONException {
		final String str = ResourceUtil.getStringFromResource("/json/error/valid3.json");
		final Collection<String> stringCollection = AbstractJerseyRestClient.extractErrors(str);
		Assert.assertThat(stringCollection, IterableMatcher.hasOnlyElements("aa", "bb"));
	}

	@Test
	public void testExtractErrors4() throws JSONException {
		final String str = ResourceUtil.getStringFromResource("/json/error/valid4.json");
		final Collection<String> stringCollection = AbstractJerseyRestClient.extractErrors(str);
		Assert.assertThat(stringCollection, IterableMatcher.hasOnlyElements("a", "b", "y", "z"));
	}

}
