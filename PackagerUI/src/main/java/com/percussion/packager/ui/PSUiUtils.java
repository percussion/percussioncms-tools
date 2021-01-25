/******************************************************************************
 *
 * [ PSUiUtils.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Enumeration;

/**
 * @author erikserating
 *
 */
public class PSUiUtils
{
   /**
    * Private ctor to inhibit initialization
    */
   private PSUiUtils()
   {
      
      
   }
   
   /**
    * Centers a window on the screen, based on its current size.
    * @window the dialog to be centered, cannot be <code>null</code>.
    */   
   public static void center(Window window)
   {
      if(window == null)
         throw new IllegalArgumentException("dialog cannot be null.");
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = window.getSize();
      window.setLocation(( screenSize.width - size.width ) / 2,
            ( screenSize.height - size.height ) / 2 );
   }
   
   /**
    * Creates the menu items with the specified parameters.
    *
    * @param label the label to be used for menu item, may not be <code>
    * null</code> or empty.
    * @param mnemonic the character that is set to access menu items by using
    * ALT key combos, ignored if it is '0'.
    * @param accelKey keystroke describing the accelerator key for this menu,
    * ignored if it is <code>null</code>
    * @param toolTip the tool tip to be set on menu item, ignored if it is
    * <code>null</code> or empty.
    * @param icon The icon to set, ignored if it is <code>null</code>
    * @param name the name of the component to recognize it, ignored if it is
    * <code>null</code> or empty.
    * @param actionCommand the action command to set, ignored if it is <code>
    * null</code> or empty.
    * @param listener the listener to this menu action, ignored if it is <code>
    * null</code>
    *
    * @return the menu item, never <code>null</code>
    *
    * @throws IllegalArgumentException if label is <code>null</code> or empty.
    */
   public static JMenuItem createMenuItem(String label, char mnemonic,
      KeyStroke accelKey, String toolTip, ImageIcon icon, String name,
      String actionCommand, ActionListener listener)
   {
      if(label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty.");

      JMenuItem menuItem = new JMenuItem( label );
      if(0 != mnemonic)
         menuItem.setMnemonic(mnemonic);

      if(null != accelKey)
         menuItem.setAccelerator( accelKey );

      if (null != toolTip && toolTip.length() > 0)
         menuItem.setToolTipText( toolTip );

      if(null != icon)
         menuItem.setIcon(icon);

      if (null != name && name.length() > 0)
         menuItem.setName(name);

      if (null != actionCommand && actionCommand.length() > 0)
         menuItem.setActionCommand(actionCommand);

      if(null != listener)
         menuItem.addActionListener(listener);

      return menuItem;
   }
   
   /**
    * Get the expansion state of a tree.
    *
    * @param tree
    * @return expanded tree path as Enumeration
    */
   public static Enumeration getExpansionState(JTree tree) {

       return tree.getExpandedDescendants(
          new TreePath(tree.getModel().getRoot()));

   }


   /**
    * Restore the expansion state of a JTree.
    * @param tree
    * @param enumeration an Enumeration of expansion state. You can get it using
    *  {@link #saveExpansionState(javax.swing.JTree)}.
    */
   public static void loadExpansionState(JTree tree, Enumeration enumeration) 
   {
       if (enumeration != null)
       {
           while (enumeration.hasMoreElements())
           {
              TreePath treePath = (TreePath) enumeration.nextElement();              
              tree.expandPath(treePath);
           }
       }
   }  
   
   /**
    * Modifies the JFileChooser to be a directory only chooser.
    * Changing the file name label to directory name and removing the
    * file type field. Also sets file selection to DIRECTORIES_ONLY.
    */
   public static void makeDirChooser(Container c)
   {
      if(c instanceof JFileChooser)
         ((JFileChooser)c).setFileSelectionMode(
            JFileChooser.DIRECTORIES_ONLY);
      String fileName = 
         UIManager.getString("FileChooser.fileNameLabelText");
      String fileType = 
         UIManager.getString("FileChooser.filesOfTypeLabelText");
      int len = c.getComponentCount();
      for (int i = 0; i < len; i++)
      {
         Component comp = c.getComponent(i);
         if (comp instanceof JLabel)
         {
            JLabel lab = (JLabel) comp;
            if(fileName.equals(lab.getText()))
            {
               lab.setText(
                  PSResourceUtils.getCommonResourceString("label.dir.name"));               
            }
            if(fileType.equals(lab.getText()))
            {
               lab.setVisible(false);
               c.getComponent(i + 1).setVisible(false);
            }
            
         }
         else if (comp instanceof Container)
         {
            makeDirChooser((Container) comp);
         }
      }
   }
  
}
