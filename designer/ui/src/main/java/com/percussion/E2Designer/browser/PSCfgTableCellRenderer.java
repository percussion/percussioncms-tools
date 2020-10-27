/******************************************************************************
 *
 * [ PSCfgTableCellRenderer.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgTableCellEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;

/**
 * The table cell editor that supports rendering of all columns of the
 * relationship configuration table's different views. In general it provides
 * a label for the data that represents a <code>String</code> and a check-box
 * for a <code>Boolean</code>. Displays a button with image 'C' for
 * 'Conditions' column.
 * Extracted it from {@link PSRelationshipEditorDialog} for easier comprehension.
 */
class PSCfgTableCellRenderer extends DefaultTableCellRenderer
{
   public PSCfgTableCellRenderer(PSRelationshipEditorDialog owner)
   {
      mi_owner = owner;
   }
   
   /**
    * Implements interface method to display check-box if the value is a
    * Boolean or a label for any other value except in 'conditions' column of
    * all table models. Displays a button with Bold 'C' image if it has
    * conditions, otherwise Normal 'C' image.
    */
   public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column)
   {
      Component renderer = null;
      if (value instanceof Boolean)
      {
         renderer = new JCheckBox();
         ((JCheckBox)renderer).setHorizontalAlignment(SwingConstants.CENTER);
         ((JCheckBox)renderer).setSelected(((Boolean)value).booleanValue());

         if (isSelected)
            renderer.setBackground(table.getSelectionBackground());
         else
            renderer.setBackground(table.getBackground());
      }
      else
      {
         TableModel model = table.getModel();
         String colName = model.getColumnName(column);

         if (colName.equals(mi_owner.getResourceString("conditionsColumn")))
         {
            if (value != null)
            {
               List conds = (List) value;
               
               boolean isCloningView = mi_owner.getCurViewName().equals(
                     mi_owner.getResourceString("cloningProps"));

               boolean editCloningConditionals = false;
               if (isCloningView)
               {
                  TableCellEditor editor = table.getCellEditor(row, column);
                  if (editor instanceof CfgTableCellEditor)
                  {
                     CfgTableCellEditor cellEditor = 
                        (CfgTableCellEditor) editor;
                     String actionCommand = 
                        cellEditor.m_condEditor.getActionCommand();
                     if (actionCommand != null)
                        editCloningConditionals = actionCommand.equals(
                           PSRelationshipEditorDialog.COMMAND_NAME_CLONING_CONFIG_CONDITIONALS);
                  }
               }
               
               renderer = new JButton();

               int threshold = isCloningView && editCloningConditionals
                     ? 2 : 1;

               if (conds.size() >= threshold)
                  ((JButton) renderer).setIcon(PSRelationshipEditorDialog.ms_condIcon);
               else
                  ((JButton) renderer).setIcon(PSRelationshipEditorDialog.ms_noCondIcon);
            }
         }
      }

      if(renderer == null)
      {
         renderer = super.getTableCellRendererComponent(table, value,
            isSelected, hasFocus, row, column);
      }
      return renderer;
   }
   
   final PSRelationshipEditorDialog mi_owner;
}
