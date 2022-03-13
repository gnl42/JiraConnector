package com.atlassian.theplugin.crucible.api.rest;

import org.easymock.IArgumentMatcher;

/**
 * User: pmaruszak
 */
public class CharArrayEquals implements IArgumentMatcher {
	char[] expected;

	public CharArrayEquals(final char[] expected) {
		this.expected = expected;
	}

	public boolean matches(final Object argument) {
		if (!(argument instanceof char[])) {
			return false;
		}
		char[] givenValue = (char[]) argument;
		return expected.length == givenValue.length;
	}

	public void appendTo(final StringBuffer buffer) {
		buffer.append("eqException(");
		buffer.append(expected.getClass().getName());
		buffer.append(" with value \"");
		buffer.append(String.valueOf(expected));
		buffer.append("\")");

	}


}
