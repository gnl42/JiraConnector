/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/clientlib/src/java/org/apache/webdav/lib/methods/SubscribeMethod.java,v 1.3 2004/07/28 09:30:37 ib Exp $
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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;

/**
 * Implements the SUBSCRIBE method.
 * 
 * @see <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/e2k3/e2k3/_webdav_subscribe.asp">Reference</a>
 */
public class SubscribeMethod extends XMLResponseMethodBase 
   implements DepthSupport
{

   private static final String HEADER_SUBSCRIPTION_ID = "Subscription-Id";
   private static final String HEADER_SUBSCRIPTION_LIFETIME = "Subscription-Lifetime";
   private static final String HEADER_NOTIFICATION_TYPE = "Notification-Type";
   private static final String HEADER_NOTIFICATION_DELAY = "Notification-Delay";
   private static final String HEADER_DEPTH = "Depth";
   private static final String HEADER_CALL_BACK = "Call-Back";
   private static final String HEADER_CONTENT_LOCATION = "Content-Location";
   
   public static final String TYPE_UPDATE = "update";
   public static final String TYPE_UPDATE_NEW_MEMBER = "update/newmember";
   public static final String TYPE_DELETE = "delete";
   public static final String TYPE_MOVE = "move";
   
   private String callback = null;
   private String notificationType = null;
   private int depth = -1;
   private long subsciptionLifetime = -1;
   private int subscriptionId = -1;
   private long notificationDelay = -1;
   
   private long responsedSubscriptionLifetime = -1;
   private int responsedSubscriptionId = -1;
   private String responsedContentLocation = null;
   
   public SubscribeMethod() {
      
   }
   
   public SubscribeMethod(String path) {
      super(path);
   }

   public String getCallback()
   {
      return callback;
   }
   /**
    * Sets the URI that's to be notified if the subscribed event does occur.  
    */
   public void setCallback(String callback)
   {
      if (callback != null && callback.length() > 0) {
         this.callback = callback;
      }
   }
   public String getNotificationType()
   {
      return notificationType;
   }
   /**
    * Sets the notification type, i.e. determines the events that are 
    * subscribed.
    * @see #TYPE_DELETE 
    * @see #TYPE_MOVE
    * @see #TYPE_UPDATE
    * @see #TYPE_UPDATE_NEW_MEMBER
    */
   public void setNotificationType(String notificationType)
   {
      this.notificationType = notificationType;
   }
   public long getSubsciptionLifetime()
   {
      return subsciptionLifetime;
   }
   /**
    * Sets the duration of the subscription in seconds.
    */
   public void setSubsciptionLifetime(long subsciptionLifetime)
   {
      this.subsciptionLifetime = subsciptionLifetime;
   }
   public long getSubscriptionId()
   {
      return subscriptionId;
   }
   /**
    * Sets the ID of a subscription to be refreshed.
    * @param subscriptionId
    */
   public void setSubscriptionId(int subscriptionId)
   {
      this.subscriptionId = subscriptionId;
   }
   /**
    * Sets the notification delay in seconds.
    */
   public void setNotificationDelay(long delay) {
      this.notificationDelay = delay;
   }
   public long getNotificationDelay() {
      return this.notificationDelay;
   }
   public int getDepth()
   {
      return this.depth;
   }
   /**
    * Sets the depth.
    */
   public void setDepth(int depth)
   {
      switch(depth) {
         case DEPTH_0:
         case DEPTH_1:
         case DEPTH_INFINITY:
            this.depth = depth;
            break;
         default:
            throw new IllegalArgumentException(
                  "Depth must be 0, 1 or "+DEPTH_INFINITY+".");
      }
   }
   
   /**
    * Returns the subscription ID responsed from the server.
    * @return -1 if no subscription id was in the response 
    */
   public int getResponsedSubscriptionId() {
      checkUsed();
      return this.responsedSubscriptionId;
   }
   /**
    * Returns the subscription lifetime responsed from the server.
    * @return -1 if no subscription lifetime was given in the response
    */
   public long getResponsedSubscriptionLifetime() {
      checkUsed();
      return this.responsedSubscriptionLifetime;
   }
   /**
    * Returns the value of the content-location header of the response. 
    * This shall be used to the request uri for a POLL method querying this
    * subscription.
    */
   public String getResponsedContentLocation() {
      checkUsed();
      return this.responsedContentLocation;
   }
   // --------------------------------------------------- WebdavMethod Methods

   public String getName()
   {
      return "SUBSCRIBE";
   }
   
   public void recycle()
   {
      super.recycle();
      this.callback = null;
      this.depth = -1;
      this.notificationDelay = -1;
      this.notificationType = null;
      this.responsedSubscriptionId = -1;
      this.responsedSubscriptionLifetime = -1;
      this.subsciptionLifetime = -1;
      this.subscriptionId = -1;
   }
   protected void addRequestHeaders(HttpState state, HttpConnection conn)
         throws IOException, HttpException
   {
      super.addRequestHeaders(state, conn);
      
      if (this.callback != null) {
         super.setRequestHeader(HEADER_CALL_BACK, this.callback);
      }
      if (this.depth > -1) {
         super.setRequestHeader(HEADER_DEPTH, 
               this.depth == DEPTH_INFINITY ? "infinity" 
                     : String.valueOf(this.depth));
      }
      if (this.notificationType != null) {
         super.setRequestHeader(HEADER_NOTIFICATION_TYPE, this.notificationType);
      }
      if (this.subsciptionLifetime > 0) {
         super.setRequestHeader(HEADER_SUBSCRIPTION_LIFETIME, 
               Long.toString(this.subsciptionLifetime));
      }
      if (this.subscriptionId > 0) {
         super.setRequestHeader(HEADER_SUBSCRIPTION_ID, Long.toString(
               this.subscriptionId));
      }
      if (this.notificationDelay > 0) {
         super.setRequestHeader(HEADER_NOTIFICATION_DELAY, Long.toString(
               this.notificationDelay));
      }
   }
   
   /**
    * Adds special checking of header values of the SUBSCRIBE method to
    * the super class implementation.
    */
   public void setRequestHeader(String headerName, String headerValue) 
   {
       try {
         if (headerName.equalsIgnoreCase(HEADER_DEPTH)){
            if ("infinity".equalsIgnoreCase(headerValue)) {
               setDepth(DEPTH_INFINITY);
            } else {
               setDepth(Integer.parseInt(headerValue));
            }
          } 
          else if(headerName.equals(HEADER_SUBSCRIPTION_ID)) {
             setSubscriptionId(Integer.parseInt(headerValue));
          }
          else if(headerName.equals(HEADER_SUBSCRIPTION_LIFETIME)) {
             setSubscriptionId(Integer.parseInt(headerValue));
          }
          else if(headerName.equals(HEADER_NOTIFICATION_DELAY)) {
             setNotificationDelay(Long.parseLong(headerValue));
          }
          else {
             super.setRequestHeader(headerName, headerValue);
          }
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Invalid header value '" +
               headerValue + "' for header " + headerName + "!");
      }
   }
   
   protected void processResponseHeaders(HttpState state, HttpConnection conn)
   {
      super.processResponseHeaders(state, conn);
      
      Header header; 
      
      header = getResponseHeader(HEADER_SUBSCRIPTION_ID);
      if (header != null) {
         this.responsedSubscriptionId = Integer.parseInt(header.getValue());
      }
      
      header = getResponseHeader(HEADER_SUBSCRIPTION_LIFETIME);
      if (header != null) {
         this.responsedSubscriptionLifetime = Long.parseLong(header.getValue());
      }
      
      header = getResponseHeader(HEADER_CONTENT_LOCATION);
      if (header != null) {
         this.responsedContentLocation = header.getValue();
      }
   }
}
