/*******************************************************************************
 * Copyright (c) 2011, 2024 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *     Tasktop Technologies - initial API and implementation
 *     ArSysOp - ongoing support
 *     See git history
 *******************************************************************************/

package org.eclipse.mylyn.commons.sdk.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.mylyn.commons.core.net.NetUtil;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.commons.repositories.core.auth.CertificateCredentials;
import org.eclipse.mylyn.commons.repositories.core.auth.UserCredentials;
import org.eclipse.mylyn.internal.commons.net.CommonsNetPlugin;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

/**
 * @author Steffen Pingel
 */
@SuppressWarnings({ "nls", "restriction" })
public class CommonTestUtil {

	public enum PrivilegeLevel {
		ADMIN, ANONYMOUS, GUEST, READ_ONLY, USER
	}

	public static final String KEY_CREDENTIALS_FILE = "mylyn.credentials";

	private static final String KEY_IGNORE_LOCAL_SERVICES = "org.eclipse.mylyn.tests.ignore.local.services";

	private static final String KEY_IGNORE_GLOBAL_SERVICES = "org.eclipse.mylyn.tests.ignore.global.services";

	private final static int MAX_RETRY = 5;

	private static final String ciServer = TestUrl.DEFAULT.getHttpsOk().toString();

	/**
	 * Returns the given file path with its separator character changed from the given old separator to the given new separator.
	 *
	 * @param path
	 *            a file path
	 * @param oldSeparator
	 *            a path separator character
	 * @param newSeparator
	 *            a path separator character
	 * @return the file path with its separator character changed from the given old separator to the given new separator
	 */
	public static String changeSeparator(final String path, final char oldSeparator, final char newSeparator) {
		return path.replace(oldSeparator, newSeparator);
	}

	/**
	 * Copies the given source file to the given destination file.
	 */
	public static void copy(final File source, final File dest) throws IOException {
		try (InputStream in = new FileInputStream(source);
				OutputStream out = new BufferedOutputStream(new FileOutputStream(dest))) {
			transferData(in, out);
		}
	}

	/**
	 * Copies all files in the current data directory to the specified folder. Will overwrite.
	 */
	public static void copyFolder(final File sourceFolder, final File targetFolder) throws IOException {
		for (final File sourceFile : sourceFolder.listFiles()) {
			if (sourceFile.isFile()) {
				final var destFile = new File(targetFolder, sourceFile.getName());
				copy(sourceFile, destFile);
			}
		}
	}

	/**
	 * Copies all files in the current data directory to the specified folder. Will overwrite.
	 */
	public static void copyFolderRecursively(final File sourceFolder, final File targetFolder) throws IOException {
		for (final File sourceFile : sourceFolder.listFiles()) {
			if (sourceFile.isFile()) {
				final var destFile = new File(targetFolder, sourceFile.getName());
				copy(sourceFile, destFile);
			} else if (sourceFile.isDirectory()) {
				final var destDir = new File(targetFolder, sourceFile.getName());
				if (!destDir.exists()) {
					if (!destDir.mkdir()) {
						throw new IOException("Unable to create destination folder: " + destDir.getAbsolutePath());
					}
				}
				copyFolderRecursively(sourceFile, destDir);
			}
		}
	}

	public static File createTempFileInPlugin(final Plugin plugin, final IPath path) {
		var stateLocation = plugin.getStateLocation();
		stateLocation = stateLocation.append(path);
		return stateLocation.toFile();
	}

	public static File createTempFolder(final String prefix) throws IOException {
		final var location = File.createTempFile(prefix, null);
		location.delete();
		location.mkdirs();
		return location;
	}

	public static void delete(final File file) {
		if (file.exists()) {
			for (var i = 0; i < MAX_RETRY; i++) {
				if (file.delete()) {
					i = MAX_RETRY;
				} else {
					try {
						Thread.sleep(1000); // sleep a second
					} catch (final InterruptedException e) {
						// don't need to catch this
					}
				}
			}
		}
	}

	public static void deleteFolder(final File path) {
		if (path.isDirectory()) {
			for (final File file : path.listFiles()) {
				file.delete();
			}
			path.delete();
		}
	}

	public static void deleteFolderRecursively(final File path) {
		final var files = path.listFiles();
		if (files != null) {
			for (final File file : files) {
				if (file.isDirectory()) {
					deleteFolderRecursively(file);
				} else {
					file.delete();
				}
			}
		}
		path.delete();
	}

