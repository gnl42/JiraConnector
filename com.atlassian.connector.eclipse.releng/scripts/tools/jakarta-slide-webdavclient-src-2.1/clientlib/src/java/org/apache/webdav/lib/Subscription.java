// vi: set ts=3 sw=3:
package org.apache.webdav.lib;

/**
 * Object that holds information about a single WebDAV subscription.
 * 
 * @see org.apache.webdav.lib.WebdavResource#subscribeMethod(String, String, String, long, int, long)
 */
public class Subscription
{
   public static final String UPDATE_NOTIFICATION = "update";
   public static final String NEW_MEMBER_NOTIFICATION = "update/newmember";
   public static final String DELETE_NOTIFICATION = "delete";
   public static final String MOVE_NOTIFICATION = "move";
   
   private int id;
   private long lifetime;
   private String callback;
   private String contentLocation;
   private String notificationType;
   private String path;
   
   public Subscription(String path, int id, String callback, long lifetime, 
         String contentLocation, String notificationType)
   {
      this.path = path;
      this.id = id;
      this.callback = callback;
      this.lifetime = lifetime;
      this.contentLocation = contentLocation;
      this.notificationType = notificationType;
   }
   
   public String getCallback()
   {
      return callback;
   }
   public String getContentLocation()
   {
      return contentLocation;
   }
   public int getId()
   {
      return id;
   }
   public long getLifetime()
   {
      return lifetime;
   }
   public String getNotificationType()
   {
      return notificationType;
   }
   public String getPath() 
   {
      return path;
   }
}
