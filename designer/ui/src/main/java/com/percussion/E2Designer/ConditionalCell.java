/*[ ConditionalCell.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The panel used for the ConditionalCellEditor contains either a label
 * (display mode) or a button (edit mode).
 */
////////////////////////////////////////////////////////////////////////////////
public class ConditionalCell extends JPanel
{
   /**
   * Construct the panel, assigning an editor, which is brought up while the
   * edit button was pressed.
   *
   * @param editor the editore dialog
    */
  //////////////////////////////////////////////////////////////////////////////
   public ConditionalCell(ConditionalPropertyDialog editor)
   {
     m_editor = editor;
     initPanel();
   }

   /**
   * Sets the panel into edit mode.
   */
  //////////////////////////////////////////////////////////////////////////////
  public void setIcon(ImageIcon icon)
  {
     m_label.setIcon(icon);
  }

   /**
   * Set editor value.
   *
   * @param value the new value to set
   */
  //////////////////////////////////////////////////////////////////////////////
  public void setValue(Object value)
  {
     m_value = value;
  }

   /**
   * Get editor value.
   *
   * @return Object the editor value
   */
  //////////////////////////////////////////////////////////////////////////////
  public Object getValue()
  {
      return m_value;
  }

   /**
   * Get the display status (returns true if in display mode, false if in edit
   * mode).
   *
   * @return boolean the current display status
   */
  //////////////////////////////////////////////////////////////////////////////
  public boolean isDisplayMode()
  {
      return m_isDisplayMode;
  }

   /**
   * Sets the panel into display mode.
   */
  //////////////////////////////////////////////////////////////////////////////
  public void setDisplayMode()
  {
      removeAll();
    add(m_displayPanel);
    m_isDisplayMode = true;

    invalidate();
    repaint();
  }

   /**
   * Sets the panel into edit mode.
   */
  //////////////////////////////////////////////////////////////////////////////
  public void setEditMode()
  {
      removeAll();
    add(m_editPanel);
    m_isDisplayMode = false;

    invalidate();
    repaint();
  }

   /**
   * Toggles the panel view from display mode to edit mode and vice versa.
   */
  //////////////////////////////////////////////////////////////////////////////
  public void toggleDisplayMode()
  {
    removeAll();
     if (isDisplayMode())
    {
      add(m_editPanel);
      m_isDisplayMode = false;
    }
    else
    {
      add(m_displayPanel);
      m_isDisplayMode = true;
    }

    invalidate();
    repaint();
  }

   /**
    * Set the current active backend datatank.
   *
    * @backendTank the new backend tank
    */
   //////////////////////////////////////////////////////////////////////////////
   public void setBackendTank(OSBackendDatatank backendTank)
  {
     m_backendTank = backendTank;
  }

   /**
    * Set the current active page datatank.
   *
    * @pageTank the new page tank
    */
   //////////////////////////////////////////////////////////////////////////////
   public void setPageTank(OSPageDatatank pageTank)
  {
     m_pageTank = pageTank;
  }

   /**
   * Initialize the panels.
    */
  //////////////////////////////////////////////////////////////////////////////
  private void initPanel()
  {
     // create the panels
    m_displayPanel = createDisplayPanel();
    m_editPanel = createEditPanel();

    // add the display panel by default
     setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(m_displayPanel);
    m_isDisplayMode = true;
  }

   /**
   * Create and initialize the display panel. This will be displayed if the cell
   * is not in edit-mode.
   *
   * return JPanel the display panel
    */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createDisplayPanel()
  {
     JPanel p1 = new JPanel();
    p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
    p1.add(m_label);

    return p1;
  }

   /**
   * Create and initialize the edit panel. This will be displayed if the cell
   * is in edit-mode.
   *
   * return JPanel the edit panel
    */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createEditPanel()
  {
     m_edit.addActionListener(new ActionListener()
    {
       public void actionPerformed(ActionEvent event)
      {
        if (m_editor != null)
        {
           m_editor.onEdit(m_value, m_backendTank, m_pageTank);
          m_editor.center();
           m_editor.setVisible(true);
          m_editor.dispose();
        }
      }
    });

     JPanel p1 = new JPanel();
    p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
      p1.add(m_edit);

    return p1;
  }

   //////////////////////////////////////////////////////////////////////////////
  /**
   * the value storage
   */
  private Object m_value = null;
  private OSBackendDatatank m_backendTank = null;
  private OSPageDatatank m_pageTank = null;
  /**
   * the edit button
   */
  private UTFixedButton m_edit = new UTFixedButton((new ImageIcon(getClass().getResource(E2Designer.getResources().getString("gif_Browser")))), new Dimension(20, 20));
  /**
   * the display label
   */
  private JLabel m_label = new JLabel(new ImageIcon(getClass().getResource(E2Designer.getResources().getString("gif_Conditionals"))));
  /**
   * the editor dialog
   */
  private ConditionalPropertyDialog m_editor = null;
  /**
   * the panel display status: true for display mode, false for edit mode
   */
  private boolean m_isDisplayMode = true;
  /**
   * the display panel
   */
  private JPanel m_displayPanel = null;
  /**
   * the edit panel
   */
  private JPanel m_editPanel = null;
}