	public static CertificateCredentials getCertificateCredentials() {
		File keyStoreFile;
		try {
			keyStoreFile = CommonTestUtil.getFile(CommonTestUtil.class, "testdata/keystore");
			final var password = CommonTestUtil.getUserCredentials().getPassword();
			return new CertificateCredentials(keyStoreFile.getAbsolutePath(), password, null);
		} catch (final IOException cause) {
			final var e = new MylynResourceMissingException("Failed to load keystore file");
			e.initCause(cause);
			throw e;
		}
	}

	public static boolean hasCredentials(final PrivilegeLevel level) {
		try {
			CommonTestUtil.getCredentials(level);
			return true;
		} catch (final MylynResourceMissingException error) {
			return false;
		}
	}

	public static UserCredentials getCredentials(final PrivilegeLevel level) {
		return getCredentials(level, null);
	}

	public static UserCredentials getCredentials(final PrivilegeLevel level, String realm) {
		final var properties = new Properties();
		try {
			File file;
			final var filename = System.getProperty(KEY_CREDENTIALS_FILE);
			if (filename != null) {
				// 1. use user specified file
				file = new File(filename);
			} else {
				// 2. check in home directory
				file = new File(new File(System.getProperty("user.home"), ".mylyn"), "credentials.properties");
				if (!file.exists()) {
					// 3. fall back to included credentials file
					file = getFile(CommonTestUtil.class, "testdata/credentials.properties");
				}
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
		};

	}

	private static boolean isOsgiVersion310orNewer(final ClassLoader classLoader) {
		return classLoader.getClass().getName().equals("org.eclipse.osgi.internal.loader.ModuleClassLoader") // user before 4.4M4
				|| classLoader.getClass().getName().equals("org.eclipse.osgi.internal.loader.EquinoxClassLoader");
	}

	public static File getFile(final Object source, final String filename) throws IOException {
		final var clazz = source instanceof Class<?> ? (Class<?>) source : source.getClass();
		if (Platform.isRunning()) {
			final var classLoader = clazz.getClassLoader();
			try {
				if (isOsgiVersion310orNewer(classLoader)) {
					return Objects.requireNonNull(getFileFromClassLoader4Luna(filename, classLoader));
				} else {
					return Objects.requireNonNull(getFileFromClassLoaderBeforeLuna(filename, classLoader));
				}
			} catch (final Exception e) {
				final var exception = new MylynResourceMissingException(
						NLS.bind("Could not locate {0} using classloader for {1}", filename, clazz));
				exception.initCause(e);
				throw exception;
			}
		} else {
			return getFileFromNotRunningPlatform(filename, clazz);
		}
	}

	private static File getFileFromNotRunningPlatform(String filename, final Class<?> clazz)
			throws UnsupportedEncodingException, IOException {
		final var localURL = clazz.getResource("");
		var path = URLDecoder.decode(localURL.getFile(), Charset.defaultCharset().name());
		final var i = path.indexOf("!");
		if (i != -1) {
			final var j = path.lastIndexOf(File.separatorChar, i);
			if (j != -1) {
				path = path.substring(0, j) + File.separator;
			} else {
				throw new MylynResourceMissingException("Unable to determine location for '" + filename + "' at '" + path + "'");
			}
			// class file is nested in jar, use jar path as base
			if (path.startsWith("file:")) {
				path = path.substring(5);
			}
			return new File(path + filename);
		} else {
			// remove all package segments from name
			var directory = clazz.getName().replaceAll("[^.]", "");
			directory = directory.replaceAll(".", "../");
			if (path.contains("/bin/")) {
				// account for bin/ when running from Eclipse workspace
				directory += "../";
			} else if (path.contains("/target/classes/")) {
				// account for bin/ when running from Eclipse workspace
				directory += "../../";
			}
			filename = path + (directory + filename).replace("/", File.separator);
			return new File(filename).getCanonicalFile();
		}
	}

	private static File getFileFromClassLoaderBeforeLuna(final String filename, final ClassLoader classLoader) throws Exception {
		final var classpathManager = MethodUtils.invokeExactMethod(classLoader, "getClasspathManager", null);
		final var baseData = MethodUtils.invokeExactMethod(classpathManager, "getBaseData", null);
		final var bundle = (Bundle) MethodUtils.invokeExactMethod(baseData, "getBundle", null);
		final var localURL = FileLocator.toFileURL(bundle.getEntry(filename));
		return new File(localURL.getFile());
	}

	private static File getFileFromClassLoader4Luna(final String filename, final ClassLoader classLoader) throws Exception {
		final var classpathManager = MethodUtils.invokeExactMethod(classLoader, "getClasspathManager", null);
		final var generation = MethodUtils.invokeExactMethod(classpathManager, "getGeneration", null);
		final var bundleFile = MethodUtils.invokeExactMethod(generation, "getBundleFile", null);
		return (File) MethodUtils.invokeExactMethod(bundleFile, "getFile", new Object[] { filename, true },
				new Class[] { String.class, boolean.class });
	}

	public static InputStream getResource(final Object source, final String filename) throws IOException {
		final var clazz = source instanceof Class<?> ? (Class<?>) source : source.getClass();
		final var classLoader = clazz.getClassLoader();
		final var in = classLoader.getResourceAsStream(filename);
		if (in == null) {
			final var file = getFile(source, filename);
			if (file != null) {
				return new FileInputStream(file);
			}
		}
		if (in == null) {
			throw new IOException(NLS.bind("Failed to locate ''{0}'' for ''{1}''", filename, clazz.getName()));
		}
		return in;
	}

	public static UserCredentials getUserCredentials() {
		return getCredentials(PrivilegeLevel.USER, null);
	}

	public static String read(final File source) throws IOException {
		final InputStream in = new FileInputStream(source);
		try (in) {
			final var sb = new StringBuilder();
			final var buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				sb.append(new String(buf, 0, len));
			}
			return sb.toString();
		}
	}

