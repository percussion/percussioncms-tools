/******************************************************************************
 *
 * [ PSCheckBoxNode.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.packager.ui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

class PSCheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {

  PSCheckBoxNodeRenderer renderer = new PSCheckBoxNodeRenderer();

  ChangeEvent changeEvent = null;

  JTree tree;
  DefaultMutableTreeNode m_lastNode;

  public PSCheckBoxNodeEditor(JTree tree) {
    this.tree = tree;
  }
  
  public DefaultMutableTreeNode getLastNode()
  {
     return m_lastNode;
  }

  public Object getCellEditorValue() {
     JCheckBox checkbox = renderer.getLeafRenderer();
     TreePath path = tree.getSelectionPath();
     if(path == null)
        return null;
     m_lastNode = 
        (DefaultMutableTreeNode)path.getLastPathComponent();
     PSCheckBoxNode node = (PSCheckBoxNode)m_lastNode.getUserObject();
     node.setSelected(checkbox.isSelected());    
     return node;
   }

  public boolean isCellEditable(EventObject event) {
    boolean returnValue = false;
    if (event instanceof MouseEvent) {
      MouseEvent mouseEvent = (MouseEvent) event;
      TreePath path = tree.getPathForLocation(mouseEvent.getX(),
          mouseEvent.getY());
      if (path != null) {
        Object node = path.getLastPathComponent();
        if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
          DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
          Object userObject = treeNode.getUserObject();
          returnValue = ((treeNode.isLeaf()) && (userObject instanceof PSCheckBoxNode));
        }
      }
    }
    return returnValue;
  }

  public Component getTreeCellEditorComponent(JTree tree, Object value,
      boolean selected, boolean expanded, boolean leaf, int row) {

    Component editor = renderer.getTreeCellRendererComponent(tree, value,
        true, expanded, leaf, row, true);

    // editor always selected / focused
    ItemListener itemListener = new ItemListener() {
      public void itemStateChanged(ItemEvent itemEvent) {
        if (stopCellEditing()) {
          fireEditingStopped();
        }
      }
    };
    if (editor instanceof JCheckBox) {
      ((JCheckBox) editor).addItemListener(itemListener);
    }

    return editor;
  }
}
