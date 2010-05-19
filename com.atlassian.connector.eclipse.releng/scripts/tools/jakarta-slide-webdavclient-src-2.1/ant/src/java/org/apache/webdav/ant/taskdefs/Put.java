/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/taskdefs/Put.java,v 1.3.2.1 2004/08/15 13:01:15 luetzkendorf Exp $
 * $Revision: 1.3.2.1 $
 * $Date: 2004/08/15 13:01:15 $
 * ========================================================================
 * Copyright 2004 The Apache Software Foundation
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
 * ========================================================================
 */
package org.apache.webdav.ant.taskdefs;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URIException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterSet;
import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.types.ZipScanner;
import org.apache.tools.ant.util.LineTokenizer;
import org.apache.webdav.ant.Mimetypes;
import org.apache.webdav.ant.Utils;
import org.apache.webdav.lib.methods.DepthSupport;

/**
 * WebDAV task for writing files to an WebDAV server.
 * 
 * @see <a href="../doc-files/tasks.htm#davput">Tasks documentation</a> 
 * @version $Revision: 1.3.2.1 $
 */
public class Put extends WebdavTask {
   private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream"; 
   /** Should we try to lock the remote resource as we upload it? */
   private boolean lock = true;
   /** The locktoken provided or or self created when lock is true. */
   private String locktoken = null;
   /** The timeout to be used with locking. */
   private int lockTimeout = 3600;
   private String lockOwnerInfo = null;
   /** The sets of files that will be sent to the web server. */
   private List filesets = new ArrayList();
   private List zipfilesets = new ArrayList();
   /** Single file to be putted. */
   private File file = null;
   private boolean overwrite = false; 
   
   private FilterSetCollection filterSets = new FilterSetCollection();
   private String encoding = null;
   
   private int countWrittenFiles = 0;
   private int countOmittedFiles = 0;
   
   // configuration methods
   /**
    * Adds a set of files (nested fileset attribute).
    */
   public void addFileset(FileSet set) {
      filesets.add(set);
   }
   public void setLock(boolean lock) {
      this.lock = lock;
   }

   public void setOverwrite(boolean value) {
      this.overwrite = value;
   }
   public void setLocktoken(String token) {
      this.locktoken = token;
      if (!this.locktoken.startsWith("opaquelocktoken:")) {
         throw new BuildException("Invalid locktoken: " + token);
      }
   }
   public FilterSet createFilterSet() {
      FilterSet filterSet = new FilterSet();
      this.filterSets.addFilterSet(filterSet);
      return filterSet;
   }
   public void setEncoding(String enc) {
      this.encoding = enc;
   }
   public void setFile(File file) {
      this.file = file;
   }
   public void setTimeout(int value) {
      if (value > 0) {
         this.lockTimeout = value;
      } else {
         throw new BuildException("Invalid timeout value (Must be " +
               "positive integer)");
      }
   }
   public void setOwnerinfo(String value) {
      this.lockOwnerInfo = value;
   }
   /**
    * Does the work.
    * 
    * @exception BuildException Thrown in unrecoverable error.
    */
   public void execute() throws BuildException {
      boolean selfCreatedLock = false;
      
      validate();

      try {
         log("Uploading to: " + getUrl(), ifVerbose());
         
         if (this.file == null) {
            Utils.assureExistingCollection(getHttpClient(), getUrl(), this.locktoken);
         }

         // lock URL if requested and no locktoken explicitly provided
         if (this.lock && this.locktoken == null) {
            log("Locking " + getUrl(), ifVerbose());
            this.locktoken = Utils.lockResource(
                   getHttpClient(),
                   getUrl(),
                   this.lockOwnerInfo,
                   DepthSupport.DEPTH_INFINITY,
                   this.lockTimeout);
            log("locktoken: " + this.locktoken, Project.MSG_DEBUG);
            selfCreatedLock = true;
         }
         
         if (this.file != null) {
            // put a single file
            if (Utils.collectionExists(getHttpClient(), getUrl())) {
               // if the given URL is a collection put a file named as the given one
               setUrl(assureCollectionUrl(getUrl()));
               uploadFile(this.file.getName(), this.file);
            } else {
               if (getUrl().getURI().endsWith("/")) {
                  Utils.assureExistingCollection(getHttpClient(), getUrl(), this.locktoken);
                  uploadFile(this.file.getName(), this.file);
               } else {
                  HttpURL targetColl = Utils.createHttpURL(getUrl(), ".");
                  Utils.assureExistingCollection(getHttpClient(), targetColl, this.locktoken);
                  uploadFile(getUrl(), this.file, this.file.getName());
               }
            }
         } else { 
            for (int i = 0; i < filesets.size(); i++) {
               FileSet fileset = (FileSet) filesets.get(i);
               uploadFileSet(fileset);
            }
            for (int i = 0; i < zipfilesets.size(); i++) {
               ZipFileSet fileset = (ZipFileSet) zipfilesets.get(i);
               uploadZipFileSet(fileset);
            }
         }
         
         log("Puted " + this.countWrittenFiles 
               + (this.countWrittenFiles == 1 ? " file" : " files")
               + " to " + getUrl(),
               this.countWrittenFiles > 0 ? Project.MSG_INFO : ifVerbose());

      } 
      catch (IOException e) {
         throw Utils.makeBuildException("Put error!", e);
      }
      finally {
         try {
            if (this.locktoken != null && selfCreatedLock) {
               log("Unlocking " + getUrl(), ifVerbose());
               Utils.unlockResource(getHttpClient(),
                                    getUrl(),
                                    this.locktoken);
            }
         }
         catch (IOException e) {
            throw Utils.makeBuildException("Can't unlock!", e);
         }
      }
   }
   
