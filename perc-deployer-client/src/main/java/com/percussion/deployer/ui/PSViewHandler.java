/******************************************************************************
 *
 * [ PSViewHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.error.PSDeployException;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * The abstract base class to implement the common or minimal functionality of
 * view handler.
 */
public abstract class PSViewHandler  implements IPSViewHandler
{
   /**
    * The default constructor.
    */
   public PSViewHandler()
   {
   }

   //implements the interface method
   public TableModel getTableModel()
   {
      if(m_object == null)
         throw new IllegalStateException("The data object is not set.");

      return null;
   }

   // implements the interface method
   public void setData(Object object) throws PSDeployException
   {
      if (!supportsObject(object))
         throw new IllegalArgumentException(
               "supplied object is not a supported type");

      m_object = object;
      // store the selected server
      if (m_object instanceof PSDeploymentServer)
      {
         PSMainFrame.putDeployProperty(PSMainFrame.LAST_SELECTED_SERVER,
               m_object);
      }
      else
      {
         removeSelectedData();
      }
   }
   
   //implements the interface method
   public void removeSelectedData()
   {
      PSMainFrame.removeDeployProperty(PSMainFrame.LAST_SELECTED_SERVER);
   }


   //implements interface method
   public Object getData()
   {
      if(m_object == null)
         throw new IllegalStateException("The data object is not set.");

      return m_object;
   }

   /**
    * Always returns <code>false</code>, the derived classes which supports
    * popup menu for the table model(if it has), should return <code>true</code>
    *
    * @return <code>false</code>
    */
   public boolean hasPopupMenu()
   {
      return false;
   }

   /**
    * Always returns <code>null</code>. Derived classes should implement this if
    * it supports popup menu.
    *
    * @param row the row index, ignored.
    *
    * @return <code>null</code>
    * @throws IllegalStateException if the data object is not yet set.
    */
   public JPopupMenu getPopupMenu(int row)
   {
      if(m_object == null)
         throw new IllegalStateException("The data object is not set.");

      return null;
   }

   //implements the interface method
   public boolean supportsDetailView()
   {
      return false;
   }

   //implements the interface method
   public void showDetailView(int row)
   {
      throw new UnsupportedOperationException(
         "Method, showDetailView(), is not supported");
   }

   /**
    * The current data object this view handler need to represent. Initialized
    * to <code>null</code> and is modified
    */
   protected Object m_object = null;
}
