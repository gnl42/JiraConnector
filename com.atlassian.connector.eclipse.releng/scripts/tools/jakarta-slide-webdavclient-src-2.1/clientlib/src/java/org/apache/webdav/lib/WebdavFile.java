/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/WebdavFile.java,v 1.5 2004/07/28 09:31:39 ib Exp $
 * $Revision: 1.5 $
 * $Date: 2004/07/28 09:31:39 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
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
 *
 */

package org.apache.webdav.lib;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

/**
 * Implements a file for WebDav
 *
 */
public class WebdavFile extends File {

  /** Directory separator */
  public static final char davSeparatorChar = '/';
  /** Directory separator */
  public static final String davSeparator = String.valueOf(davSeparatorChar);

  // WebdavFile may be absolute or relative
  private HttpURL httpUrl = null;
  private String relPath = null;

  /**
   * @param parent directory
   * @param child element in parent
   */
  public WebdavFile(WebdavFile parent, String child) throws URIException {
    this(
      parent.getAbsolutePath() + davSeparator + child,
      parent.getUser(),
      parent.getPass()
    );
  }

  /**
   * @param pathname complete path to element
   * @param user user name
   * @param pass password
   */
  public WebdavFile(String pathname, String user, String pass) throws URIException {
    this(new HttpURL(user, pass, null, -1, pathname));
  }

  /**
   * @param url file url
   * @param user user name
   * @param pass password
   */
  public WebdavFile(URL url, String user, String pass) throws URIException {
    this(url.getProtocol().equals("https")
        ? new HttpsURL(user, pass, url.getHost(), url.getPort(), url.getPath())
        : new HttpURL(user, pass, url.getHost(), url.getPort(), url.getPath()));
  }
  /**
   * @param parent parent name
   * @param child name of element in parent
   * @param user user name
   * @param pass password
   */
  public WebdavFile(String parent, String child, String user, String pass) throws URIException {
    this(parent + davSeparator + child, user, pass);
  }

  /**
   * @param httpUrl Webdav URL
   */
  public WebdavFile(HttpURL httpUrl) throws URIException {
    super(httpUrl.getURI());
    this.httpUrl = httpUrl;
  }

  /**
   * A WebdavFile with a relative file. Hence nobody keeps track
   * of a "working directory" the resulting object is only
   * a container for a String (pathname). You cannot do anything
   * usefull with an instance created this way
   */
  public WebdavFile(String aPath) {
    super(aPath);
    relPath = aPath;
  }

  private WebdavResource createRes() {
    try {
      if(httpUrl==null)
        throw new WebdavException(WebdavException.RELATIVE_FILE);
      return new WebdavResource(httpUrl);
    } catch(Exception e) {
      throw new WebdavException(e);
    }
  }

  private void closeRes(WebdavResource res) {
    try {
      if(res!=null)
        res.close();
    } catch(Exception e) {
      throw new WebdavException(e);
    }
  }

  private File [] toFileArray(List fileList) {
    File files [] = new File [fileList.size()];
    Iterator it = fileList.iterator();
    for(int i=0; i<files.length; i++)
      files[i] = (WebdavFile)it.next();
    return files;
  }

  public String getUser() throws URIException {
    if(relPath!=null)
      return null;
    return httpUrl.getUser();
  }

  public String getPass() throws URIException {
    if(relPath!=null)
      return null;
    return httpUrl.getPassword();
  }

  public String getName() {
    if(relPath!=null)
      return relPath;
    String escapedPath = httpUrl.getEscapedPath();
    String escapedName =
        URIUtil.getName(escapedPath.endsWith("/")
                        ? escapedPath.substring(0, escapedPath.length() - 1)
                        : escapedPath);
    try {
        return URIUtil.decode(escapedName);
    } catch (URIException e) {
        return escapedName;
    }
  }

  public String getParent() {
    if(relPath!=null)
      return null;
    String escapedPath = httpUrl.getEscapedPath();
    String parent = escapedPath.substring(
        0, escapedPath.lastIndexOf('/', escapedPath.length() - 2) + 1);
    if (parent.length() <= 1)
      return null;
    try {
        return URIUtil.decode(parent);
    } catch (URIException e) {
        return parent;
    }
  }

  public File getParentFile() {
    String parent = getParent();
    if(parent==null)
      return null;

    try {
      return new WebdavFile(parent, getUser(), getPass());
    } catch(URIException e) {
      throw new WebdavException(e);
    }
  }

  public String getPath() {
    if(relPath!=null)
      return relPath;
    try {
        return httpUrl.getURI();
    } catch (URIException e) {
        throw new WebdavException(e);
    }
  }

  public boolean isAbsolute() {
    return relPath==null;
  }

  public String getAbsolutePath() {
    return getPath();
  }

  public File getAbsoluteFile() {
    return this;
  }

