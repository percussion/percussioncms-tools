/******************************************************************************
 *
 * [ PSTreeCellRender.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.packager.ui;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * @author luisteixeira
 * 
 * Render Parent nodes for Design Elements Tree
 */
public class PSTreeCellRenderer extends DefaultTreeCellRenderer
{

   Color selectionBackground = UIManager.getColor("Tree.selectionBackground");
   Color textBackground = UIManager.getColor("Tree.textBackground");
   
   /**
    * Returns the color to use for the background if node is selected.
    */
  @Override
public Color getBackgroundSelectionColor() {
  return selectionBackground;
  }
 
 /**
  * Returns the background color to be used for non selected nodes.
  */
@Override
public Color getBackgroundNonSelectionColor() {
return textBackground;
}

/**
 * Returns the color the border is drawn.
 */
@Override
public Color getBorderSelectionColor() {
return selectionBackground;
}
}
