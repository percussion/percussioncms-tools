/*[ PSProgressEvent.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import java.util.EventObject;

/**
 * A progress event, fired upon the progress changes of a process
 */
public class PSProgressEvent extends EventObject
{
   /**
    * Constructs a <code>PSProgressEvent</code> object from a given progress
    * percentage, without message.
    * 
    * @param source The object which fired the event, may not <code>null</code>.
    * @param percent The current progress percentage.
    */
   public PSProgressEvent(Object source, int percent)
   {
      super(source);

      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      m_percent = percent;
   }
   
   /**
    * Constructs a <code>PSStateEvent</code> object from a given progress
    * percentage and a message.
    * 
    * @param source The object which fired the event, may not <code>null</code>.
    * @param counter The progress counter.
    * @param resourceId The resource id of the event, it may be 
    *    <code>null</code> or empty.
    */
   public PSProgressEvent(Object source, int counter, String resourceId)
   {
      super(source);
      
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      m_counter = counter;
      m_resourceId = resourceId;
   }
   
   /**
    * Get the progress percentage.
    * 
    * @return The progress percentage, <code>-1</code> if there is no 
    * percentage info.
    */
   public int getPercent()
   {
      return m_percent;
   }
   
   /**
    * Get the progress counter.
    * 
    * @return The progress counter, <code>-1</code> if there is no counter info.
    */
   public int getCounter()
   {
      return m_counter;
   }
   
   /**
    * Get the progress resource id.
    * 
    * @return The resource id, it may be <code>null</code> or empty if there is
    * no resource id info for this event.
    */
   public String getResourceId()
   {
      return m_resourceId;
   }

   /**
    * The progress percentage, <code>-1</code> when there is no percentage info
    */
   private int m_percent = -1;

   /**
    * The progress counter. <code>-1</code> when there is no counter info
    */
   private int m_counter = -1;
   
   /**
    * The unique identifier of the item that is currently processed. 
    * For example, it may be a full path of a URL or file. It may be 
    * <code>null</code> or empty if it is not available.
    */
   private String m_resourceId = null;
   
}
