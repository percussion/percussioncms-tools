/*[ JavaExitCellRenderer.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.BitmapManager;
import com.percussion.E2Designer.E2Designer;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;


/**
*custom DefaultTreeCellRenderer, modifies the icon based on the the
*JavaExitNode ( java exit type )
*/
public class JavaExitCellRenderer extends DefaultTreeCellRenderer
{
   /**
    *extracts the icon based on the passed value
    *
    *@param val which icon to extract
    *
    *@return icon or null if val is invalid
    *
    */
   private ImageIcon SetRepeatIcon( int type )
   {
      ImageIcon m_icon = null;

      String strIcon = null;

      // see which icon
      if ( type == JavaExitNode.REQUEST_PRE_PROC_EXT
         || type == JavaExitNode.RESULT_DOC_PROC_EXT )
      {
         strIcon="SmallJavaExit";
      }

      // found something?
      if( null != strIcon )
      {
         // get the bitmap manager
         BitmapManager manager=BitmapManager.getBitmapManager();
         // and get the icon
           m_icon = manager.getImage(E2Designer.getResources().getString(strIcon));
      }

      return(m_icon);
  }

   /**
    *changes the icon to based on the parent node
    *
    *@param tree the parent tree
    *
    *@param value the javaExitNode to be rendered
    *
    *@param sel if <code> true</code> node is selected
    *
    *@param expanded if <code> true </code> node is expanded
    *
    *@param leaf if <code> true </code> the node is a leaft
    *
    *@param row the row on tree
    *
    *@param hasFocus if <code> true <code> the tree has the focus
    */
   public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf,
      int row, boolean hasFocus)
   {
      super.getTreeCellRendererComponent(tree, value,
         sel, expanded, leaf, row, hasFocus);

      // see if value is a JavaExitNode
      if (value instanceof JavaExitNode)
      {
         // now can be cast
         JavaExitNode elem = (JavaExitNode) value;
         if ( elem != null)
         {
            // get the new icon
            ImageIcon m_icon = SetRepeatIcon(elem.getJavaExitType());
            if( m_icon != null )// found something?
               setIcon(m_icon);
         }
      }
      else if(value instanceof DefaultBrowserNode)
      {
         DefaultBrowserNode node = (DefaultBrowserNode)value;
         if(node.getIcon() != null)
            setIcon(node.getIcon());
      }
        return this;
   }
}