	/**
	 * Returns whether to run a limited suite of tests. Returns true, unless a system property has been set to force running of all tests.
	 */
	public static boolean runHeartbeatTestsOnly() {
		return !Boolean.getBoolean("org.eclipse.mylyn.tests.all");
	}

	/**
	 * Returns whether to run a limited suite of tests. Returns true, unless a system property has been set to force running of all network
	 * tests (using CI Server).
	 */
	public static boolean runNonCIServerTestsOnly() {
		return !runOnCIServerTestsOnly();
	}

	public static boolean runOnCIServerTestsOnly() {

		return TestConfiguration.URL_SERVICES_CI_DEFAULT.equals(TestConfiguration.URL_SERVICES_DEFAULT)
				|| TestConfiguration.URL_SERVICES_CI_DEFAULT.equals(TestConfiguration.URL_SERVICES_LOCALHOST);
	}

	/**
	 * Unzips the given zip file to the given destination directory extracting only those entries the pass through the given filter.
	 *
	 * @param zipFile
	 *            the zip file to unzip
	 * @param dstDir
	 *            the destination directory
	 * @throws IOException
	 *             in case of problem
	 */
	public static void unzip(final ZipFile zipFile, final File dstDir) throws IOException {
		unzip(zipFile, dstDir, dstDir, 0);
	}

	public static void write(final String fileName, final StringBuffer content) throws IOException {
		try (Writer writer = new FileWriter(fileName)) {
			writer.write(content.toString());
		}
	}

	private static UserCredentials createCredentials(final Properties properties, final String prefix, final String defaultUsername,
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
			throw new MylynResourceMissingException("username or password not found for " + prefix
					+ " in <plug-in dir>/credentials.properties, make sure file is valid");
		}

