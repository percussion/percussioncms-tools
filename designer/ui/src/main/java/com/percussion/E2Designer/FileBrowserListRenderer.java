/*[ FileBrowserListRenderer.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


import javax.swing.*;
import java.awt.*;


public class FileBrowserListRenderer extends JLabel
          implements javax.swing.ListCellRenderer, java.io.Serializable
{

   static private javax.swing.border.Border sm_noFocusBorder;
   private FileBrowser m_ref;

/**
  * Constructs a default renderer object for an item
  * in a list.
  */
   public FileBrowserListRenderer()
   {
      super();
      sm_noFocusBorder = new javax.swing.border.EmptyBorder(1, 1, 1, 1);
      setOpaque(true);
      setBorder(sm_noFocusBorder);
      m_ref = null;
   }


   public FileBrowserListRenderer(FileBrowser listOwner)
   {
      super();
      sm_noFocusBorder = new javax.swing.border.EmptyBorder(1, 1, 1, 1);
      setOpaque(true);
      setBorder(sm_noFocusBorder);
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
      if (index < m_ref.getDirFileSeparater())
           setIcon(new ImageIcon("/e2/designer/Classes/com/percussion/E2Designer/images/folder.gif"));
      else
           setIcon(new ImageIcon("/e2/designer/Classes/com/percussion/E2Designer/images/xml.gif"));

      setEnabled(list.isEnabled());
      setFont(list.getFont());
      setBorder((cellHasFocus) ? javax.swing.UIManager.getBorder("List.focusCellHighlightBorder") : sm_noFocusBorder);

      return this;
   }

    /**
     * A subclass of FileBrowserListRenderer that implements UIResource.
     * FileBrowserListRenderer doesn't implement UIResource
     * directly so that applications can safely override the
     * cellRenderer property with DefaultListCellRenderer subclasses.
     */
    public static class UIResource extends FileBrowserListRenderer
        implements javax.swing.plaf.UIResource
    {
    }
} // end FileBrowserListRenderer...

