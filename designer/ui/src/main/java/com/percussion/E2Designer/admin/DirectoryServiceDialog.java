/******************************************************************************
 *
 * [ DirectoryServiceDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.guitools.PSDialog;

import java.awt.*;

/**
 * An abstract dialog used to implement the base functionality common to all
 * directory service dialogs.
 */
public abstract class DirectoryServiceDialog extends PSDialog
{
   /**
    * Construct the dialog for the supplied parameters.
    * 
    * @param parent the parent frame, may be <code>null</code>.
    * @param data the current directory service data, not <code>null</code>.
    */
   protected DirectoryServiceDialog(Frame parent, DirectoryServiceData data)
   {
      super(parent);
      
      if (data == null)
         throw new IllegalArgumentException("data cannot be null");
      
      m_data = data;
   }
   
   /**
    * Get the directory service data added with this edit session so far.
    * 
    * @return a collection with all new added data during this edit session,
    *    never <code>null</code>, may be empty.
    */
   protected DirectoryServiceData getNewData()
   {
      return m_newData;
   }
   
   /**
    * Add the supplied directory service data collection to the local data
    * collection of all new data collected during this edit session.
    * 
    * @param data the new directory service data to be added, not 
    *    <code>null</code>.
    */
   protected void addNewData(DirectoryServiceData data)
   {
      m_newData.addAll(data);
   }
   
   /**
    * The directory service data known at construction time of this dialog, 
    * never <code>null</code> after construction.
    */
   protected DirectoryServiceData m_data = null;
   
   /**
    * A directory service data model used to collect all objects added within 
    * an edit session of this dialog. Initialized to an empty data collection
    * while constructed, never <code>null</code>.
    */
   protected DirectoryServiceData m_newData = new DirectoryServiceData();
}
