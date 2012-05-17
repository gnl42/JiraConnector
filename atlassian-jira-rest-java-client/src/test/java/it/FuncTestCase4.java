/*
 * Copyright (C) 2012 Atlassian
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

import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.Form;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.HtmlPage;
import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.Parser;
import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.log.FuncTestLogger;
import com.atlassian.jira.rest.client.annotation.Annotations;
import com.atlassian.jira.rest.client.annotation.Restore;
import com.atlassian.jira.rest.client.annotation.RestoreOnce;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import net.sourceforge.jwebunit.WebTester;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.lang.reflect.Method;

public class FuncTestCase4 {

	private static boolean wasRestorePerformed;

	@Rule
	public TestName runningTestMethod = new TestName();

	// From FuncTestCase {
	public WebTester tester;
	public Navigation navigation;
	public Form form;
	public HtmlPage page;
	public Administration administration;
	public Assertions assertions;
	public TextAssertions text;
	public Parser parse;
	public FuncTestLogger log;
	public LocatorFactory locator;
	public JIRAEnvironmentData environmentData;
	// }

	private FakeFuncTestCase fakeFuncTestCase;

	@BeforeClass
	public static void beforeClass() {
		wasRestorePerformed = false;
	}

	@Before
	public void beforeMethod() {
		fakeFuncTestCase = new FakeFuncTestCase();
		fakeFuncTestCase.setMeUp();

		doRestore();
	}

	@After
	public void afterMethod() {
		fakeFuncTestCase.cleanUp();
	}

	protected RestoreConfig getRestoreConfig() {
		return getRestoreConfigFromAnnotations();
	}

	private void doRestore() {
		final RestoreConfig rc = getRestoreConfig();
		if (rc.restoreMode == RestoreConfig.RestoreMode.RESTORE_ALWAYS ||
				(rc.restoreMode == RestoreConfig.RestoreMode.RESTORE_ONCE && !wasRestorePerformed)) {

			administration.restoreData(rc.restoreFile);
			wasRestorePerformed = true;
		}
	}

	private RestoreConfig getRestoreConfigFromAnnotations() {
		// First check if there is @Restore on method
		final Class<? extends FuncTestCase4> aClass = this.getClass();
		if (!Strings.isNullOrEmpty(runningTestMethod.getMethodName())) {
			try {
				final Method m = aClass.getMethod(runningTestMethod.getMethodName());
				final Restore restore = m.getAnnotation(Restore.class);
				if (restore != null) {
					return RestoreConfig.restoreAlways(restore.value());
				}
			} catch (NoSuchMethodException e) {
				// method with params? not supported for now, ignore
			}
		}

		// Check for @RestoreOnce and @Restore on class (and super classes)
		final RestoreOnce restoreOnce = Annotations.getAnnotationIncludingParents(aClass, RestoreOnce.class);
		final Restore restore = Annotations.getAnnotationIncludingParents(aClass, Restore.class);
		if (restore != null) {
			if (restoreOnce != null) {
				throw new RuntimeException("Both @Restore and @RestoreOnce found on class. Only one should be present.");
			}

			return RestoreConfig.restoreAlways(restore.value());
		} else if (restoreOnce != null) {
			return RestoreConfig.restoreOnce(restoreOnce.value());
		} else {
			return RestoreConfig.doNotRestore();
		}
	}

	public static class RestoreConfig {

		public static RestoreConfig doNotRestore() {
			return new RestoreConfig(null, RestoreMode.DO_NOT_RESTORE);
		}

		public static RestoreConfig restoreOnce(final String restoreFile) {
			return new RestoreConfig(restoreFile, RestoreMode.RESTORE_ONCE);
		}

		public static RestoreConfig restoreAlways(final String restoreFile) {
			return new RestoreConfig(restoreFile, RestoreMode.RESTORE_ALWAYS);
		}

		public RestoreConfig(final String restoreFile, final RestoreMode restoreMode) {
			this.restoreFile = restoreFile;
			this.restoreMode = restoreMode;
		}

		public static enum RestoreMode {
			RESTORE_ALWAYS,
			RESTORE_ONCE,
			DO_NOT_RESTORE
		}

		private final RestoreMode restoreMode;
		private final String restoreFile;

		@Override
		public String toString() {
			return Objects.toStringHelper(this).
					add("restoreMode", restoreMode).
					add("restoreFile", restoreFile).
					toString();
		}
	}

	public class FakeFuncTestCase extends FuncTestCase {

		public void setMeUp() {
			setUp();

			final FuncTestCase4 ftc = FuncTestCase4.this;
			ftc.environmentData = funcTestHelperFactory.getEnvironmentData();
			ftc.tester = this.tester;
			ftc.navigation = this.navigation;
			ftc.form = this.form;
			ftc.page = this.page;
			ftc.administration = this.administration;
			ftc.assertions = this.assertions;
			ftc.text = this.text;
			ftc.parse = this.parse;
			ftc.log = this.log;
			ftc.locator = this.locator;
		}

		public void cleanUp() {
			try {
				final Method clearTestCaseVariablesMethod = FuncTestCase.class.getDeclaredMethod("clearTestCaseVariables");
				clearTestCaseVariablesMethod.setAccessible(true);
				clearTestCaseVariablesMethod.invoke(this);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
