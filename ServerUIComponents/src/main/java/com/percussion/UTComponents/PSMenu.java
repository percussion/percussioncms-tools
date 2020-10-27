/*[ PSMenu.java ]**************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.UTComponents;

import javax.swing.*;


/**
 * This class deals with PSAction objects rather than Action objects. PSAction
 * objects have more information about how to display themselves in a menu.
 * <p>
 * If the supplied action is not a PSAction, this object behaves identically 
 * to its base class.
 */
public class PSMenu extends JMenu
{
   // constructors
   /**
    * Default constructor.
    */
   PSMenu()
   {
   }
   
   /**
    * Present to match base class interface.
    * 
    * @param strLabel the label for menu, may be <code>null</code> or empty.
    */
   public PSMenu(String strLabel)
   {
      super(strLabel);
   }
   
   /**
    * Constructs menu with supplied label and mnemonic.
    * 
    * @param strLabel the label of the menu, may be <code>null</code> or empty.
    * @param mnemonic the keyboard character mnemonic 
    */
   public PSMenu(String strLabel, char mnemonic)
   {
      super(strLabel);
      setMnemonic( mnemonic);
   }
   
   /**
    * Similar to add(), but creates a check box menu item rather than a 
    * standard menu item.
    *
    * @returns the newly created menu item
    */
   public JCheckBoxMenuItem addCheckBox(PSAction a)
   {
      JCheckBoxMenuItem newItem = new JCheckBoxMenuItem((String) a.getValue(Action.NAME));
      decorateMenuItem(newItem, a);
      newItem.addActionListener(a);
      add(newItem);
      return(newItem);
   }
  
   public JMenuItem add(Action a)
   {
      JMenuItem newItem = null;
      if (a instanceof PSAction)
         newItem = insert(a, getItemCount());
      else
         newItem = super.add(a);
      return(newItem);
      
   }
   
   /**
    * Inserts the supplied action in this menu. If the supplied action is a 
    * PSAction object, checks if various properties are set. If they are, the
    * menuitem is modified appropriately.
    *
    * @returns the newly created MenuItem
    */
   public JMenuItem insert(Action a, int pos)
   {
      JMenuItem newItem = super.insert(a, pos);
      if (a instanceof PSAction)
      {
         // we have more info to set the menu item
         decorateMenuItem(newItem, (PSAction) a);
      }
      return(newItem);
      
   }

   /**
    * Takes properties out of the action and sets the corresponding property
    * in the menu item.
    *
    * @returns the passed in menu item
    */
   private JMenuItem decorateMenuItem(JMenuItem item, PSAction action)
   {
      char cMnemonic = action.getMnemonic();
      if (0 != cMnemonic)
           item.setMnemonic(cMnemonic);

      KeyStroke ks = action.getAccelerator();
      if (null != ks)
         item.setAccelerator(ks);
      item.setIcon(action.getIcon());
   
      String strTTText = action.getToolTipText();
      if (strTTText.length() > 0)
         item.setToolTipText(strTTText);
      return(item);
      
   }
}
