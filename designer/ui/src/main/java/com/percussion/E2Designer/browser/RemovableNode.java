/*[ RemovableNode.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * A removable node is designed for entries that need to allow the user to
 * remove the entry from the tree. A menu with 1 action is returned that
 * allows removal of this object (it does not delete what the entry represents.
 * If this is desired, override this class.)
 */
public class RemovableNode extends DefaultBrowserNode
{
  //create static resource bundle object
  static ResourceBundle res = BrowserFrame.getBrowser().getResources();

   // constructors
   RemovableNode( Object userObject )
   {
      super( userObject );
   }

   /**
    * A removable node is removable by default.
    */
   public boolean isRemovable( )
   {
      return m_bRemovable;
   }

   /**
    * Sets the removable state of the node. If bAllowRemoval is <code>true</code>,
    * then the context menu will show a Remove action item. If it is false, no
    * context menu will be available.
    */
   public void setRemovable( boolean bAllowRemoval )
   {
      m_bRemovable = bAllowRemoval;
   }

   /**
    * This node has a menu, so <code>true</code> is returned.
    */
   public boolean hasMenu( )
   {
      return true;
   }

   /**
    * Creates a menu with a single 'Remove' Action. If derived classes want
    * to add extra code for the remove action, override removeEntry and call
    * the base classes' implemention before performing the additional work.
    * <p>
    * If isRemovable() returns false, null is returned.
    */
   public JPopupMenu getContextMenu( )
   {
    if(!isRemovable())
      return null;

    JPopupMenu popup = new JPopupMenu();
    popup.add(new JMenuItem(res.getString("REMOVE")));
      return popup;
   }

   /**
    * Does the work of removing this entry from the tree when the user selects
    * the remove action item in the context menu.
    * <p>
    * If this method is called when the node is not removable, an exception is
    * thrown.
    *
    * @throws UnsupportedOperationException if isRemovable returns <code>false</code>
    * when this method is called.
    */
   protected void removeEntry( )
   {
    if(!isRemovable())
      throw new UnsupportedOperationException("Node is not removable");
    this.removeFromParent();

   }

   // storage
   private boolean m_bRemovable = true;
}


