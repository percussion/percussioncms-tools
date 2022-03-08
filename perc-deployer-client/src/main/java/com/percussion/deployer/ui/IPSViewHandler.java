/******************************************************************************
 *
 * [ IPSViewHandler.java ]
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
 * The interface that defines a view handler to represent a view in the view
 * panel.
 */
public interface IPSViewHandler
{
   /**
    * Checks whether the object is supported by the view handler.
    *
    * @param object the object to check, may be <code>null</code>
    *
    * @return <code>true</code> if the object is supported, otherwise
    * <code>false</code>
    */
   public boolean supportsObject(Object object);

   /**
    * Sets the data object that this view handler should represent. Please call
    * {@link #supportsObject(Object) supportsObject} to check whether this view
    * supports the supplied object. Please see the link for the supported object
    * types. This method should be called before calling any <code>get</code>
    * methods on the handler.
    *
    * @param object the data object to set, may not be <code>null</code> and
    * must be a valid object.
    *
    * @throws IllegalArgumentException if the object is not supported by the
    * view.
    * @throws PSDeployException if extracting required data from the object
    * fails.
    */
   public void setData(Object object) throws PSDeployException;

   /**
    * Removes the the reference to the selected server.
    */
   public void removeSelectedData();
   
   /**
    * Gets the data object set on this view handler.
    *
    * @return the data object, never <code>null</code>
    *
    * @throws IllegalStateException if the data object is not yet set.
    */
   public Object getData();

   /**
    * Gets the view label that needs to be displayed in status bar of the view
    * panel.
    *
    * @return the view label to show, never <code>null</code> may be empty.
    *
    * @throws IllegalStateException if the data object is not yet set.
    */
   public String getViewLabel();

   /**
    * Gets the table model that displays the details of the particular view.
    *
    * @return the table model, may be <code>null</code> if it does not have
    * anything to display in tabular format.
    *
    * @throws IllegalStateException if the data object is not yet set.
    */
   public TableModel getTableModel();

   /**
    * Checks whether the table represented by this view has popup menu or not.
    * If the view does not represent a table view, this should return <code>
    * false</code>
    *
    * @return <code>true</code> if it has, otherwise <code>false</code>
    */
   public boolean hasPopupMenu();

   /**
    * Gets the popup menu that has to be displayed on a pop-up trigger on the
    * selected row of the table this view represents. This should handle any
    * dynamic enabling or disabling of the pop-up menu based on the selected row
    * in the table. Can be saved to use in the pop-up menu action if it
    * requires.
    *
    * @param row the row index of the model representing the selected row in the
    * model, must be >= 0 and less than total rows of the table.
    *
    * @return the popup menu, may be <code>null</code> if the {@link
    * #hasPopupMenu() } returns <code>false</code>
    *
    * @throws IllegalArgumentException if row index is not valid.
    * @throws IllegalStateException if the data object is not yet set.
    */
   public JPopupMenu getPopupMenu(int row);

   /**
    * Specifies whether detail view is supported or not.
    * If the view is not supported, this should return <code>
    * false</code>
    *
    * @return <code>true</code> if it does, otherwise <code>false</code>
    */
   public boolean supportsDetailView();

   /**
    * Displays detail view based on the index supplied. It should not be called
    * if <code>supportsDetailView()</code> returns <code>false</code>.
    *
    * @param index indicates the index for which the detail is to be
    * displayed. It should be a valid row number for this view.
    *
    * @throws UnsupportedOperationException if <code>supportsDetailView()</code>
    * returns <code>false</code>.
    */
   public void showDetailView(int index);
}
