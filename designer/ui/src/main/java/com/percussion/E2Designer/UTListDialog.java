/******************************************************************************
 *
 * [ UTListDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * The generic dialog with list box and command panel.
 */
public class UTListDialog extends PSDialog
{
   /**
    * Convenience constructor that calls {@link #UTListDialog(Frame, String, 
    * String, Vector, Object) UTListDialog(frame, title, listTitle, data, null)}.
    */
   public UTListDialog(Frame frame, String title, String listTitle, Vector data)
   {
      this(frame, title, listTitle, data, null);
   }
   
   /**
    * Constructs a list dialog with for the supplied data.
    * 
    * @param frame the parent frame, may be <code>null</code>.
    * @param title the dialog title, may be <code>null</code> or empty.
    * @param listTitle the list box title, not <code>null</code> or empty.
    * @param data a vector of objects to be added to the list, not 
    *    <code>null</code> or empty. Each data objects <code>toString()</code>
    *    method will be used to render the item for the list.
    * @param selectedValue the object which will be selected by default, may
    *    be <code>null</code> in which case nothing will be selected.
    */
   public UTListDialog(Frame frame, String title, String listTitle, 
      Vector data, Object selectedValue)
   {
      super(frame, title);

      if (listTitle == null || listTitle.trim().length() == 0)
         throw new IllegalArgumentException(
            "The title to set to the list box can not be null or empty");

      if (data == null || data.isEmpty())
         throw new IllegalArgumentException(
            "The data to set in the list box can not be null or empty");

      m_bInitialized = initDialog(listTitle, data);
      
      if (selectedValue != null)
         m_list.setSelectedValue(selectedValue, true);
   }

   /**
    * Create the dialog frme work and sets data.
    *
    * @param listTitle the title of the list box, assumed not to be
    * <code>null</code> or empty.
    * @param data the list of items to be added to the list, assumed not to be
    * <code>null</code> or empty and each item object's <code>toString()</code>
    * method will be used for rendering the item in the list.
    *
    * @return <code>true</code> to indicate initialization is successful,
    * otherwise <code>false</code>.
    */
   private boolean initDialog(String listTitle, Vector data)
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      panel.setBorder((new EmptyBorder (5,10,5,10)));

      JPanel command_panel = createCommandPanel();
      JPanel list_panel = createListPanel(listTitle, data);

      panel.add(list_panel);
      panel.add(Box.createHorizontalStrut(10));
      panel.add(command_panel);

      pack();
      center();
      return true;
   }


   /**
    *   Creates the command panel with OK, Cancel and Help buttons.
    *
    * @return the command panel, never <code>null</code>
    */
   private JPanel createCommandPanel()
   {
      UTStandardCommandPanel commandPanel =
         new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
      {
         public void onOk()
         {
            UTListDialog.this.onOk();
         }

      };

      getRootPane().setDefaultButton(commandPanel.getOkButton());

      return commandPanel;
   }

   /**
    * Creates a panel with a list box and title for it and adds all items
    * to the list.
    *
    * @param listTitle the title of the list box, assumed not to be
    * <code>null</code> or empty.
    * @param data the list of items to be added to the list, assumed not to be
    * <code>null</code> or empty and each item object's <code>toString()</code>
    * method will be used for rendering the item in the list.
    *
    * @return the panel, never <code>null</code>
    */
   private JPanel createListPanel(String listTitle, Vector data)
   {
      m_list = new JList(data);
      m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      m_list.addMouseListener(new MouseAdapter()
      {
         /**
          * Handles the double-click mouse event on an item in the list as
          * selecting that item and clicking OK.
          * <br>
          * @param event the mouse event generated for this action, assumed
          * not to be <code>null</code> as Swing model calls this method with
          * an event when mouse pressed action occurs.
          */
         public void mousePressed(MouseEvent event)
         {
            //handle the event only if left mouse button is double-clicked
            //on an item in the list
            if( ((event.getModifiers() & InputEvent.BUTTON1_MASK) ==
               InputEvent.BUTTON1_MASK) &&
               event.getClickCount() == 2 &&
               m_list.locationToIndex(event.getPoint()) != -1)
            {
               onOk();
            }
         }
      });
      JScrollPane pane = new JScrollPane(m_list);
      JLabel label = new JLabel(listTitle);
      label.setAlignmentX(LEFT_ALIGNMENT);

      JPanel list_panel = new JPanel();
      list_panel.setLayout(new BoxLayout(list_panel, BoxLayout.Y_AXIS));
      list_panel.add(label);
      list_panel.add(Box.createVerticalStrut(5));
      list_panel.add(pane);

      return list_panel;
   }

   /**
    * Accessor function to check whether OK button clicked or not.
    *
    * @return <code>true</code> if OK button is clicked
    **/
   public boolean isOk()
   {
      return  m_bOk;
   }

   /**
    * Accessor function to get the selected object.
    *
    * @return the selected object, may be <code>null</code> if no item is
    * selected.
    **/
   public Object getSelection()
   {
      return m_selected;
   }

   /**
    * Closes the dialog upon setting the flag to indicate that OK is clicked
    * and saves the selected object.
    **/
   public void onOk()
   {
      m_bOk = true;
      m_selected = m_list.getSelectedValue();

      setVisible(false);
      dispose();
   }

   /**
    * The list box to display the list of items. Initialized in
    * <code>createListPanel()</code> and never changed after that.
    */
   private JList m_list;

   /**
    * The selected item in the list, initialized in <code>onOk()</code> and
    * never changed after that.
    */
   private Object m_selected = null;

   /**
    * OK Clicked flag. Initialized to <code>false</code> and set to
    * <code>true</code> in {@link #onOk() onOk}.
    */
   private boolean m_bOk = false;

}