		return new UserCredentials(username, password);
	}

	/**
	 * Copies all bytes in the given source stream to the given destination stream. Neither streams are closed.
	 *
	 * @param source
	 *            the given source stream
	 * @param destination
	 *            the given destination stream
	 * @throws IOException
	 *             in case of error
	 */
	private static void transferData(final InputStream in, final OutputStream out) throws IOException {
		final var buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}

	private static void unzip(final ZipFile zipFile, final File rootDstDir, final File dstDir, final int depth) throws IOException {

		final Enumeration<? extends ZipEntry> entries = zipFile.entries();

		try {
			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				final var entryName = entry.getName();
				final var file = new File(dstDir, changeSeparator(entryName, '/', File.separatorChar));
				file.getParentFile().mkdirs();
				try (var src = zipFile.getInputStream(entry);
						OutputStream dst = new BufferedOutputStream(new FileOutputStream(file))) {
					transferData(src, dst);
				}
			}
		} finally {
			try {
				zipFile.close();
			} catch (final IOException e) {
				// don't need to catch this
			}
		}
	}

	public static boolean isCertificateAuthBroken() {
		// not entirely correct since 1.6.0_3 would also satisfy this check but it should be sufficient in reality
		return new VersionRange("[0.0.0,1.6.0.25]").isIncluded(CoreUtil.getRuntimeVersion());
	}

	public static boolean hasCertificateCredentials() {
		try {
			CommonTestUtil.getCertificateCredentials();
			return true;
		} catch (final MylynResourceMissingException error) {
			return false;
		}
	}

	public static String getShortUserName(final UserCredentials credentials) {
		final var username = credentials.getUserName();
		if (username.contains("@")) {
			return username.substring(0, username.indexOf("@"));
		}
		return username;
	}

	/**
	 * Activates manual proxy configuration in the Ecipse proxy service if system proxy support is not available. This sets proxy
	 * configuration to Java system properties.
	 * <p>
	 * This work around is required on e3.5/gtk.x86_64 where system proxy settings get enabled but the proxy configuration is not actually
	 * detected resulting in a broken configuration.
	 * <p>
	 * Please note that this only works for http proxies. The https proxy system property is ignored.
	 *
	 * @see #isHttpsProxyBroken()
	 */
	public static boolean fixProxyConfiguration() {
		if (Platform.isRunning() && CommonsNetPlugin.getProxyService() != null
				&& CommonsNetPlugin.getProxyService().isSystemProxiesEnabled()
				&& !CommonsNetPlugin.getProxyService().hasSystemProxies()) {
			System.err.println("Forcing manual proxy configuration");
			CommonsNetPlugin.getProxyService().setSystemProxiesEnabled(false);
			CommonsNetPlugin.getProxyService().setProxiesEnabled(true);
			return true;
		}
		return false;
	}

	public static void dumpSystemInfo(final PrintStream out) {
		final var p = System.getProperties();
		if (Platform.isRunning()) {
			p.put("build.system", Platform.getOS() + "-" + Platform.getOSArch() + "-" + Platform.getWS());
		} else {
			p.put("build.system", "standalone");
		}
		var info = "System: ${os.name} ${os.version} (${os.arch}) / ${build.system} / ${java.vendor} ${java.vm.name} ${java.version}";
		for (final Entry<Object, Object> entry : p.entrySet()) {
			info = info.replaceFirst(Pattern.quote("${" + entry.getKey() + "}"), entry.getValue().toString());
		}
		out.println(info);

		out.print("HTTPS Proxy : " + WebUtil.getProxyForUrl(ciServer) + " (Platform)");
		try {
			out.print(" / " + ProxySelector.getDefault().select(new URI(ciServer)) + " (Java)");
		} catch (final URISyntaxException e) {
			// ignore
		}
		out.println();
		out.println();
	}

	public static boolean isEclipse4() {
		return Platform.getBundle("org.eclipse.e4.core.commands") != null;
	}

	/**
	 * If Eclipse proxy configuration is set to manual https proxies aren't detected and hence tests that rely on https connections may
	 * fail. Use this method to detect whether https is configured correctly.
	 *
	 * @see #fixProxyConfiguration()
	 */
	public static boolean isHttpsProxyBroken() {
		// checks if http and https proxy configuration matches
		final var httpProxy = WebUtil.getProxyForUrl(TestUrl.DEFAULT.getHttpOk().toString()); // NO http on ci server, points to https
		final var httpsProxy = WebUtil.getProxyForUrl(TestUrl.DEFAULT.getHttpsOk().toString());
		return CoreUtil.areEqual(httpProxy, httpsProxy);
	}

	/**
	 * Returns whether to run on local services if present. Returns false, unless a system property has been set to force to ignore local
	 * running services.
	 */
	public static boolean ignoreLocalTestServices() {
		return Boolean.getBoolean(KEY_IGNORE_LOCAL_SERVICES);
	}

	public static boolean ignoreGlobalTestServices() {
		return Boolean.getBoolean(KEY_IGNORE_GLOBAL_SERVICES);
	}

	public static boolean isBehindProxy() {
		return NetUtil.getProxyForUrl(TestUrl.DEFAULT.getHttpsOk().toString()) != null;
	}

	public static boolean skipBrowserTests() {
		return Boolean.getBoolean("mylyn.test.skipBrowserTests");
	}

	public static boolean bundleWithNameIsPresent(final String name) {
		final var bundle = Platform.getBundle(name);
		return bundle != null;
	}

}
