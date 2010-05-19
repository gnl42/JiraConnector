/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/ui/WebdavSystemView.java,v 1.1.2.1 2004/10/11 08:18:14 luetzkendorf Exp $
 * $Revision: 1.1.2.1 $
 * $Date: 2004/10/11 08:18:14 $
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

package org.apache.webdav.ui;

import java.io.File;
import java.io.IOException;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.URIException;
import org.apache.webdav.lib.WebdavResource;

/**
 * WebdavSystemView.java
 */
public class WebdavSystemView extends FileSystemView {


    /** The WebDAV resource. */
    private WebdavResource webdavResource = null;
    private HttpURL rootURL = null;
    private WebdavFile homedir = null;
    private String username = null;
    private String password = null;
    private String uri = null;
    private String rootPath = null;

    private static final String newFolderString =
    UIManager.getString("FileChooser.other.newFolder");
    static FileSystemView fsv = null;


    public WebdavSystemView(String uri, String rootPath, String username,
                            String password)
        throws IllegalAccessError, URIException, IOException {
        try {
            this.rootURL = this.uriToHttpURL(uri + rootPath);
            this.uri = uri;
            this.rootURL.setUserinfo(username, password);
            this.username = username;
            this.password = password;
            this.rootPath = rootPath;


            this.connect();
            //System.out.println("Connected successfully to  : " + this.rootURL);
            this.disconnect();

            // Create home directory object
            this.homedir = new WebdavFile(this.rootURL, this.rootURL);
            //System.out.println("Homedir : " + this.homedir);
        } catch (IllegalAccessError e) {
            System.err.println(e.toString());
            e.printStackTrace();
            throw e;
        } catch (URIException e) {
            System.err.println(e.toString());
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            System.err.println(e.toString());
            e.printStackTrace();
            throw e;
        }

    }

    private static HttpURL uriToHttpURL(String uri) throws URIException {
        HttpURL url = null;
        if (uri.startsWith("http://")) {
            url = new HttpURL(uri);
        } else if (uri.startsWith("https://")) {
            url = new HttpsURL(uri);
        } else {
            throw new URIException("Unknown protocol in URL " + uri);
        }
        return url;
    }

    public void disconnect() throws java.lang.UnknownError {
        try {
            this.webdavResource.close();
        } catch (Exception e) {
            System.err.println(e.toString());
            throw new UnknownError();
        }
    }
    public void connect() throws java.lang.IllegalAccessError {
        try {
            this.webdavResource = new WebdavResource(this.rootURL);
        } catch (Exception e) {
            System.err.println(e.toString());
            throw new IllegalAccessError();
        }
    }

