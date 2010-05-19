/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/PollMethod.java,v 1.3 2004/08/02 15:45:48 unico Exp $
 * $Revision: 1.3 $
 * $Date: 2004/08/02 15:45:48 $
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;

import org.apache.webdav.lib.util.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Implements the POLL WebDAV method.
 * 
 * @see <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/e2k3/e2k3/_webdav_poll.asp">Reference</a>
 */
public class PollMethod extends XMLResponseMethodBase
{
   private static final String HEADER_SUBSCRIPTION_ID = "Subscription-Id";
   private static final String EXCHANGE_NS = "http://schemas.microsoft.com/Exchange/";
   private List subscriptionIds = new ArrayList();
   private List subscriptionsWithEvents = new ArrayList();
   private List subscriptionsWithoutEvents = new ArrayList();
   
   
   public PollMethod() {
      
   }
   
   public PollMethod(String path) {
      super(path);
   }
   
   /**
    * Adds an ID for a subscription that is to be polled. All added subscription
    * IDs should have the got same Content-Location uri from the SUBSCRIBE method. 
    */
   public void addSubscriptionId(int id) {
      checkNotUsed();
      this.subscriptionIds.add(new Integer(id));
   }
   
   /**
    * Returns a list of number objects containing the subscription IDs for
    * subscriptions for which events are reported.
    * @return Collection of {@link Integer}s
    */
   public Collection getSubscriptionsWithEvents() {
      checkUsed();
      return this.subscriptionsWithEvents;
   }
   /**
    * Returns a list of number objects containing the subscription IDs for
    * subscriptions for which <em>NO</em> events are reported.
    * @return Collection of {@link Integer}s
    */
   public Collection getSubscriptionsWithoutEvents() {
      checkUsed();
      return this.subscriptionsWithoutEvents;
   }
   
   // --------------------------------------------------- WebdavMethod Methods

   public String getName()
   {
      return "POLL";
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
    * Adds special checking of header values of the POLL method to
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
   
   public void parseResponse(InputStream input, HttpState state,
         HttpConnection conn) throws IOException, HttpException
   {
      int status = getStatusLine().getStatusCode();
      
      if (status == HttpStatus.SC_MULTI_STATUS) {
         parseXMLResponse(input);
         NodeList list = getResponseDocument().getDocumentElement()
            .getElementsByTagNameNS("DAV:", "response");
         
         for(int i = 0; i < list.getLength(); i++) {
            Element e = (Element)list.item(i);
            
            NodeList s = e.getElementsByTagNameNS("DAV:", "status");
            if (s.getLength() > 0) {
               Element response = ((Element)(s.item(0)).getParentNode());
               String statusText = DOMUtils.getTextValue((Element)s.item(0));
               if (statusText.indexOf(" 200 ") != -1) {
                  // polled subscriptions for which *AN* event is fired
                  NodeList p = response.getElementsByTagNameNS(EXCHANGE_NS, "subscriptionID");
                  if (p.getLength()>0) {
                     NodeList li = ((Element)p.item(0)).getElementsByTagName("li");
                     for(int l = 0; l < li.getLength();++l) {
                        String id = DOMUtils.getTextValue(li.item(i));
                        this.subscriptionsWithEvents.add(Integer.getInteger(id));
                     }
                  }
               } else
               if (statusText.indexOf(" 204 ") != -1) {
                  // polled subscriptions for which *NO* event is fired
                  NodeList p = response.getElementsByTagNameNS(EXCHANGE_NS, "subscriptionID");
                  if (p.getLength()>0) {
                     NodeList li = ((Element)p.item(0)).getElementsByTagName("li");
                     for(int l = 0; l < li.getLength();++l) {
                        String id = DOMUtils.getTextValue(li.item(i));
                        this.subscriptionsWithoutEvents.add(Integer.getInteger(id));
                     }
                  }
               }
            }
         }
      }
   }
}