   protected void validate() {
      super.validate();
      
      if (this.encoding == null && this.filterSets.hasFilters()) {
         log("If filterSets are used a file encoding should be specified!", 
               Project.MSG_WARN);
         this.encoding = "ISO-8859-1"; // TODO what sould be the default
      }
      if (this.file != null && 
            (this.filesets.size() > 0 || this.zipfilesets.size() > 0)) {
         throw new BuildException("No filesets allowed if file is set.");
      }
      
      if (this.file != null && !(this.file.isFile() && this.file.exists())) {
         throw new BuildException("File attribute must point to en existing file");
      } 
      
      if (this.file == null) {
         try {
            setUrl(assureCollectionUrl(getUrl()));
         } catch (URIException e) {
            throw new BuildException("Problem with URI: " + getUrl(), e);
         }
      }
      if (this.lockOwnerInfo == null) {
         this.lockOwnerInfo =  "ant-webdav " + getUserid();
      }
   }
   
   private void uploadFileSet(FileSet fileSet) 
      throws IOException
   {
      DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
      String basedir = scanner.getBasedir().getAbsolutePath();

      // assert that all required collections does exist
      for(Iterator i = determineRequiredDirectories(scanner); i.hasNext();) {
         String dir = (String)i.next();
         if (dir.equals("")) {
            Utils.assureExistingCollection(getHttpClient(), 
                                           getUrl(),
                                           this.locktoken);
         } else {
            HttpURL collURL = Utils.createHttpURL(getUrl(), dir + "/");
            Utils.assureExistingCollection(getHttpClient(), 
                                           collURL,
                                           this.locktoken);
         }
      }            
      
      // write all files
      String[] files = scanner.getIncludedFiles();
      for (int i = 0; i < files.length; ++i) {
         File file = getProject().resolveFile(basedir + File.separator + files[i]);
         uploadFile(asDavPath(files[i]), file);
      }
   }
   
   private Iterator determineRequiredDirectories(DirectoryScanner scanner) {
      Set result = new HashSet();
      
      // determine all directories that contain included files
      String[] files = scanner.getIncludedFiles();
      for (int i = 0; i < files.length; i++) {
         String file = asDavPath(files[i]);
         int slashPos = file.lastIndexOf('/');
         if (slashPos != -1) {
            result.add(file.substring(0, slashPos));
         }
      }
      
      // determine all included directories
      String[] dirs = scanner.getIncludedDirectories();
      for(int i = 0; i < dirs.length; i++) {
         result.add(asDavPath(dirs[i]));
      }
      
      return result.iterator();
   }
   