    public static FileSystemView getFileSystemView() {
        try {
            if (fsv == null) {
                fsv = new WebdavSystemView("http://127.0.0.1", "/", "",  "");
            }
            return fsv;
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
    }

    /**
     * Returns a File object constructed in dir from the given filename.
     */
    public File createFileObject(File dir, String filename) {
        File file = null;
        if (dir == null) {
            file = new File(filename);
        } else {
            file = new File(dir, filename);
        }
        return file;
    }

    /**
     * Returns a File object constructed from the given path string.
     */
    public File createFileObject(String path) {
        File f = new File(path);
        if (isFileSystemRoot(f)) {
            f = createFileSystemRoot(f);
        }
        return f;
    }


    /**
     * Creates a new folder with a default folder name.
     */
    public File createNewFolder(File containingDir) throws IOException {
        try {
            if (containingDir == null) {
                throw new IOException("Containing directory is null:");
            }
            WebdavFile newFolder = null;
            HttpURL url = null;

            url = this.uriToHttpURL(containingDir.getPath() +
                                    WebdavFile.davSeparator + newFolderString);
            // Need to add user info so has access for queries
            url.setUserinfo(username, password);
            newFolder = new WebdavFile(url, this.rootURL);
            //System.out.println("new folder : " + newFolder.toString());

            this.connect();
            if (this.webdavResource.mkcolMethod(
                    newFolder.getAbsolutePath())) {
                //System.out.println("succeeded.");
                return newFolder;
            } else {
                System.err.println("failed.");
                System.err.println(this.webdavResource.getStatusMessage());
                throw new IOException(
                    this.webdavResource.getStatusMessage());
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
            return null;
        } finally {
            this.disconnect();
        }
    }
    /**
     * Returns all root partitions on this system. For example, on
     * Windows, this would be the "Desktop" folder, while on DOS this
     * would be the A: through Z: drives.
     */
    public File[] getRoots() {
        try {
            return new WebdavFile[] {this.homedir};
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Returns true if the file (directory) can be visited.
     * Returns false if the directory cannot be traversed.
     *
     * @param f the <code>File</code>
     * @return <code>true</code> if the file/directory can be traversed,
     *         otherwise <code>false</code>
     * @see JFileChooser#isTraversable
     * @see FileView#isTraversable
     */
    public Boolean isTraversable(File f) {
        try {
            //            System.out.println("isTraversable : " + f.getPath());
            //            WebdavFile webdavFile = null;
            //            this.connect();
            //            webdavFile = (WebdavFile) f;
            //            this.webdavResource.setHttpURL(new HttpURL(f.getPath()));
            //            System.out.println(this.webdavResource.getPath() + " : collection : " + this.webdavResource.isCollection());
            //            return Boolean.valueOf(this.webdavResource.isCollection());
            return f.isDirectory() ? Boolean.TRUE : Boolean.FALSE;
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
            return Boolean.FALSE;
        } finally {
            this.disconnect();
        }
    }

    /**
     * Name of a file, directory, or folder as it would be displayed in
     * a system file browser. Example from Windows: the "M:\" directory
     * displays as "CD-ROM (M:)"
     *
     * The default implementation gets information from the ShellFolder
     * class.
     *
     * @param f a <code>File</code> object
     * @return the file name as it would be displayed by a native file
     *         chooser
     * @see JFileChooser#getName
     */
    public String getSystemDisplayName(File f) {
        try {
            //System.out.println("getSystemDisplayName : getName          : "
                               //+ f.getName());
            //System.out.println("getSystemDisplayName : getAbsolutePath  : "
                               //+ f.getAbsolutePath());
            //System.out.println("getSystemDisplayName : getCanonicalPath : "
                               //+ f.getCanonicalPath());
            //System.out.println("getSystemDisplayName : getPath          : "
                               //+ f.getPath());
            return f.getName();
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
            return null;
        } finally {
            this.disconnect();
        }
    }

    /**
     * Type description for a file, directory, or folder as it would be
     * displayed in
     * a system file browser. Example from Windows: the "Desktop" folder
     * is desribed as "Desktop".
     *
     * Override for platforms with native ShellFolder implementations.
     *
     * @param f a <code>File</code> object
     * @return the file type description as it would be displayed by a
     *         native file chooser or null if no native information is
     *         available.
     * @see JFileChooser#getTypeDescription
     */
    public String getSystemTypeDescription(File f) {
        return null;
    }



    /**
     * Checks if <code>f</code> represents a real directory or file as
     * opposed to a special folder such as <code>"Desktop"</code>. Used by UI
     * classes to decide if a folder is selectable when doing directory
     * choosing.
     *
     * @param f a <code>File</code> object
     * @return <code>true</code> if <code>f</code> is a real file or directory.
     */
    public boolean isFileSystem(File f) {
        return true;
    }


    /**
     * Returns whether a file is hidden or not.
     */
    public boolean isHiddenFile(File f) {
        return f.isHidden();
    }


    /**
     * Is dir the root of a tree in the file system, such as a drive
     * or partition. Example: Returns true for "C:\" on Windows 98.
     *
     * @param f a <code>File</code> object representing a directory
     * @return <code>true</code> if <code>f</code> is a root of a filesystem
     * @see #isRoot
     */
    public boolean isFileSystemRoot(File dir) {
        try {
            return (rootURL.getPath().equals(dir.getPath()));
        }
        catch (Exception e) {
            System.err.println("isFileSystemRoot" + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Used by UI classes to decide whether to display a special icon
     * for drives or partitions, e.g. a "hard disk" icon.
     *
     * The default implementation has no way of knowing, so always returns
     * false.
     *
     * @param dir a directory
     * @return <code>false</code> always
     */
    public boolean isDrive(File dir) {
        return false;
    }

    /**
     * Used by UI classes to decide whether to display a special icon
     * for a floppy disk. Implies isDrive(dir).
     *
     * The default implementation has no way of knowing, so always returns
     * false.
     *
     * @param dir a directory
     * @return <code>false</code> always
     */
    public boolean isFloppyDrive(File dir) {
        return false;
    }

    /**
     * Used by UI classes to decide whether to display a special icon
     * for a computer node, e.g. "My Computer" or a network server.
     *
     * The default implementation has no way of knowing, so always returns
     * false.
     *
     * @param dir a directory
     * @return <code>false</code> always
     */
    public boolean isComputerNode(File dir) {
        return false;
    }

    // Providing default implementations for the remaining methods
    // because most OS file systems will likely be able to use this
    // code. If a given OS can't, override these methods in its
    // implementation.

    public File getHomeDirectory() {
        return this.homedir;
    }

    /**
     * Return the user's default starting directory for the file chooser.
     *
     * @return a <code>File</code> object representing the default
     *         starting folder
     */
    public File getDefaultDirectory() {
        return this.homedir;
    }


    /**
     * Gets the list of shown (i.e. not hidden) files.
     */
    public File[] getFiles(File dir, boolean useFileHiding) {
        try {

            String filenames[] = null;
            WebdavFile files[] = null;
            HttpURL url = null;
            String path = null;
            String localDir = null;

            this.connect();
            // Now we try to list files

            path = dir.getPath();
            //System.out.println("getFiles : RAW PATH : '" + path + "'");

            // If path contains the server preamble, we need to extract that
            // and have the path only
            if (path.startsWith("http")) {
                //System.out.println("getFiles : preample : " + this.uri);
                path = path.replaceAll(this.uri, "");
            }
            if (!path.endsWith("/")) {
                path = path + "/";
            }

            //System.out.println("getFiles : path : " + path);

            this.webdavResource.setPath(path);
            filenames = this.webdavResource.list();
            files = new WebdavFile[filenames.length];
            for (int i = 0; i < filenames.length; i++) {
                //System.out.println("file : " + filenames[i]);
                // Lets try to construct a uri from the dir
                // given and the current file

                localDir = dir.getPath();
                if (!localDir.endsWith("/")) localDir = localDir + "/";

                String filepath =  localDir + filenames[i];
                //System.out.println("getFiles : file fullpath : " + filepath);
                url = this.uriToHttpURL(filepath);
                // Need to add user info so has access for queries
                url.setUserinfo(username, password);
                files[i] = new WebdavFile(url, this.rootURL);
            }
            return files;
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
            return null;
        } finally {
            this.disconnect();
        }
    }



    /**
     * Returns the parent directory of <code>dir</code>.
     * @param dir the <code>File</code> being queried
     * @return the parent directory of <code>dir</code>, or
     *   <code>null</code> if <code>dir</code> is <code>null</code>
     */
    public File getParentDirectory(File dir) {
        //System.out.println("dir : " + dir);
        if (dir == null) {
            return null;
        } else if (dir.equals(this.homedir)) {
            return this.homedir;
        } else {
            //System.out.println("getParentDirectory : calling getParentFile" + dir);
            return dir.getParentFile();
        }
    }

    /**
     * Creates a new <code>File</code> object for <code>f</code> with
     * correct behavior for a file system root directory.
     *
     * @param f a <code>File</code> object representing a file system root
     *      directory, for example "/" on Unix or "C:\" on Windows.
     * @return a new <code>File</code> object
     */
    protected File createFileSystemRoot(File f) {
        try {
            return new FileSystemRoot((WebdavFile) f);

        } catch (Exception e) {
            System.err.println("createFileSystemRoot : " + e.toString());
            return null;
        }
    }

    static class WebdavFile extends org.apache.webdav.lib.WebdavFile {
        protected WebdavResource webdavResource = null;
        protected HttpURL rootUrl = null;

        public WebdavFile(HttpURL pathUrl, HttpURL rootUrl) throws
            URIException, IOException {
            super(pathUrl);
            this.webdavResource = new WebdavResource(pathUrl);
            this.rootUrl = rootUrl;
        }


        public String getName() {
            String name = null;

            // Get the base name
            name = super.getName();

            // If is a directory, we need to add a trailing slash
            if (this.isDirectory()) {
                name = name + "/";
            }
            return name;
        }
        public boolean isRoot() {
            try {
                String path = null;
                String root = null;
                root =  this.rootUrl.getPath();
                path = this.webdavResource.getHttpURL().getPath();
                //System.out.println("isRoot : root : " + root);
                //System.out.println("isRoot : path : " + path);

                // If we are at the root already
                if (root .equalsIgnoreCase(path)) {
                    //System.out.println("isRoot : we are at root");
                    return true;
                } else {
                    // otherwise call original
                    return false;
                }
            }
            catch (Exception e) {
                System.err.println(e.toString());
                e.printStackTrace();
                return false;
            }
        }
        public String getParent() {
            try {
                // If we are at the root already
                if (this.isRoot()) {
                    //System.out.println("getParentFile : we are at root");
                    return null;
                } else {
                    // otherwise call original
                    String escapedPath =
                        this.webdavResource.getHttpURL().getPath();
                    String parent = escapedPath.substring(
                        0, escapedPath.lastIndexOf('/', escapedPath.length() -
                                                   2) + 1);
                    //System.out.println("getParent : escapedPath : " + escapedPath);
                    return super.getParent();
                }
            }
            catch (Exception e) {
                System.err.println(e.toString());
                e.printStackTrace();
                return null;
            }
        }
        public File getParentFile() {
            try {
                HttpURL httpURL = null;
                String parent = null;
                parent = this.getParent();
                if (parent == null) {
                    //System.out.println("getParentFile : at root so return null");
                    return null;
                } else {
                    httpURL = this.rootUrl;
                    httpURL.setPath(parent);
                    //System.out.println("getParentFile : set path to " + parent);
                    return new WebdavFile(httpURL, this.rootUrl);
                }
            } catch (Exception e) {
                System.err.println(e.toString());
                e.printStackTrace();
                return null;
            }

        }
        public boolean exists() {
            //System.out.println("exists : " + this.getPath());
            return this.webdavResource.exists();
        }
        public boolean isDirectory() {
            //System.out.println("isDirectory : " + this.getPath()  + " : " +
                               //this.webdavResource.isCollection());
            return this.webdavResource.isCollection();
        }
    }



    static class FileSystemRoot extends WebdavFile {

        public FileSystemRoot(HttpURL rootUrl) throws URIException,
            IOException {
            super(rootUrl, rootUrl);
        }
        public FileSystemRoot(WebdavFile webdavFile) throws URIException,
            IOException {
            super(webdavFile.rootUrl, webdavFile.rootUrl);
        }

    }

    // Test code
    public static void main(String args[]) throws Exception {
        javax.swing.JFrame frame = new javax.swing.JFrame();

        // Setup
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser(
            new WebdavSystemView(
                "http://localhost:8080", "/slide/files", "root", "root"));
        fc.showOpenDialog(frame);
    }

}
