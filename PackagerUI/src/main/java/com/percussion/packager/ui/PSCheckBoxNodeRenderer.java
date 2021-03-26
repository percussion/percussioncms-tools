/******************************************************************************
 *
 * [ PSCheckBoxNodeRenderer.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.packager.ui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Renderer for Design Element tree
 */
class PSCheckBoxNodeRenderer implements TreeCellRenderer {
  private JCheckBox leafRenderer = new JCheckBox();

  /**
   * Renderer for parent nodes
   */
  private PSTreeCellRenderer nonLeafRenderer = new PSTreeCellRenderer();

  /**
   * Color variables
   */
  Color selectionBorderColor, selectionForeground, selectionBackground,
      textForeground, textBackground;
  
  /**
   * Get Leaf Renderer
   * 
   * @return leafRender - rendered child
   */
  protected JCheckBox getLeafRenderer() {
    return leafRenderer;
  }

  /**
   * Renderer for Design Element tree
   */
  public PSCheckBoxNodeRenderer() {
    Font fontValue;
    fontValue = UIManager.getFont("Tree.font");
    if (fontValue != null) {
      leafRenderer.setFont(fontValue);
    }
    Boolean booleanValue = (Boolean) UIManager
        .get("Tree.drawsFocusBorderAroundIcon");
    leafRenderer.setFocusPainted((booleanValue != null)
        && (booleanValue.booleanValue()));

    selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
    selectionForeground = UIManager.getColor("Tree.selectionForeground");
    selectionBackground = UIManager.getColor("Tree.selectionBackground");
    textForeground = UIManager.getColor("Tree.textForeground");
    textBackground = UIManager.getColor("Tree.textBackground");
  }

  /**
   * Gets rendered component.  Renders children and uses PSTreeCellRender 
   * for Parents.
   */
  public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean selected, boolean expanded, boolean leaf, int row,
      boolean hasFocus) {
     
    Component returnValue;
    
    // Child
    if (leaf && 
       (value != null && 
          ((DefaultMutableTreeNode)value).getUserObject() != null))
    {

      String stringValue = tree.convertValueToText(value, selected,
          expanded, leaf, row, false);
      leafRenderer.setText(stringValue);
      leafRenderer.setSelected(false);

      leafRenderer.setEnabled(tree.isEnabled());
      leafRenderer.setOpaque(false);
      if (selected) {
        leafRenderer.setForeground(selectionForeground);
        leafRenderer.setBackground(selectionBackground);
      } else {
        leafRenderer.setForeground(textForeground);
        leafRenderer.setBackground(textBackground);
      }

      if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
        Object userObject = ((DefaultMutableTreeNode) value)
            .getUserObject();
        if (userObject instanceof PSCheckBoxNode) {
          PSCheckBoxNode node = (PSCheckBoxNode) userObject;
          leafRenderer.setText(node.getText());
          leafRenderer.setSelected(node.isSelected());
        }
      }

      returnValue = leafRenderer;
    } else {
      // parent 
      returnValue = nonLeafRenderer.getTreeCellRendererComponent(tree,
          value, selected, expanded, leaf, row, hasFocus);
      if (selected){
         // Had to hardcode color - this needs to be changed if we change 
         //look and feel
         returnValue.setBackground(new Color(248, 220, 137));
      }
      else
      {
         returnValue.setBackground(UIManager.getColor("Tree.textBackground"));
      }

    }
    return returnValue;
  }
}