   /**
    * Puts a file to a resource relative to the url attribute.
    * @param relative path relative
    * @param file file to be written
    * @throws IOException
    */
   private void uploadFile(String relative, File file)
      throws IOException
   {
      HttpURL url = Utils.createHttpURL(getUrl(), relative);
      uploadFile(url, file, relative);
   }
   /**
    * Puts a file to a given URL.
    * @param relative for logging purposes only. 
    */
   private void uploadFile(HttpURL url, File file, String relative)
      throws IOException
   {

      boolean putit = false;
      try {
         if (this.overwrite) {
            putit = true;
         } else {
            // check last modified date (both GMT)
            long remoteLastMod = Utils.getLastModified(getHttpClient(), url); 
            long localLastMod = file.lastModified();
            putit = localLastMod > remoteLastMod;
         }
      }
      catch (HttpException e) {
         switch (e.getReasonCode()) {
            case HttpStatus.SC_NOT_FOUND:
               putit = true;
               break;
            default:
               throw Utils.makeBuildException("Can't get lastmodified!?", e);
         }
      }

      if (putit) {
         log("Uploading: " + relative, ifVerbose());
         try {
            String contentType = Mimetypes.getMimeType(file, DEFAULT_CONTENT_TYPE);
            if (this.filterSets.hasFilters()) {
               // TODO this part doesn't look nice
               InputStreamReader reader = new InputStreamReader(
                                          new FileInputStream(file), this.encoding);
               ByteArrayOutputStream out = new ByteArrayOutputStream();
               LineTokenizer tok = new LineTokenizer();
               tok.setIncludeDelims(true);
               
               for (String l = tok.getToken(reader); l != null; l = tok.getToken(reader)) {
                  out.write(this.filterSets.replaceTokens(l).getBytes(this.encoding));
               }
               Utils.putFile(getHttpClient(), url, 
                     new ByteArrayInputStream(out.toByteArray()), 
                     contentType, this.locktoken);
            } else {
               Utils.putFile(getHttpClient(), url,
                     new FileInputStream(file), 
                     contentType, this.locktoken);
            }
            this.countWrittenFiles++;
         } 
         catch (HttpException e) {
            throw Utils.makeBuildException("Can't upload " + url, e);
         }
      } else {
         countOmittedFiles++;
         log("Omitted: " + relative + " (uptodate)", ifVerbose());
      }
   }
   
   private void uploadZipFileSet(ZipFileSet fileSet) 
      throws IOException
   {
      DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
      
      ZipFile zipFile = new ZipFile(fileSet.getSrc());
   
      // assert that all required collections does exist
      for(Iterator i = determineRequiredDirectories(scanner); i.hasNext();) {
         String dir = (String)i.next();
         if (dir.equals("")) {
            Utils.assureExistingCollection(getHttpClient(), 
                                           getUrl(),
                                           this.locktoken);
         } else {
            HttpURL collURL = Utils.createHttpURL(getUrl(), dir + "/");
            Utils.assureExistingCollection(getHttpClient(), 
                                           collURL,
                                           this.locktoken);
         }
      }            
      
      // write all files
      String[] files = scanner.getIncludedFiles();
      for (int i = 0; i < files.length; ++i) {
         uploadZipEntry(Utils.createHttpURL(getUrl(), files[i]), files[i], zipFile);
      }
   }
   
   private void uploadZipEntry(HttpURL url, String name, ZipFile zipFile)
      throws IOException
   {
      boolean putit = false;
      ZipEntry entry = zipFile.getEntry(name);
      
      try {
         if (this.overwrite) {
            putit = true;
         } else {
            // check last modified date (both GMT)
            long remoteLastMod = Utils.getLastModified(getHttpClient(), url); 
            long localLastMod = entry.getTime();
            putit = localLastMod > remoteLastMod;
         }
      }
      catch (HttpException e) {
         switch (e.getReasonCode()) {
            case HttpStatus.SC_NOT_FOUND:
               putit = true;
               break;
            default:
               throw Utils.makeBuildException("Can't get lastmodified!?", e);
         }
      }
      
      if (putit) {
         log("Uploading: " + name, ifVerbose());
         String contentType = Mimetypes.getMimeType(name, DEFAULT_CONTENT_TYPE);
         Utils.putFile(getHttpClient(), url,
               zipFile.getInputStream(entry), 
               contentType, this.locktoken);
         this.countWrittenFiles++;
      } else {
         countOmittedFiles++;
         log("Omitted: " + name + " (uptodate)", ifVerbose());
      }
   }
   
   private String asDavPath(String path) {
      return path.replace('\\', '/');
   }
   
   public ZipFileSet createZipfileset() {
      ZipFileSet fileSet =  new ZipFileSet();
      this.zipfilesets.add(fileSet);
      return fileSet;
   }
   
   public static class ZipFileSet extends AbstractFileSet {
      private File src = null;
      
      public void setSrc(File src) {
         this.src = src;
      }
      
      File getSrc() {
         if (this.src != null) {
            return this.src;
         } else {
            throw new BuildException("ZipFileSet requires a src attribute!");
         }
      }

      /* 
       * @see org.apache.tools.ant.types.AbstractFileSet#getDirectoryScanner(org.apache.tools.ant.Project)
       */
      public DirectoryScanner getDirectoryScanner(Project p)
      {
         ZipScanner zs = new ZipScanner();
         zs.setSrc(getSrc());
         super.setDir(p.getBaseDir());
         setupDirectoryScanner(zs, p);
         zs.init();
         return zs;
      }
   }
}
