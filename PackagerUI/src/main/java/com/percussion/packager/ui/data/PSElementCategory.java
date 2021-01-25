/******************************************************************************
 *
 * [ PSElementCategory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui.data;

import org.apache.commons.lang.StringUtils;

/**
 * Simple data class to represent element categories.
 */
public class PSElementCategory
{
   
   
   /**
    * @param id the category id, cannot be <code>null</code> or empty.
    * @param name the category name, cannot be <code>null</code> or empty.
    * @param isCustom flag indicating a custom category.
    */
   public PSElementCategory(String id, String name, boolean isCustom)
   {
      setId(id);
      setName(name);
      m_isCustom = isCustom;
   }
   /**
    * @return the id
    */
   public String getId()
   {
      return m_id;
   }
   /**
    * @param id the id to set cannot be <code>null</code>
    * or empty.
    */
   public void setId(String id)
   {
      if(StringUtils.isBlank(id))
         throw new IllegalArgumentException("id cannot be null.");
      m_id = id;
   }
   /**
    * @return the name
    */
   public String getName()
   {
      return m_name;
   }
   /**
    * @param name the name to set, cannot be <code>null</code>
    * or empty.
    */
   public void setName(String name)
   {
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null.");
      m_name = name;
   }
   /**
    * @return the isCustom
    */
   public boolean isCustom()
   {
      return m_isCustom;
   }
   /**
    * @param isCustom the isCustom to set
    */
   public void setCustom(boolean isCustom)
   {
      m_isCustom = isCustom;
   }
   private String m_id;
   private String m_name;
   private boolean m_isCustom;
   
}
