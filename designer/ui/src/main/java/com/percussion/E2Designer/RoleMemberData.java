/******************************************************************************
 *
 * [ RoleMemberData.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.EditableListBox.EditableListBoxCellNameHelper;
import com.percussion.EditableListBox.ICellImageHelper;
import com.percussion.design.objectstore.PSAclEntry;

import javax.swing.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class RoleMemberData implements ICellImageHelper, 
                                       EditableListBoxCellNameHelper
{
   /** Here for subclassing */
   public RoleMemberData()
   {}

   public RoleMemberData(PSAclEntry entry)
   {
         m_strName = entry.getName();
      if(entry.isUser())
         m_strMemberType   = getResources().getString("user");
      else if(entry.isGroup())
         m_strMemberType = getResources().getString("group");
      else
         m_strMemberType = getResources().getString("role");   
   }

   public String getName()
   {
      return m_strName;
   }
   
   public void setName(String name)
   {
      m_strName = name;
   }


   public String getMemberType()
   {
      return m_strMemberType;
   }
   
   public void setMemberType(String type)
   {
      m_strMemberType = type;
   }


   public String toString()
   {
      return m_strName;
   }

   public boolean isUser()
   {
      return getMemberType().equals(getResources().getString("user"));
   }

   public boolean isGroup()
   {
      return getMemberType().equals(getResources().getString("group"));
   }

   public boolean isRole()
   {
      return getMemberType().equals(getResources().getString("role"));
                     
   }
   
   public boolean isFilter()
   {
      return getMemberType().equals(getResources().getString("filter"));
   }

   /**
    * @returns the image to be displayed in the cell. Uses the member type and
    * the Sec provider type to determine the image.
    */
   public ImageIcon getImage()
   {
      if (isUser())
      {
         if (m_imageUserAny == null)
         {
            m_imageUserAny = new ImageIcon(getClass().getResource(
               getResources().getString("gif_user_any")));
         }
         return m_imageUserAny;
      }
      else if (isRole() || isGroup())
      {
         if (m_imageGroupAny == null)
         {
            m_imageGroupAny = new ImageIcon(getClass().getResource(
               getResources().getString("gif_group_any")));
         }
         return m_imageGroupAny;
      }
      else
      {
         return null;
      }
   }

/** @param image The image to be set in. 
*/
  public void setImage(ImageIcon image)
   {
      ;
   }

   public void copyFrom(RoleMemberData data)
   {
      m_strName = data.getName();
      m_strMemberType = data.getMemberType();

   }

  public ResourceBundle getResources()
  {
      try
    {
      if (m_res == null)
        m_res = ResourceBundle.getBundle( "com.percussion.E2Designer.RoleMemberPropertyDialogResources", Locale.getDefault() );
    }
    catch (MissingResourceException e)
      {
         System.out.println(e);
      }

       return m_res;
    }

   protected ImageIcon m_imageUserAny = null;
   protected ImageIcon m_imageGroupAny = null;

   private ResourceBundle m_res = null;
   private String m_strName = "";
   private String m_strMemberType = "";
}
