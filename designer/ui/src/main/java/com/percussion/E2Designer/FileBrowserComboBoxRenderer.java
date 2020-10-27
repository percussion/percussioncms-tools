/*[ FileBrowserComboBoxRenderer.java ]*****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class FileBrowserComboBoxRenderer extends JLabel
          implements javax.swing.ListCellRenderer, java.io.Serializable
{

   private javax.swing.border.Border m_noFocusBorder;
   private FileBrowser m_ref;

/**
  * Constructs a default renderer object for an item
  * in a list.
  */
   public FileBrowserComboBoxRenderer()
   {
      super();
      setOpaque(true);
      m_ref = null;
   }


   public FileBrowserComboBoxRenderer(FileBrowser listOwner)
   {
      super();
      setOpaque(true);
      m_ref = listOwner;
   }


   public Component getListCellRendererComponent( JList list,
                                                   Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus)
   {

      if (isSelected)
      {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
      }
      else
      {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
      }

      setText((value == null) ? "" : value.toString());

      setIcon(new ImageIcon("/e2/designer/Classes/com/percussion/E2Designer/images/folder.gif"));

      int i = ((FileBrowserComboBoxModel)m_ref.getDirStructModel()).getLevelAt(index);

      // add indent here with EmptyBorder...
      //setBorder(new EmptyBorder(0, 5 * i + 1,0,0));
      //m_noFocusBorder = new EmptyBorder(0, 5 * i + 1,0,0);

      m_noFocusBorder = new CompoundBorder(new EmptyBorder(1,1,1,1),
                                           new EmptyBorder(0,4 * i + 1,0,0));

      setEnabled(list.isEnabled());
      setFont(list.getFont());

      setBorder((cellHasFocus) ? javax.swing.UIManager.getBorder("List.focusCellHighlightBorder") : m_noFocusBorder);

      return this;
   }

    /**
     * A subclass of FileBrowserComboBoxRenderer that implements UIResource.
     * FileBrowserComboBoxRenderer doesn't implement UIResource
     * directly so that applications can safely override the
     * cellRenderer property with DefaultListCellRenderer subclasses.
     */
    public static class UIResource extends FileBrowserComboBoxRenderer
        implements javax.swing.plaf.UIResource
    {
    }
} // end FileBrowserComboBoxRenderer...

