// vi: set ts=3 sw=3:
/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/taskdefs/Get.java,v 1.3.2.1 2004/08/15 13:01:15 luetzkendorf Exp $
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpURL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FilterSet;
import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.util.LineTokenizer;
import org.apache.webdav.ant.CollectionScanner;
import org.apache.webdav.ant.Utils;
import org.apache.webdav.ant.WebdavFileSet;

/**
 * WebDAV task for retrieving resources.
 * 
 * @see <a href="../doc-files/tasks.htm#davget">Task documentation</a>
 */
public class Get extends WebdavMatchingTask {
   private File toDir = null;
   private File toFile = null;
   private boolean overwrite = false; 
   private String encoding = null;
   private FilterSetCollection filterSets = new FilterSetCollection();
   
   private int countWrittenFiles = 0;
   private int countOmittedFiles = 0;
   
   /* 
    * @see org.apache.tools.ant.Task#execute()
    */
   public void execute() throws BuildException {
      validate();
      
      try {
         if (this.toFile != null) {
            downloadSingleFile();
         } else {
            log("Downloading from: " + getUrl(), ifVerbose());
            // URL must be a collection
            if (!getUrl().getPath().endsWith("/")) {
               getUrl().setPath(getUrl().getPath() + "/");
            }
            for(Iterator i = getFileSets(); i.hasNext(); ) {
               downloadFileset((WebdavFileSet)i.next());
            }
         }
         
         if (this.countWrittenFiles > 0) {
            log("Downloaded " + this.countWrittenFiles 
                  + (this.countWrittenFiles == 1 ? " resource" : " resources")
                  + " from " + getUrl(),
                  this.countWrittenFiles > 0 
                     ? Project.MSG_INFO
                     : ifVerbose());
         }
      } catch (IOException e) {
         throw Utils.makeBuildException("Error while downloading!", e);
      }
   }
   
   protected void validate() {
      super.validate();
      
      if (this.toDir == null && this.toFile == null) {
         throw new BuildException("Missing one of the required attributes toDir" +
               " or toFile.");
      }
      if (this.toDir != null && this.toFile != null) {
         throw new BuildException("Only one of the attributes toDir and toFile" +
               " is alowed.");
      }
      if (this.toFile != null && getFileSets().hasNext()) {
         throw new BuildException("Not filesets allowed if toFile is set.");
      }
      if (this.encoding == null && this.filterSets.hasFilters()) {
         log("If filterSets are used a file encoding should be specified!", 
               Project.MSG_WARN);
         this.encoding = "ISO-8859-1"; // TODO what should be the default
      }
   }
   
   protected void downloadFileset(WebdavFileSet fileSet) throws IOException 
   {
      CollectionScanner scanner = 
          fileSet.getCollectionScanner(getProject(), getHttpClient(), getUrl());
      HttpURL baseUrl = scanner.getBaseURL();
      
      String[] files = scanner.getIncludedFiles();
      for (int i = 0; i < files.length; i++) {
         HttpURL url = Utils.createHttpURL(baseUrl, files[i]);
         downloadFile(url, files[i], scanner);
      }
   }
   
   protected void downloadSingleFile()
      throws IOException 
   {
      long lastMod = Utils.getLastModified(getHttpClient(), getUrl());
      
      if (shallWeGetIt(this.toFile, lastMod)) {
         getAndStoreResource(getUrl(), this.toFile, lastMod, getUrl().getURI());
      } else {
         log("Omitted: " + getUrl() + " (uptodate)", ifVerbose());
         this.countOmittedFiles++;
      }
   }
   
   protected void downloadFile(HttpURL url, 
                               String relativePath, 
                               CollectionScanner scanner) 
      throws IOException
   {
      File target = new File(this.toDir, relativePath);
      
      long lastMod = scanner.getProperties().getLastModified(url.toString());
      
      if (shallWeGetIt(target, lastMod)) {
         getAndStoreResource(url, target, lastMod, relativePath);
      } else {
         log("Omitted: " + relativePath + " (uptodate)", ifVerbose());
         this.countOmittedFiles++;
      }
   }
   
   private boolean shallWeGetIt(File target, long lastMod) {
      boolean getit = this.overwrite || !target.exists();
      if (!this.overwrite && target.exists()) {
         if (lastMod > target.lastModified()) {
            getit = true;
         }
      }
      return getit;
   }

   /**
    * Retrieves the data of a resource and stores it in a file.
    * 
    * Creates required directories and sets the last modified time of the file.
    * 
    * @param url url of the resource to be retrieved
    * @param target file where the resource data ist stored
    * @param lastMod last modified date of the resource, used to set 
    *        the last modified date of the target file
    * @param relative path og the resource for logging purposes.
    * @throws IOException
    * @throws HttpException
    * @throws FileNotFoundException
    */
   private void getAndStoreResource(HttpURL url, File target, long lastMod, String relative) 
      throws IOException, HttpException, FileNotFoundException 
   {
      log("downloading: " + relative, ifVerbose());
      File directory = target.getParentFile();
      if (!directory.exists()) {
         directory.mkdirs();
      }
      
      InputStream in = Utils.getFile(getHttpClient(), url);
      
      if (!target.exists()) {
         target.createNewFile();
      }
      FileOutputStream out = new FileOutputStream(target);
      
      copyStream(in, out, this.filterSets, this.encoding);
      
      out.close();
      target.setLastModified(lastMod);
      this.countWrittenFiles++;
   }

   protected static void copyStream(InputStream in, OutputStream out,
         FilterSetCollection filterSets, String encoding) 
      throws IOException 
   {
      byte[] b = new byte[1024];
      if (filterSets.hasFilters()) {
         InputStreamReader reader = new InputStreamReader(in, encoding);
         OutputStreamWriter writer = new OutputStreamWriter(out, encoding);
         
         LineTokenizer tok = new LineTokenizer();
         tok.setIncludeDelims(true);

         for (String l = tok.getToken(reader); l != null; l = tok.getToken(reader)) {
            writer.write(filterSets.replaceTokens(l));
         }
         writer.close();
         reader.close();
      } else {
         while (in.available() > 0) {
            int cnt = in.read(b, 0, b.length);
            if (cnt > -1) {
               out.write(b, 0, cnt);
            }
         }
      }
   }


   public void setTodir(File directory) {
      this.toDir = directory;
      if (this.toDir.isFile()) {
         throw new BuildException("toDir must not point to a file!");
      }
   }
   
   public void setTofile(File file) {
      this.toFile = file;
      if (this.toFile.isDirectory()) {
         throw new BuildException("toFile must not point to a directory!");
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
   public void setOverwrite(boolean b) {
      this.overwrite = b;
   }
}
