/******************************************************************************
 *
 * [ PSAutoResizeTableLayout.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.layouts;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * A table layout that automatically resizes a table's columns
 * to fit the current visible space of the table.
 * @author erikserating
 *
 */
public class PSAutoResizeTableLayout extends TableLayout
   implements ControlListener
{
   
   /**
    * The ctor
    * @param table
    */
   public PSAutoResizeTableLayout(Table table)
   {
     this(table, null);
   }
   
   /**
    * The ctor
    * @param table
    * @param ratios
    */
   public PSAutoResizeTableLayout(Table table, float[] ratios)
   {
      m_table = table;
      m_ratios = ratios;
      table.addControlListener(this);
      registerUnregisterColumnControlListeners(true);     
      
   }

   /* 
    * @see org.eclipse.jface.viewers.TableLayout#addColumnData(
    * org.eclipse.jface.viewers.ColumnLayoutData)
    */
   @SuppressWarnings("unchecked")
   @Override
   public void addColumnData(ColumnLayoutData data)
   {
      m_columns.add(data);
      super.addColumnData(data);
   }

   /* 
    * @see org.eclipse.swt.events.ControlListener#controlMoved(
    * org.eclipse.swt.events.ControlEvent)
    */
   public void controlMoved(@SuppressWarnings("unused") ControlEvent e)
   {
      // no-op
   }

   /* 
    * @see org.eclipse.swt.events.ControlListener#controlResized(
    * org.eclipse.swt.events.ControlEvent)
    */
   public void controlResized(ControlEvent e)
   {
      Object source = e.getSource();
      if(source instanceof TableColumn)
      {
         m_ratios = getColumnWidthRatios();
         fireColumnResizedEvent();
      }
      else
      {
         if (m_autosizing)
            return;
         m_autosizing = true;
         try
         {
            autoSizeColumns();
         }
         finally
         {
            m_autosizing = false;
         }
      }
   }   
   

   /* 
    * @see org.eclipse.jface.viewers.TableLayout#layout(
    * org.eclipse.swt.widgets.Composite, boolean)
    */
   @Override
   public void layout(Composite c, boolean flush)
   {
      registerUnregisterColumnControlListeners(false);
      super.layout(c, flush);
      registerUnregisterColumnControlListeners(true);
   }

   /**
    * Does the calculations to auto resize the table columns
    */     
   private void autoSizeColumns()
   {
      int width = m_table.getClientArea().width;
      
      // XXX: Layout is being called with an invalid value
      // the first time it is being called on Linux.
      // This method resets the layout to null,
      // so we run it only when the value is OK.
      if (width <= 1)
         return;

      if(m_hasBeenSizedOnce && width <= getTotalColumnWidth())
         return;
      m_hasBeenSizedOnce = true;
      if(m_ratios != null && m_ratios.length == m_table.getColumnCount())
      {
         sizeColumnsByRatios();
         return;
      }
      
      TableColumn[] tableColumns = m_table.getColumns();
      int size = Math.min(m_columns.size(), tableColumns.length);
      int[] widths = new int[size];
      int fixedWidth = 0;
      int numberOfWeightColumns = 0;
      int totalWeight = 0;

      // First calc space occupied by fixed columns.
      for (int i = 0; i < size; i++)
      {
         ColumnLayoutData col = (ColumnLayoutData) m_columns.get(i);
         if (col instanceof ColumnPixelData)
         {
            int pixels = ((ColumnPixelData) col).width;
            widths[i] = pixels;
            fixedWidth += pixels;
         }
         else if (col instanceof ColumnWeightData)
         {
            ColumnWeightData cw = (ColumnWeightData) col;
            numberOfWeightColumns++;
            int weight = cw.weight;
            totalWeight += weight;
         }
         else
         {
            throw new IllegalStateException("Unknown column layout data");
         }
      }

      // Do we have columns that have a weight?
      if (numberOfWeightColumns > 0)
      {
         // Now, distribute the rest
         // to the columns with weight.
         int rest = width - fixedWidth;
         int totalDistributed = 0;
         for (int i = 0; i < size; i++)
         {
            ColumnLayoutData col = (ColumnLayoutData) m_columns.get(i);
            if (col instanceof ColumnWeightData)
            {
               ColumnWeightData cw = (ColumnWeightData) col;
               int weight = cw.weight;
               int pixels = totalWeight == 0 ? 0 : weight * rest / totalWeight;
               if (pixels < cw.minimumWidth)
                  pixels = cw.minimumWidth;
               totalDistributed += pixels;
               widths[i] = pixels;
            }
         }

         // Distribute any remaining pixels
         // to columns with weight.
         int diff = rest - totalDistributed;
         for (int i = 0; diff > 0; i++)
         {
            if (i == size)
               i = 0;
            ColumnLayoutData col = (ColumnLayoutData) m_columns.get(i);
            if (col instanceof ColumnWeightData)
            {
               ++widths[i];
               --diff;
            }
         }
      }
      boolean colsResized = false;
      registerUnregisterColumnControlListeners(false);
      for (int i = 0; i < size; i++)
      {
         if (tableColumns[i].getWidth() != widths[i])
         {
            tableColumns[i].setWidth(widths[i]);
            colsResized = true;
         }
      }
      registerUnregisterColumnControlListeners(true);
      if(colsResized)
      {
         m_ratios = getColumnWidthRatios();
         fireColumnResizedEvent();
      }
      
   }
   
   /**
    * Size the columns by ratios.
    */
   private void sizeColumnsByRatios()
   {
      int colCount = m_table.getColumnCount();
      if(m_ratios == null || m_ratios.length != colCount)
         throw new RuntimeException("ratios array cannot be null and must " +
            "contain an entry for each column.");
      float tablewidth = m_table.getClientArea().width;
      int[] widths = new int[m_table.getColumnCount()];
      int totalWidth = 0;
      TableColumn[] tableColumns = m_table.getColumns();
      // Calculate widths
      for(int i = 0; i < tableColumns.length; i++)         
      {
         widths[i] = (int)(tablewidth * m_ratios[i]);
         totalWidth += widths[i];
      }
      // Spread remaining pixels among columns
      if(totalWidth < (int)tablewidth)
      {
         int pixels = ((int)tablewidth) - totalWidth;
         for (int i = 0; pixels > 0; i++)
         {
            if (i == colCount)
               i = 0;            
            ++widths[i];
            --pixels;            
         }
      }
      // Remove overage pixels from columns
      else if(totalWidth > (int)tablewidth)
      {
         int pixels = totalWidth - ((int)tablewidth);
         for (int i = 0; pixels > 0; i++)
         {
            if (i == colCount)
               i = 0; 
            if(widths[i] > 10)
            {
               widths[i]--;
               --pixels;
            }
         }
      }
      //Set the widths
      boolean colsResized = false;
      registerUnregisterColumnControlListeners(false);
      for (int i = 0; i < colCount; i++)
      {
         if (tableColumns[i].getWidth() != widths[i])
         {
            tableColumns[i].setWidth(widths[i]);
            colsResized = true;
         }
      }
      registerUnregisterColumnControlListeners(true);
      if(colsResized)
         fireColumnResizedEvent();
   }
   
   /**
    * Calculates and returns the column width ratios for each
    * column in the table.
    * @return Never <code>null</code>.
    */
   public float[] getColumnWidthRatios()
   {
      float tablewidth = m_table.getClientArea().width;
      float[] ratios = new float[m_table.getColumnCount()];
      int count = 0;
      for(TableColumn col : m_table.getColumns())
      {
         float width = col.getWidth();
         ratios[count++] = width / tablewidth;         
      }
      return ratios;
   }
   
   /**
    * Sets the column width ratios. 
    * @param ratios cannot be <code>null</code> and there must
    * be a ratio for each existing table column.
    */
   public void setColumnWidthRatios(float[] ratios)
   {
      if(ratios == null)
         throw new IllegalArgumentException("Ratios cannot be null.");
      if(ratios.length != m_table.getColumnCount())
         throw new IllegalArgumentException(
            "There must be a ratio specified for each table column.");
      m_ratios = ratios;
   }
   
   /**
    * Adds the specified table layout listener
    * @param listener cannot be <code>null</code>.
    */
   public void addTableLayoutListener(IPSTableLayoutListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(!m_listeners.contains(listener))
      {
         m_listeners.add(listener);         
      }
   }
   
   /**
    * Removes the specified table layout listener
    * @param listener cannot be <code>null</code>.
    */
   public void removeTableLayoutListener(IPSTableLayoutListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(m_listeners.contains(listener))
      {
         m_listeners.remove(listener);         
      }
   }
   
   /**
    * Notifies all registered table modified listeners of
    * a table modified event.
    */
   protected void fireColumnResizedEvent()
   {
          
      PSTableLayoutEvent event = new PSTableLayoutEvent(this);
      for(IPSTableLayoutListener listener : m_listeners)
      {
        listener.columnsResized(event);
      }
   }
   
   /**
    * Helper method to calculate the total width of all
    * combined columns
    * @return total width
    */
   private int getTotalColumnWidth()
   {
      int width = 0; 
      for(TableColumn col : m_table.getColumns())
         width += col.getWidth();
      return width;
   }
   
   /**
    * Registers or unregisters control listeners on all the table columns
    * @param register if <code>true</code> then register, else
    * unregister.
    */
   private void registerUnregisterColumnControlListeners(boolean register)
   {
      for(TableColumn col : m_table.getColumns())
      {
         if(register)
            col.addControlListener(this);
         else
            col.removeControlListener(this);
      }
      
   }
   
   
   private final Table m_table;
   private List m_columns = new ArrayList();
   private boolean m_autosizing = false;
   private float[] m_ratios;
   private boolean m_hasBeenSizedOnce = false;
   private List<IPSTableLayoutListener> m_listeners = 
      new ArrayList<IPSTableLayoutListener>();
}
