/*[ CatalogEntry.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import javax.swing.*;


/**
 * This is a simple entry that doesn't support a display name or data object.
 *
 * @see ICatalogEntry
 */
public class CatalogEntry implements ICatalogEntry
{
   public String getDisplayName( )
   {
    return getInternalName( );
   }

   public void setDisplayName( String strName )
   {
      throw new UnsupportedOperationException( );
   }

   public String getInternalName( )
   {
      return m_strInternalName;
   }

   public void setInternalName( String strName )
   {
      m_strInternalName = strName;
   }

   public Object getData( )
   {
      return null;
   }

   public void setData( Object Data )
   {
      throw new UnsupportedOperationException( );
   }

  public int getType( )
   {
      return m_iType;
   }

   public void setType( int Type )
   {
    m_iType = Type;
   }


   public boolean hasDisplayNameDifferentThanInternalName( )
   {
      return false;
   }

  public String toString()
  {
    return getInternalName();
  }

   public ImageIcon getIcon()
   {
      return m_icon;
   }

   public void setIcon(ImageIcon icon)
   {
      m_icon = icon;
   }

  protected String m_strInternalName = "";
  protected int m_iType = -1;
   protected ImageIcon m_icon = null;

}

