/*******************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Pawel Niewiadomski - fixes for bug 288347
 *     See git history
 *******************************************************************************/

package org.eclipse.mylyn.tests.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil;
import org.eclipse.mylyn.commons.sdk.util.MylynResourceMissingException;


/**
 * @author Steffen Pingel
 * @deprecated use {@link CommonTestUtil} instead
 */
@Deprecated
@SuppressWarnings({ "nls", "restriction" })
public class TestUtil {

	@Deprecated
	public static final String KEY_CREDENTIALS_FILE = "mylyn.credentials";

	public enum PrivilegeLevel {
		ANONYMOUS, GUEST, USER, ADMIN, READ_ONLY
	}

	@Deprecated
	public static class Credentials {

		@Deprecated
		public final String username;

		@Deprecated
		public final String password;

		@Deprecated
		public Credentials(final String username, final String password) {
			this.username = username;
			this.password = password;
		}

		@Deprecated
		@Override
		public String toString() {
			return getClass().getSimpleName() + " [username=" + username + ",password=" + password + "]";
		}

		@Deprecated
		public String getShortUserName() {
			if (username.contains("@")) {
				return username.substring(0, username.indexOf("@"));
			}
			return username;
		}

	}

	@Deprecated
	public static Credentials readCredentials() {
		return readCredentials(PrivilegeLevel.USER, null);
	}

	@Deprecated
	public static Credentials readCredentials(final PrivilegeLevel level) {
		return readCredentials(level, null);
	}

	@Deprecated
	public static Credentials readCredentials(final PrivilegeLevel level, String realm) {
		final var properties = new Properties();
		try {
			File file;
			final var filename = System.getProperty(KEY_CREDENTIALS_FILE);
			if (filename == null) {
				try {
					file = getFile(TestUtil.class, "credentials.properties");
					if (!file.exists()) {
						throw new MylynResourceMissingException("Can't find 'credentials.properties'");
					}
				} catch (final MylynResourceMissingException e) {
					file = new File(new File(System.getProperty("user.home"), ".mylyn"), "credentials.properties");
				}
			} else {
				file = new File(filename);
			}
			properties.load(new FileInputStream(file));
		} catch (final Exception e) {
			final var error = new MylynResourceMissingException(
					"must define credentials in $HOME/.mylyn/credentials.properties");
			error.initCause(e);
			throw error;
		}

		final var defaultPassword = properties.getProperty("pass");

		realm = realm != null ? realm + "." : "";
		return switch (level) {
		case ANONYMOUS -> createCredentials(properties, realm + "anon.", "", "");
		case GUEST -> createCredentials(properties, realm + "guest.", "guest@mylyn.eclipse.org", defaultPassword);
		case USER -> createCredentials(properties, realm, "tests@mylyn.eclipse.org", defaultPassword);
		case READ_ONLY -> createCredentials(properties, realm, "read-only@mylyn.eclipse.org", defaultPassword);
		case ADMIN -> createCredentials(properties, realm + "admin.", "admin@mylyn.eclipse.org", null);
		default -> throw new MylynResourceMissingException("invalid privilege level");
		};
	}

	private static Credentials createCredentials(final Properties properties, final String prefix, final String defaultUsername,
			final String defaultPassword) {
		var username = properties.getProperty(prefix + "user");
		var password = properties.getProperty(prefix + "pass");

		if (username == null) {
			username = defaultUsername;
		}

		if (password == null) {
			password = defaultPassword;
		}

		if (username == null || password == null) {
			throw new MylynResourceMissingException(
					"username or password not found in <plug-in dir>/credentials.properties, make sure file is valid");
		}

		return new Credentials(username, password);
	}

	@Deprecated
	public static File getFile(final Object source, final String filename) throws IOException {
		return CommonTestUtil.getFile(source, filename);
	}

	/**
	 * @deprecated use {org.eclipse.mylyn.commons.sdk.util.CommonTestUtil#runHeartbeatTestsOnly()} instead
	 */
	@Deprecated
	public static boolean runHeartbeatTestsOnly() {
		return !Boolean.getBoolean("org.eclipse.mylyn.tests.all");
	}

}
