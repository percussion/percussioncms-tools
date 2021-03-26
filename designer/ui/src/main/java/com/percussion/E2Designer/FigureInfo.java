/*[ FigureInfo.java ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.io.Serializable;

/**
 * This class contains all of the information needed to create a new figure.
 * It is a simple wrapper around a collection of data so it can be stored in 
 * a list.
 */
class FigureInfo  implements Serializable
{
   /**
    * Creates a new object using the supplied params. The following constraints
    * must be met or an exception will be thrown.
    *
    * @param strName the name of the figure. Must not be null or empty. This 
    * name should be the internal name of the figure. To get the user viewable
    * name, pass this name as the key to the E2DesignerResources.
    *
    * @param strCategory the name of the category that this figure belongs to.
    * If null or empty, the category will default to the name of the figure.
    * This is the internal name.
    *
    * @param iId a unique identifier. All figures in the list should have a
    * different id. These ids are used to determine if 2 figures can connect
    * or not.
    *
    * @throws IllegalArgumentException if the above constraints aren't meant
    */
   public FigureInfo(String strName, String strCategory, int iId )
   {
      if (null == strName || 0 == strName.trim().length())
         throw new IllegalArgumentException();
      else
         m_strName = strName;
   
      if (null == strCategory || 0 == strCategory.trim().length())
         m_strCategory = m_strName;
      else
         m_strCategory = strCategory;

      m_iId = iId;
   }

   public String getCategory()
   {
      return(m_strCategory);
   }

   public String getName()
   {
      return(m_strName);
   }

   public int getId()
   {
      return(m_iId);
      
   }

   // private storage
   private String m_strCategory = null;
   private String m_strName = null;
   private int m_iId = 0;
}
