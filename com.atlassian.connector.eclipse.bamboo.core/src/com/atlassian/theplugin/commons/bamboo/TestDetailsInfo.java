/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.bamboo;

public class TestDetailsInfo implements TestDetails {
	private String testClassName;
	private String testMethodName;
	private double testDuration;
	private TestResult testResult;
	private String testErrors;


	public String getTestClassName() {
		return testClassName;
	}

	public String getTestMethodName() {
		return testMethodName;
	}

	public double getTestDuration() {
		return testDuration;
	}

	public TestResult getTestResult() {
		return testResult;
	}

	public String getErrors() {
		return testErrors;
	}

	public void setTestClassName(String testClassName) {
		this.testClassName = testClassName;
	}

	public void setTestDuration(double testDuration) {
		this.testDuration = testDuration;
	}

	public void setTestErrors(String testErrors) {
		this.testErrors = testErrors;
	}

	public void setTestMethodName(String testMethodName) {
		this.testMethodName = testMethodName;
	}

	public void setTestResult(TestResult testResult) {
		this.testResult = testResult;
	}


}
