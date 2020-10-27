/*[ PSTreeObject.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.PSAction;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PSTreeObject implements Comparable
{

   public PSTreeObject(String name)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("name cannot be null");
      m_name = name;
   }

   public PSTreeObject(String name, JPanel pane)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("name cannot be null");
      m_name = name;
      m_pane = pane;
   }

   public void setDataObject(Object data)
   {
      m_data = data;
   }

   public Object getDataObject()
   {
      return m_data;
   }

   public JPanel getUIObject()
   {
      return m_pane;
   }

   public String toString()
   {
      return m_name;
   }

   public void setName(String name)
   {
      m_name = name;
   }

   public void addChildren(PSTreeObject obj)
   {
      m_list.add(obj);
   }

   public boolean hasChildren()
   {
      return m_list.size() > 0;
   }

   public List getChildren()
   {
      Collections.sort(m_list);
      return m_list;
   }

   public void setParent(PSTreeObject usrObj)
   {
      m_parent = usrObj;
   }

   public boolean hasParent()
   {
      return m_parent != null;
   }

   public PSTreeObject getParent()
   {
      return m_parent;
   }

   /**
    * Compare objects by name unless they are PSAction objects with
    * parents that are cascading or dynamic menus. If that is the case then we
    * sort by sortRank.
    * @param o object to compare against. Should not be <code>null</code>.
    * @return <code>int</code> specifiying comparison result. -1 is less than,
    * 0 indicates equality, and 1 is greater than.
    */
   public int compareTo(Object o)
   {
      if(null == o)
         throw new IllegalArgumentException("Comparison object can not be null");

      int result = m_name.compareToIgnoreCase(((PSTreeObject)o).m_name);
      PSTreeObject treeObj = (PSTreeObject)o;

      Object dataObj = treeObj.getDataObject();

      if(dataObj instanceof PSAction && null != m_parent &&
         m_parent.getDataObject() instanceof PSAction)
      {

        PSAction action = (PSAction)dataObj;
        PSAction myAction = (PSAction)m_data;
        PSAction parentAction = (PSAction)m_parent.getDataObject();
        if(parentAction.isCascadedMenu() || parentAction.isDynamicMenu())
        {
          result = 0;
          if(myAction.getSortRank() != action.getSortRank())
             result = myAction.getSortRank() > action.getSortRank() ? 1 : -1;
        }
      }
      return result;
   }

   private String m_name;
   private JPanel m_pane;
   private Object m_data;
   private PSTreeObject m_parent;
   private List m_list = new ArrayList();
}