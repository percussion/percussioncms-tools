/*[ XMLDropTargetTextField.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

/**
 * A droppable table.
 */
////////////////////////////////////////////////////////////////////////////////
public class XMLDropTargetTextField extends UTFixedTextField
                                    implements DropTargetListener
{
   /**
   * Construct a new fixed text field with the standard size.
   *
   * @param string the initial text
   */
  //////////////////////////////////////////////////////////////////////////////
  public XMLDropTargetTextField(String text)
  {
    super(text);
    dropTarget = new DropTarget (this, this);
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementation for DropTargetListener
  public void dragEnter(DropTargetDragEvent dropTargetDragEvent)
  {
    dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementation for DropTargetListener
  public void dragExit(DropTargetEvent dropTargetEvent)
  {
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementation for DropTargetListener
  public void dragOver(DropTargetDragEvent dropTargetDragEvent)
  {
    if (dropTargetDragEvent.isDataFlavorSupported(MapBrowserTreeNode.MAPPER_XML_TREENODE_FLAVOR) &&
        isEnabled())
      dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    else
      dropTargetDragEvent.rejectDrag();
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementation for DropTargetListener
  public synchronized void drop(DropTargetDropEvent dropTargetDropEvent)
  {
    try
    {
       Transferable tr = dropTargetDropEvent.getTransferable();
      if (tr.isDataFlavorSupported(MapBrowserTreeNode.MAPPER_XML_TREENODE_FLAVOR) &&
          isEnabled())
      {
        dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        Object userObject = tr.getTransferData(MapBrowserTreeNode.MAPPER_XML_TREENODE_FLAVOR);
        MapBrowserTreeNode node = (MapBrowserTreeNode) userObject;
        this.setText(node.getPathString());

        repaint();
        dropTargetDropEvent.dropComplete(true);
      }
      else
        dropTargetDropEvent.rejectDrop();
    }
    catch (Exception e)
    {
      dropTargetDropEvent.dropComplete(false);
      e.printStackTrace();
    }
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementation for DropTargetListener
  public void dropActionChanged (DropTargetDragEvent dropTargetDragEvent)
  {
  }

  //////////////////////////////////////////////////////////////////////////////
  private DropTarget dropTarget = null;
}
