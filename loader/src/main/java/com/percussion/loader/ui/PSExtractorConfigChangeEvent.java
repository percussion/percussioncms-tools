/******************************************************************************
 *
 * [ PSExtractorConfigChangeEvent.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader.ui;

import java.util.EventObject;

/**
 * The event object passed around to extractor configuration change listeners.
 */
public class PSExtractorConfigChangeEvent extends EventObject
{
   /**
    * Constructs a new content loader configuration change event.
    *
    * @param type the value type for this event.
    *
    * @param source the source objects causeing this event, never
    *    <code>null</code>.
    */
   public PSExtractorConfigChangeEvent(int type, Object source)
   {
      super(source);
      m_type = type;
   }

   /**
    * Returns the value type of the field that triggered this
    * event. Either VALUE_TYPE_COMMUNITY or VALUE_TYPE_CONTENTTYPE
    *
    * @return the value type for this event.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * The value type of the field that triggered this event
    */
   private int m_type;

   /**
    * Community field value type
    */
   public static final int VALUE_TYPE_COMMUNITY = 1;

   /**
    * Content type field value type
    */
   public static final int VALUE_TYPE_CONTENTTYPE = 2;
}
