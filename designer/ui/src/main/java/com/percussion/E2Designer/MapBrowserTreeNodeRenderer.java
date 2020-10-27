/*[ MapBrowserTreeNodeRenderer.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.xml.PSDtdNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Custom DefaultTreeCellRenderer, modifies the icon based on the the
 * MapBrowserTreeNode
 */
public class MapBrowserTreeNodeRenderer extends DefaultTreeCellRenderer
{
   /**
    * Extracts the icon based on the passed value.
    *
    * @param val which icon to extract.
    *
    * @return icon or null if val is invalid.
    */
   private ImageIcon SetRepeatIcon(int val)
   {
      ImageIcon icon=null;
      String strIcon=new String();
      // see which icon
      switch( val )
      {
         case PSDtdNode.OCCURS_ANY:
            strIcon="gif_repeatzeroormore";
         break;
         case PSDtdNode.OCCURS_ATLEASTONCE :
            strIcon="gif_repeatoneormore";
         break;
         case PSDtdNode.OCCURS_OPTIONAL:
            strIcon="gif_repeatoptional";
         break;
         case PSDtdNode.OCCURS_ONCE:
            strIcon="gif_repeatexactlyonce";
         break;
         case 2000:
            strIcon="gif_repeatat";
         break;
      }
      // found something?
      if( strIcon.length() > 0 )
      {
         // get the bitmap manager
         BitmapManager manager=BitmapManager.getBitmapManager();
         // and get the icon
         icon=manager.getImage(E2Designer.getResources().getString(strIcon));
      }
      return icon;
   }

   /**
    * Changes the icon to based on the parent node
    *
    * @param tree the parent tree
    * @param value the MapBrowserTreeNode to be rendered
    * @param sel if <code> true</code> node is selected
    * @param expanded if <code> true </code> node is expanded
    * @param leaf if <code> true </code> the node is a leaft
    * @param row the row on tree
    * @param hasFocus if <code> true <code> the tree has the focus
    */
   public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf,
      int row, boolean hasFocus)
   {
      super.getTreeCellRendererComponent(tree, value,
         sel, expanded, leaf, row, hasFocus);
      // see if value is a MapBrowserTreeNode
      if (value instanceof MapBrowserTreeNode)
      {
         // now can be cast
         MapBrowserTreeNode elem=(MapBrowserTreeNode)value;
         if( elem != null)
         {
            ImageIcon icon=null;
            BitmapManager manager=BitmapManager.getBitmapManager();
            // see if this is not the root
            if( elem.getNodeType() != elem.NODETYPEROOT )
            {
               if(elem.isUdf())
               {
                  String context = elem.getUdfExit().getRef().getContext();
                  String toolTip = context.substring(0,context.lastIndexOf("/"));

                  if(toolTip.startsWith("global"))
                  {
                     icon = manager.getImage(
                        E2Designer.getResources().getString("gif_globalUdf"));
                  }
                  else
                  {
                     icon = manager.getImage(
                        E2Designer.getResources().getString("gif_appUdf"));
                  }
                  setIcon(icon);
                  setToolTipText(toolTip);
               }
               else if (elem.isDTD())
               {
                  // get the new icon
                  icon=SetRepeatIcon(elem.getRepeatAtribute());
                  if(icon != null)// found something?
                     setIcon(icon);  // change the icon

                  setToolTipText(null);

               }
               else
               {
                  if(elem.isLeaf())
                  {  
                     icon = manager.getImage(
                        E2Designer.getResources().getString("gif_genericIcon"));
                     setIcon(icon);
                  }
                  setToolTipText(null);
               }
            }
         }
      }
      else
         setToolTipText(null);
         
      return this;
   }
}


