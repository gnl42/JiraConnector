/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/UnsubscribeMethod.java,v 1.3 2004/07/28 09:30:37 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:30:37 $
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
package org.apache.webdav.lib.methods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;


/**
 * Implements the UNSUBSCRIBE method.
 * 
 * @see <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/e2k3/e2k3/_webdav_unsubscribe.asp">Reference</a>
 */
public class UnsubscribeMethod  extends XMLResponseMethodBase 
{
   private static final String HEADER_SUBSCRIPTION_ID = "Subscription-Id";
   
   private List subscriptionIds = new ArrayList();
   
   public UnsubscribeMethod() {
   }
   
   public UnsubscribeMethod(String path) {
      super(path);
   }
   
   /**
    * Adds an ID for a subscription that is to be withdrawn.
    */
   public void addSubscriptionId(int id) {
      this.subscriptionIds.add(new Integer(id));
   }

   // --------------------------------------------------- WebdavMethod Methods

   public String getName()
   {
      return "UNSUBSCRIBE";
   }
   
   public void recycle()
   {
      super.recycle();
      this.subscriptionIds.clear();
   }
   
   protected void addRequestHeaders(HttpState state, HttpConnection conn)
      throws IOException, HttpException
   {
      super.addRequestHeaders(state, conn);
      if (this.subscriptionIds.size() > 0) {
         StringBuffer b = new StringBuffer();
         boolean first = true;
         for (Iterator i = this.subscriptionIds.iterator(); i.hasNext();) {
            if (first) first = false; else b.append(", ");
            b.append(i.next());
         }
         super.addRequestHeader(HEADER_SUBSCRIPTION_ID, b.toString());
      }
   }

   /**
    * Adds special checking of header values of the UNSUBSCRIBE method to
    * the super class implementation.
    */
   public void setRequestHeader(String headerName, String headerValue) 
   {
      if (headerName.equalsIgnoreCase(HEADER_SUBSCRIPTION_ID)){
         StringTokenizer t = new StringTokenizer(headerValue, ", ");
         try {
            for(;t.hasMoreTokens();) {
               addSubscriptionId(Integer.parseInt(t.nextToken()));
            }
         } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid header value '" +
                  headerValue + "' for header " + headerName + "!");
         }
      } else {
         super.setRequestHeader(headerName, headerValue);
      }
   }
}
