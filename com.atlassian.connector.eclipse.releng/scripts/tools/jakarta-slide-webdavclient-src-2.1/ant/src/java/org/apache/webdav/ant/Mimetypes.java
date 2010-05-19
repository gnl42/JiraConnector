// vi: set ts=3 sw=3:
/* 
 * $Header: /home/cvs/jakarta-slide/webdavclient/ant/src/java/org/apache/webdav/ant/Mimetypes.java,v 1.3 2004/07/28 09:31:49 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:31:49 $
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
package org.apache.webdav.ant;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * Helper for mapping file extensions to mime-types.
 *  
 */
public class Mimetypes
{
   private static Map mimetypes = new HashMap(); 
   
   static {
      ResourceBundle rb = 
         ResourceBundle.getBundle("org.apache.webdav.ant.mimetypes");
      
      for(Enumeration e = rb.getKeys(); e.hasMoreElements(); ) {
         String ext = (String)e.nextElement();
         mimetypes.put(ext, rb.getString(ext));
      }
   }
   
   public static String getMimeTypeForExtension(String extension, String defaultType) {
      String mime = (String)mimetypes.get(extension);
      if (mime == null) mime = defaultType;
      return mime;
   }
   
   public static String getMimeType(File file, String defaultType) {
      String ext = file.getName();
      int dotPos = ext.lastIndexOf('.');
      if (dotPos != -1) {
         return getMimeTypeForExtension(ext.substring(dotPos + 1), defaultType);
      } else {
         return defaultType;
      }
   }
   public static String getMimeType(String file, String defaultType) {
      int dotPos = file.lastIndexOf('.');
      if (dotPos != -1) {
         return getMimeTypeForExtension(file.substring(dotPos + 1), defaultType);
      } else {
         return defaultType;
      }
   }
   
   public static String getExtension(String fileName) {
      int dotPos = fileName.lastIndexOf('.');
      if (dotPos != -1) {
         return fileName.substring(dotPos + 1);
      } else {
         return null;
      }
   }
}
