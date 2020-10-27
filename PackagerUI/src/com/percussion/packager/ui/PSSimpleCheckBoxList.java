/******************************************************************************
 *
 * [ PSimpleCheckBoxList.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author erikserating
 *
 */
public class PSSimpleCheckBoxList extends JList
{
   
   public PSSimpleCheckBoxList()
   {
      setCellRenderer(new CellRenderer());

      addMouseListener(new MouseAdapter()
      {
         @Override
         public void mousePressed(MouseEvent e)
         {
            int index = locationToIndex(e.getPoint());

            if (index != -1)
            {
               JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
               if (checkbox.isEnabled())
               {
                  checkbox.setSelected(!checkbox.isSelected());
                  repaint();
               }
            }
         }
      });

      addKeyListener(new KeyAdapter()
      {

         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
          */
         @Override
         public void keyPressed(KeyEvent e)
         {
            JList source = (JList) e.getSource();
            int code = e.getKeyCode();
            if (code == 32)
            {
               JCheckBox checkbox = (JCheckBox) source.getSelectedValue();
               if (checkbox != null)
               {
                  if (checkbox.isEnabled())
                  {
                     checkbox.setSelected(!checkbox.isSelected());
                     repaint();
                  }
               }
            }

         }

      });

      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);     
      

   }   

   protected class CellRenderer implements ListCellRenderer
   {
      @SuppressWarnings("unused")
      public Component getListCellRendererComponent(                    
                  JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus)
      {
         JCheckBox checkbox = (JCheckBox) value;
         checkbox.setBackground(isSelected ?
                 getSelectionBackground() : getBackground());
         checkbox.setForeground(isSelected ?
                 getSelectionForeground() : getForeground());
         checkbox.setEnabled(isEnabled());
         checkbox.setFont(getFont());
         checkbox.setFocusPainted(false);
         checkbox.setBorderPainted(true);
         checkbox.setBorder(isSelected ?
          UIManager.getBorder(
           "List.focusCellHighlightBorder") : noFocusBorder);
         return checkbox;
      }
   } 
   
   protected static Border noFocusBorder =
      new EmptyBorder(1, 1, 1, 1);



}
