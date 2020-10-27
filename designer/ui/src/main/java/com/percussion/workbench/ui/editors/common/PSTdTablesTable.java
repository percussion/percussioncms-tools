/******************************************************************************
 *
 * [ PSTdTablesTable.java ]
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.editors.common;

import com.percussion.workbench.ui.layouts.PSAutoResizeTableLayout;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The table for the Table definition schreens in the workbench.
 * The table has three columns, select, parent and table name.
 * The select column contains a checkbox for each row and the
 * parent column contains a radio button for each row.
 *
 */
public class PSTdTablesTable extends Composite implements SelectionListener
{

   /**
    * Ctor
    * @param parent the parent widget for the table.
    */
   public PSTdTablesTable(Composite parent)
   {
      super(parent, SWT.NONE);
      m_color_white = new Color(this.getDisplay(), 255, 255, 255);
      setLayout(new FillLayout());
      ScrolledComposite sc = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
      sc.setExpandHorizontal(true);
      sc.setExpandVertical(true);
      final Composite comp = new Composite(sc, SWT.NONE);
      sc.setContent(comp);
      comp.setLayout(new FillLayout());
      m_table = new Table(comp, SWT.HIDE_SELECTION);
      m_table.setHeaderVisible(true);
      m_table.setLinesVisible(true);
      m_table.setLayout(new PSAutoResizeTableLayout(m_table));
      addColumns();
      
      
   }
   
   /**
    * Return a list of all table rows.
    * @return all table <code>TdTableRow</code> objects. Never 
    * <code>null</code>, may be empty.
    */
   public List<TdTableRow> getRows()
   {
      List<TdTableRow> rows = 
         new ArrayList<TdTableRow>(m_table.getItemCount());
      for(TableItem item : m_table.getItems())
      {
         rows.add((TdTableRow)item.getData());
      }
      return rows;
   }
   
   /**
    * Sets all rows for the table.
    * @param rows a list of <code>TdTableRow</code> objects. 
    * Cannot be <code>null</code>.
    */
   public void setRows(List<TdTableRow> rows)
   {
      if(rows == null)
         throw new IllegalArgumentException("rows cannot be null.");
      clear();
      for(TdTableRow row : rows)
      {
         addRow(row);
      }
   }
   
   /**
    * Clears all rows in the table.
    */
   public void clear()
   {
      for(Button button : m_controls.keySet())
      {
         button.dispose();
      }
      m_controls.clear();
      m_table.removeAll();
   }
   /**
    * Sets all select checkboxes as selected.
    */
   public void selectAll()
   {
      for(Button button : m_controls.keySet())
      {
         button.setSelection(true);
         handleCheckboxSelection(button);
      }
   }
   
   /**
    * Sets all select checkboxes as unselected.
    */
   public void clearAll()
   {
      for(Button button : m_controls.keySet())
      {
         button.setSelection(false);
         handleCheckboxSelection(button);
      }
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */
   @Override
   public void dispose()
   {
      m_color_white.dispose();
      super.dispose();
   }

   /**
    * Adds a table row to the table. Handles adding the appropriate
    * editors and listeners.
    * @param row the table row to be added, cannot be <code>null</code>.
    */
   private void addRow(TdTableRow row)
   {
      if(row == null)
         throw new IllegalArgumentException("row cannot be null.");     
      TableItem item = new TableItem(m_table, SWT.NONE);
      item.setData(row);
      item.setText(1, row.getName());
      
      TableEditor cEditor = new TableEditor(m_table);
      Button checkButton = new Button(m_table, SWT.CHECK);
      checkButton.addSelectionListener(this);
      checkButton.setData(m_table.getItemCount() - 1);
      checkButton.setSelection(row.isSelected());
      checkButton.pack();
      cEditor.minimumWidth = checkButton.getSize().x;
      cEditor.horizontalAlignment = SWT.CENTER;
      cEditor.setEditor(checkButton, item, 0);
      m_controls.put(checkButton, row);
   }
   
   /**
    * Helper method to add all table columns.
    */
   private void addColumns()
   {
      PSAutoResizeTableLayout layout = 
         (PSAutoResizeTableLayout)m_table.getLayout();
      
      TableColumn selectCol = new TableColumn(m_table, SWT.LEFT);
      layout.addColumnData(new ColumnWeightData(5, 50));
      selectCol.setText("Select");      
      TableColumn nameCol = new TableColumn(m_table, SWT.LEFT);
      layout.addColumnData(new ColumnWeightData(20, 200));
      nameCol.setText("Table Name");      
   }   
   
   /* (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(
    * org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(SelectionEvent arg0)
   {
      // no-op      
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(
    * org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetSelected(SelectionEvent event)
   {
      Button button = (Button)event.getSource();
      handleCheckboxSelection(button);      
   }
   
   /**
    * Helper method to set the row selection flag based
    * on its checkbox.
    * @param button assumed not <code>null</code>.
    */
   private void handleCheckboxSelection(Button button)
   {
      TdTableRow row = m_controls.get(button);
      row.setSelected(button.getSelection());
   }
   
   /**
    * Selects tables by name.
    * @param tableNames cannot be <code>null</code>.
    */
   public void selectTablesByName(List<String> tableNames)
   {
      if(tableNames == null)
         throw new IllegalArgumentException("tableNames cannot be null.");
      for(Button button : m_controls.keySet())
      {
         TdTableRow row = m_controls.get(button);
         if(tableNames.contains(row.getName()))
         {
            button.setSelection(true);
            handleCheckboxSelection(button);
         }
       
      }
   }
   
   /** 
    * Small class to represent the table row data
    */
   class TdTableRow
   {
      /**
       * Ctor
       * @param name cannot be <code>null</code> or empty.
       * @param isSelected
       */
      public TdTableRow(String name, boolean isSelected)
      {
         if(StringUtils.isBlank(name))
            throw new IllegalArgumentException("Name cannot be null or empty.");
         mi_name = name;
         mi_selected = isSelected;
      }
      
      /**
       * @return the table name, never <code>null</code> or empty.
       */
      public String getName()
      {
         return mi_name;
      }
      
      /**
       * @return flag indicating the table is selected
       */
      public boolean isSelected()
      {
         return mi_selected;
      }
                  
      /**
       * Set table as selected.
       * @param selected 
       */
      public void setSelected(boolean selected)
      {
         mi_selected = selected;
      }      
      
      private String mi_name;
      private boolean mi_selected;
   }
   
   /**
    * The table object, initialized in the ctor. 
    * Never <code>null</code> after that.
    */
   private Table m_table;
        
   /**
    * Map of all controls embedded in the tables and the corresponding row. 
    * Never <code>null</code>, may be empty.
    */
   private Map<Button, TdTableRow> m_controls = new HashMap<Button, TdTableRow>();
   
   /**
    * Reference to a white color object. Need the reference for later
    * disposal.
    */
   private Color m_color_white;

   
   

}
