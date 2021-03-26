/******************************************************************************
 *
 * [ CatalogEntryEx.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.browser;


/**
 * This is a full fledged implementation of the catalog entry interface. It
 * contains the information needed for a node in the browser tree.
 *
 * @see ICatalogEntry
 * @see CatalogEntry
 */
public class CatalogEntryEx extends CatalogEntry
{
   public String getDisplayName( )
   {
    if(m_bHasDifferentDisplayName)
      return m_strDisplayName;
    else
      return m_strInternalName;
   }

   public void setDisplayName( String strName )
   {
    if(strName != null)
    {
      m_bHasDifferentDisplayName = true;
      m_strDisplayName = new String(strName);
    }
   }

   public Object getData( )
   {
      return m_Data;
   }

   public void setData( Object Data )
   {
    m_Data = Data;
   }

   public boolean hasDisplayNameDifferentThanInternalName( )
   {
      return m_strDisplayName != null && m_bHasDifferentDisplayName;
   }

  public String toString()
  {
    return getDisplayName();
  }

  //variables
  private boolean m_bHasDifferentDisplayName = false;
  private String m_strDisplayName = null;
  private Object m_Data = null;

}

