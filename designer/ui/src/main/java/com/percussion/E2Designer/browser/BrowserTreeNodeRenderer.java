/*[ BrowserTreeNodeRenderer.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;


import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;



public class BrowserTreeNodeRenderer extends DefaultTreeCellRenderer {

  public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf,
      int row, boolean hasFocus) {
      
      super.getTreeCellRendererComponent(tree, value,
         sel, expanded, leaf, row, hasFocus);

      ImageIcon icon = ((DefaultBrowserNode)value).getIcon();

      if(icon != null)
         setIcon(icon);
      return this;
  }
}