  public String getCanonicalPath() {
    return getPath();
  }

  public File getCanonicalFile() {
    return this;
  }

  public URL toURL() throws MalformedURLException {
    if(relPath!=null)
      return null;
    try {
        return new URL(httpUrl.getURI());
    } catch (URIException e) {
        throw new MalformedURLException(e.getMessage());
    }
  }

  public boolean canRead() {
    return true;
  }

  public boolean canWrite() {
    WebdavResource res = null;
    try {
      res = createRes();
      return !res.isLocked();
    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public boolean exists() {
    WebdavResource res = null;
    try {
      res = createRes();
      return res.exists();
    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public boolean isDirectory() {
    WebdavResource res = null;
    try {
      res = createRes();
      return res.isCollection();
    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public boolean isFile() {
    return !isDirectory();
  }

  public boolean isHidden() {
    WebdavResource res = null;
    try {
      res = createRes();
      return res.getIsHidden();
    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public long lastModified() {
    WebdavResource res = null;
    try {
      res = createRes();
      return res.getGetLastModified();
    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public long length() {
    WebdavResource res = null;
    try {
      res = createRes();
      return res.getGetContentLength();
    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public boolean createNewFile() {
    WebdavResource res = null;
    try {
      res = createRes();
      return res.putMethod("");
    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public boolean delete() {
    WebdavResource res = null;
    try {
      res = createRes();
      return res.deleteMethod();
    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public void deleteOnExit() {
    throw new WebdavException(WebdavException.NOT_IMPLEMENTED);
  }

  public String[] list() {
    return list(null);
  }

  public String[] list(FilenameFilter filter) {
    File [] files = listFiles(filter);
    String [] names = new String[files.length];
    for(int i=0; i<names.length; i++)
      names[i] = files[i].getAbsolutePath();

    return names;
  }

  public File [] listFiles() {
    return listFiles((FilenameFilter)null);
  }

  public File [] listFiles(FilenameFilter filter) {

    WebdavResource res = null;
    try {
      res = createRes();
      WebdavResource allFiles [] = res.listWebdavResources();
      if(allFiles==null)
        return null;

      ArrayList filtered = new ArrayList();
      for(int i=0; i<allFiles.length; i++) {
        if(filter==null || filter.accept(this, allFiles[i].getDisplayName()))
          filtered.add( new WebdavFile(allFiles[i].getHttpURL()) );
      }

      return toFileArray(filtered);

    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public File [] listFiles(FileFilter filter) {
    WebdavResource res = null;
    try {
      res = createRes();
      WebdavResource allFiles [] = res.listWebdavResources();
      if(allFiles==null)
        return null;

      ArrayList filtered = new ArrayList();
      for(int i=0; i<allFiles.length; i++) {
        WebdavFile file = new WebdavFile(allFiles[i].getHttpURL());
        if(filter==null || filter.accept(file))
          filtered.add(file);
      }

      return toFileArray(filtered);

    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public boolean mkdir() {
    WebdavResource res = null;
    try {
      res = createRes();
      return res.mkcolMethod();
    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public boolean mkdirs() {
    return mkdir();
  }

  public boolean renameTo(File dest) {
    WebdavResource res = null;
    try {
      res = createRes();
      return res.moveMethod(dest.getAbsolutePath());
    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  public boolean setLastModified(long time) {
    throw new WebdavException(WebdavException.NOT_IMPLEMENTED);
  }

  public boolean setReadOnly() {
    WebdavResource res = null;
    try {
      res = createRes();
      res.setOverwrite(false);
      return true;
    } catch(Exception e) {
      throw new WebdavException(e);
    } finally {
      closeRes(res);
    }
  }

  /** todo */
  public static File[] listRoots() {
    throw new WebdavException(WebdavException.NOT_IMPLEMENTED);
  }

  /** todo */
  public static File createTempFile(String prefix, String suffix, File directory) {
    throw new WebdavException(WebdavException.NOT_IMPLEMENTED);
  }

  /** todo */
  public static File createTempFile(String prefix, String suffix) {
    return WebdavFile.createTempFile(prefix, suffix, null);
  }

  public String toString() {
    if(relPath!=null)
      return relPath;
    return httpUrl.getEscapedURI();
  }

  public int compareTo(File pathname) {
    if(pathname instanceof WebdavFile) {
      WebdavFile df = (WebdavFile)pathname;
      return df.getPath().compareTo(getPath());
    }
    return -1;
  }

  public int compareTo(Object o) {
    return compareTo((File)o);
  }

  public boolean equals(Object x) {
    if(x==null)
      return false;

    if(x instanceof WebdavFile) {
      WebdavFile xf = (WebdavFile)x;
      return xf.getPath().equals(getPath());
    }

    return false;
  }

  public int hashCode() {
    return getPath().hashCode();
  }
}

