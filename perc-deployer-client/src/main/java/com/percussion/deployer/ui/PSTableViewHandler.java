/******************************************************************************
 *
 * [ PSTableViewHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.catalog.PSCatalogResultSet;

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * The abstract view handler that supports the table view and sorting of the 
 * table data. The derived classes should implement the creation of pop-up menu
 * and its display. 
 */
public abstract class PSTableViewHandler extends PSServerViewHandler
{
   /**
    * Initializes the handler with table and table model and pop-up menu to show
    * in this view.
    */
   public PSTableViewHandler()
   {
      init();
   }
   
   /**
    * Initializes the table with sorter model and adds listeners. Calls 
    * <code>createPopupMenu</code> to set pop-up menu on the table.
    */
   public void init()
   { 
      //Creates the popup menu that needs to be displayed 
      createPopupMenu();
   }
   
   /**
    * Creates the table model from the supplied catalog resultset or updates the
    * data if it is already created.
    * 
    * @param dataSet the data of the table model, may not be <code>null</code>
    * and must have atleast a single column.
    * 
    * @throws IllegalArgumentException if <code>dataSet</code> is not valid.
    */
   public void setTableModel(PSCatalogResultSet dataSet)
   {
      if(dataSet == null)
         throw new IllegalArgumentException("dataSet may not be null");
      
      if(dataSet.getColumns() == null)
         throw new IllegalArgumentException(
            "dataSet must have columns");  
            
      if(m_tableModel == null)
         m_tableModel = new PSCatalogTableModel(dataSet);
      else
         m_tableModel.setTableData(dataSet);
         
      m_curSelectedRow = -1;
   }
   
   // implements interface method
   public TableModel getTableModel()
   {
      if(m_object == null)
         throw new IllegalStateException("The data object is not set.");
         
      return m_tableModel;
   }
   
   //implements interface method
   public boolean hasPopupMenu()
   {
      return true;
   }
   
   //implements interface method.
   public JPopupMenu getPopupMenu(int row)
   {
      int rowcount = m_tableModel.getRowCount();
      if(row < 0 || row >= rowcount)
         throw new IllegalArgumentException("row must be between 0 and " + 
            (rowcount-1) + "inclusive");   
            
      m_curSelectedRow = row;            
      updatePopupMenu();
      return m_popupMenu;
   }

   /**
    * Creates the pop-up menu items and corresponding actions for the pop-up 
    * menu that needs to be displayed for this table. Derived classes should 
    * implement this according its view.
    */
   protected abstract void createPopupMenu();
   
   /**
    * Enables or disables the pop-up menu items based on the current selected
    * row for which the pop-up menu is shown. See {@link #m_curSelectedRow} for
    * more description on current selected row.     
    * 
    * @throws IllegalStateException if current selected row is <code>-1</code>
    */
   protected abstract void updatePopupMenu();
   
   /**
    * The table model that represents the table structure and data, set in 
    * <code>setTableModel</code>.
    */
   protected PSCatalogTableModel m_tableModel = null;
  
   /**
    * The popup menu that needs to be shown when user right clicks on a row in
    * table, initialized in the <code>createPopupMenu</code> and never <code>
    * null</code> after that. The pop-up menu items(actions) will be added in
    * <code>createPopupMenu()</code>.
    */
   protected JPopupMenu m_popupMenu = new JPopupMenu();
   
   /**
    * The row index that represents the row index of the data model that 
    * represents the selected row in this view table, initialized to <code>-1
    * </code> and gets updated by a call to <code>getPopupMenu(int)</code>. 
    * Reset to <code>-1</code> whenever the model data has changed.
    */
   protected int m_curSelectedRow = -1;
}
