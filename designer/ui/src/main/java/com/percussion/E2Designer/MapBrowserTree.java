/******************************************************************************
 *
 * [ MapBrowserTree.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.xml.PSDtdNode;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;


/**
 * A draggable tree.
 */
////////////////////////////////////////////////////////////////////////////////
public class MapBrowserTree extends JTree implements DragGestureListener,DropTargetListener
{
   /**
   * Default constructor.
    */
  //////////////////////////////////////////////////////////////////////////////
   public MapBrowserTree()
   {
      dragSource.createDefaultDragGestureRecognizer(this,
                                               DnDConstants.ACTION_COPY_OR_MOVE,
                                               this);
      // Drop target will register itself with the component
      new DropTarget(this,this);
      setCellRenderer(new MapBrowserTreeNodeRenderer());
      initListeners();
   }


  /**
   * @return <code>true</code> if the DTD was modified <code>false</code> if
   * not.
   */
   public boolean isDTDDirty()
   {
      return(m_dtdTreeDirty);
   }


  /**
   * sets the state of the DTD, if set then overwrite the DTD on OSPageDatatank.
   *
   *@param <code> true </code> if the DTD was modified <code>
   *false</code> if not
   */
   public void setDTDDirty(boolean bDTDisDirty)
   {
      m_dtdTreeDirty=bDTDisDirty;
   }
  /**
   * Constructor
   *
   * @param model   the tree model
   */
  //////////////////////////////////////////////////////////////////////////////
   public MapBrowserTree(TreeModel model)
   {
      super(model);
      dragSource.createDefaultDragGestureRecognizer(this,
                                               DnDConstants.ACTION_COPY_OR_MOVE,
                                               this);
      new DropTarget(this,this);
      setCellRenderer(new MapBrowserTreeNodeRenderer());
      initListeners();
   }


   private Component getUltimateParent(java.awt.Component item)
   {
      java.awt.Component parent = item.getParent();
      if (!(parent instanceof MapBrowser))
      {
         return getUltimateParent(parent);
      }
      else
      {
         return parent;
      }
   }


   //////////////////////////////////////////////////////////////////////////////
  // implementation for DragGestureListener
  public void dragGestureRecognized(DragGestureEvent dragGestureEvent)
  {
    TreePath path = getSelectionPath();
    if (path != null)
    {
      Object selectedNode = path.getLastPathComponent();
      if ( !(selectedNode instanceof MapBrowserTreeNode) )
         return; // ignore if last path node is not a MapBrowserTreeNode

      MapBrowserTreeNode selection = (MapBrowserTreeNode)selectedNode;

      if (isDragable(selection))
      {
         MapBrowserTreeNode node = new MapBrowserTreeNode(selection);
         dragSource.startDrag(dragGestureEvent, DragSource.DefaultCopyNoDrop,
                              node, dragSourceListener);
         dragSource.addDragSourceListener(new MapBrowserTreeDragSourceListener());
         m_causedDrag = this;
      }
    }
  }

  /**
   * Checks if the provided node is dragable in the current context. All leafs
   * are dragable. Then all XML nodes and folder which have only attributes
   * as children except the root node.
   *
   * @param node the node to check, may be <code>null</code>.
   * @return <code>true</code> if the provided node is dragable in the current
   *     context, <code>false</code> otherwise.
   */
  private boolean isDragable(MapBrowserTreeNode node)
  {
     if (node == null)
        return false;
     
     if (node.isLeaf())
        return true;
     
     if (node.isRoot())
        return false;
     
     if (node.isXml())
        return true;
     
     return false;
  }

  /**
  *set the tree read only
  *
  *@param treeReadOnly if <code> true </code> the DTD repeat attributes
  *can not be modified, <code> false </code> it can
  *
  */
  public void setDTDRepeatAttributesReadOnly()
  {
    m_treeReadOnly=true;
    removeListeners();
    setDTDDirty(false);
  }

  /**
  *@return <code> true </code> if tree is read only
  *<code> false </code> if tree can be modified
  */
  public boolean getDTDRepeatAttributesReadOnly()
  {
    return(m_treeReadOnly);
  }

  /**
   * remove the tree listeners, they were created trough initListeners
   *
   */
   private void removeListeners()
   {
      this.removeMouseListener(m_popupTriggerListener);
   }
  /**
   * initialize the tree listeners, they include the expanded tree, click and
   * menu listerners
   */
   private void initListeners()
   {
      m_popupTriggerListener = new  PopupTriggerListener();
      m_menuItemListener = new MenuItemListener();
      this.addMouseListener(m_popupTriggerListener);
   }

  /**
   * This class checks for the mouse action.
   *
   */
   class PopupTriggerListener extends MouseAdapter
   {
      /**
       *checks for the mouse release, displaying the menu
       *
       */
      public void mouseReleased( MouseEvent e )
      {
         if( m_treeReadOnly )
            return;

         createPopupMenu(e,e.getPoint());
      }
   }


  /**
   * creates the popup menu, which allows the user change the DTD repeat
   * attribute.
   *
   * @param point where the mouse cursor is located
   *
   */
   private void createPopupMenu(MouseEvent e ,Point point)
   {

    // find the tree item
    TreePath tpc = this.getPathForLocation(point.x, point.y);
    if(tpc != null)
    {
      this.setSelectionPath(tpc); //set this node as selected - enforce selection
    }
    TreePath tp = this.getSelectionPath();
    if( tp != null )
    {
       m_lastSelectedItem = this.getMinSelectionRow();
       Rectangle rectSel = this.getRowBounds(m_lastSelectedItem);
       if(rectSel.contains(point))
       {
           // create the popup menu and modify a single selection
            Object item=getLastSelectedPathComponent();
            if (item instanceof MapBrowserTreeNode)
            {
                // see if item is read only
                MapBrowserTreeNode node=(MapBrowserTreeNode)item;
                if( node.isNodeReadOnly() )
                {
                   return;   // it is do nothing
                }

                if( node.getNodeType() == MapBrowserTreeNode.NODETYPEROOT )
                {
                    if( node.getChildCount() == 0 )
                    {
                      return;
                    }
                }
           }
           createGeneralPopupMenu(point);
       }
       else
       {
           m_lastSelectedItem=-1; // outside the tree item, do nothing
       }
    }
    else
    {
           m_lastSelectedItem=-1; // outside the tree item, do nothing
    }

  }


  /**
   * this class is responsible to do the menu action, if selection was done and
   * tree is not read only call the MapBrowser with the new DTD repeat
   */
   class MenuItemListener implements ActionListener
   {
      /**
       * Menu event handler, if event is handled then call the MapBrowser with
       * new DTD repeat.
       *
       * @param e the event
       */
    public void actionPerformed( ActionEvent e)
    {
        JMenuItem mi = (JMenuItem)e.getSource();
        m_iRepeatAttribute=PSDtdNode.OCCURS_UNKNOWN;
      // find which attribute the user wants
       if (mi.getActionCommand().equals(E2Designer.getResources().getString("DTDCanAppearExactlyOnce")))
       {
         m_iRepeatAttribute=PSDtdNode.OCCURS_ONCE;  // ok
       }
       else if(mi.getActionCommand().equals(E2Designer.getResources().getString("DTDCanAppearZeroOrMore")))
       {
           m_iRepeatAttribute=PSDtdNode.OCCURS_OPTIONAL;  //ok
       }
       else if (mi.getActionCommand().equals(E2Designer.getResources().getString("DTDCanAppearOneOrMore")))
       {
            m_iRepeatAttribute=PSDtdNode.OCCURS_ATLEASTONCE;

       }
       else if (mi.getActionCommand().equals(E2Designer.getResources().getString("DTDCanAppearOptional")))
       {
           m_iRepeatAttribute=PSDtdNode.OCCURS_ANY;
       }

       // if attribute was set
       if( m_iRepeatAttribute != PSDtdNode.OCCURS_UNKNOWN)
       {
            //JTree
            Object item=getLastSelectedPathComponent();
            if (item instanceof MapBrowserTreeNode)
            {
              MapBrowserTreeNode node=(MapBrowserTreeNode)item;

              if( node.getNodeType() == MapBrowserTreeNode.NODETYPEROOT )
              {
                notifyAllChildren(node,m_iRepeatAttribute);
              }
              else
              {
                 node.setRepeatAttribute(m_iRepeatAttribute);
              }
              setDTDDirty(true);
              repaint();
            }
        }
    }
  }


  private void notifyAll(MapBrowserTreeNode node,int repeatAttribute)
  {
    if( visitedNodesMap.put(node.getPathString(),node) != null  )
             return;

    int count=node.getChildCount();
    if( count > 0)
    {
         Enumeration v = node.children();
         while(v.hasMoreElements())
         {
              node = (MapBrowserTreeNode)v.nextElement();
            node.setRepeatAttribute(repeatAttribute);
            if( node.getChildCount() > 0 )
            {
              notifyAll(node,repeatAttribute);
            }
         }
         if( node.getChildCount() > 0 )
         {
            notifyAll(node,repeatAttribute);
         }
    }


  }


 /**
 *changes all the tree repeat attributes
 *
 *@param node the root element
 *
 *@param  repeatAttribute the new repeat attribute
 *
 */
 private void notifyAllChildren(MapBrowserTreeNode node,int repeatAttribute)
 {
    if( visitedNodesMap == null )
    {
       visitedNodesMap=new HashMap();
    }
    else
    {
      visitedNodesMap.clear();
    }
    notifyAll(node,repeatAttribute);
 }

  /**
  *create the popup menu and display it
  *
  *@param point the mouse location
  *
  *@param bMultipleSelect is multiple selection
  *
  */
  private int max(int val1, int val2)
  {
     int iRet=0;
     if( val1 > val2 )
      iRet=val1;
     else
       iRet=val2;

     return(iRet);
  }


  private void createGeneralPopupMenu(Point point)
  {     
      JMenuItem mi = null;
      JPopupMenu popup = null;
      popup = new JPopupMenu();

      // add the items to the menu
       popup.add(new JMenuItem(E2Designer.getResources().getString("DTDCanAppearExactlyOnce")));
      popup.add(new JMenuItem(E2Designer.getResources().getString("DTDCanAppearZeroOrMore")));
      popup.add(new JMenuItem(E2Designer.getResources().getString("DTDCanAppearOneOrMore")));
      popup.add(new JMenuItem(E2Designer.getResources().getString("DTDCanAppearOptional")));

      // get the menu fontmetrics
      FontMetrics fontMetric=popup.getFontMetrics(popup.getFont());

      int width=0;

      // set the menu call back
        MenuElement [] mItems = popup.getSubElements();
      for(int i=0; i<mItems.length; i++)
      {
            mi = (JMenuItem)mItems[i];
            //get the longest string
            width=max(width,fontMetric.stringWidth(mi.getText()));
            mi.addActionListener(m_menuItemListener);
      }
      // get the dialog
      Component parent=getUltimateParent(this);
      if( parent == null )  // something was wrong?
        return;        // yes return


      MapBrowser browser=(MapBrowser)parent;

      // convert the point from the tree control to the dialog
      Point converted=new Point(SwingUtilities.convertPoint(this,point,parent));

      Rectangle visible=new Rectangle();
      // get the DIALOG area
      browser.computeVisibleRect(visible);

      // get a single line height
      int height=fontMetric.getHeight();
      // calculate the dimensions (the height+4 is the menu item interspace)
      height=(height+4)*4;  // the the height+'4' is an imaginary number

      // so lets see where we are

      // check the bottom
      if(converted.y+height > visible.height )
      {
           //too low fix it
          converted.y=visible.height-height;
      }
      // check the right
      if( converted.x+width > visible.width )
      {
            // too far to the right
           converted.x=visible.width-width;
      }
      // check the left
      if( converted.x< visible.x)
      {
          // too far left
          converted.x=visible.x+10;
      }
      // now display the menu on the dialog
      popup.show(browser, converted.x, converted.y);
  }


  static class MapBrowserTreeDragSourceListener implements DragSourceListener
  {
      ////////////////////////////////////////////////////////////////////////////
   // implementation for DragSourceListener
     public void dragDropEnd(DragSourceDropEvent dsde)
   {
     }
      ////////////////////////////////////////////////////////////////////////////
     // implementation for DragSourceListener
      public void dragEnter(DragSourceDragEvent dsde)
     {
         //          
     }
      ////////////////////////////////////////////////////////////////////////////
     // implementation for DragSourceListener
      public void dragExit(DragSourceEvent dse)
     {
       
     }
      ////////////////////////////////////////////////////////////////////////////
     // implementation for DragSourceListener
      public void dragOver(DragSourceDragEvent dsde)
     {
         DragSourceContext ctx = dsde.getDragSourceContext();
         
         int action = dsde.getDropAction();
         if (action == DnDConstants.ACTION_COPY)
         {
            ctx.setCursor(DragSource.DefaultCopyDrop);
         }
         else if (action == DnDConstants.ACTION_MOVE)
         {
            ctx.setCursor(DragSource.DefaultMoveDrop);
         }
         else
         {
            ctx.setCursor(DragSource.DefaultMoveNoDrop);
         }         
     }
      ////////////////////////////////////////////////////////////////////////////
     // implementation for DragSourceListener
      public void dropActionChanged(DragSourceDragEvent dsde)
     {
     }
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementation for DropTargetListener
  public void dragExit(DropTargetEvent dropTargetEvent)
  {

  }

  public void dragEnter(DropTargetDragEvent dropTargetEvent)
  {
   
   dropTargetEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
  }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for DropTargetListener
   public void dragOver(DropTargetDragEvent dropTargetDragEvent)
   {
      if (dropTargetDragEvent
            .isDataFlavorSupported(MapBrowserTreeNode.MAPPER_XML_TREENODE_FLAVOR)
            || dropTargetDragEvent
                  .isDataFlavorSupported(MapBrowserTreeNode.MAPPER_BACKEND_TREENODE_FLAVOR))
      {
         dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
         if (m_causedDrag != null && m_causedDrag == this)
            dropTargetDragEvent.rejectDrag();
         else
            dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
      }
      else
      {
         dropTargetDragEvent.rejectDrag();
      }
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for DropTargetListener
   public synchronized void drop(DropTargetDropEvent dropTargetDropEvent)
   {
      try
      {
         Transferable tr = dropTargetDropEvent.getTransferable();
         if (tr.isDataFlavorSupported(MapBrowserTreeNode.MAPPER_XML_TREENODE_FLAVOR) ||
             tr.isDataFlavorSupported(MapBrowserTreeNode.MAPPER_BACKEND_TREENODE_FLAVOR))
         {
            Point dropPoint = dropTargetDropEvent.getLocation();
            Object userObject = tr.getTransferData(MapBrowserTreeNode.MAPPER_XML_TREENODE_FLAVOR);
            if(userObject instanceof MapBrowserTreeNode)
            {
               MapBrowserTreeNode sourcenode = (MapBrowserTreeNode) userObject;

               TreePath path = getClosestPathForLocation(dropPoint.x, dropPoint.y);
               if (path != null)
               {
                  MapBrowserTreeNode selection = (MapBrowserTreeNode) path.getLastPathComponent();
                  if (isDragable(selection))
                  {
                     MapBrowserTreeNode targetnode = new MapBrowserTreeNode(selection);
                     boolean bLeftToRight = false;
                     Point dragLoc = m_causedDrag.getLocation();

                     Point dropLoc = getLocation();
                     MapperPropertyDialog dlg = MapperPropertyDialog.getCurrentEditor();
                     dragLoc = SwingUtilities.convertPoint(m_causedDrag, dragLoc, dlg);
                     dropLoc = SwingUtilities.convertPoint(this, dropLoc, dlg);

                     if( dragLoc.x < 0 )
                        dragLoc.x=-dragLoc.x;

                     if (dragLoc.x < dropLoc.x)
                        bLeftToRight = true;

                     if (dlg.editingQuery() == true)
                     {
                        if (bLeftToRight)
                           dlg.addNodesToTable(sourcenode, targetnode);
                        else
                           dlg.addNodesToTable(targetnode, sourcenode);
                     }
                     else
                     {
                        if (bLeftToRight)
                           dlg.addNodesToTable(targetnode, sourcenode);
                        else
                           dlg.addNodesToTable(sourcenode, targetnode);
                     }
                  }
               }
            }
        
            repaint();
            dropTargetDropEvent.getDropTargetContext().dropComplete(true);
         }
         else
         {
            System.err.println ("Rejected");
            dropTargetDropEvent.rejectDrop();
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
         dropTargetDropEvent.rejectDrop();
      }
      catch (UnsupportedFlavorException e)
      {
         e.printStackTrace();
         dropTargetDropEvent.rejectDrop();
      }
   }

   //////////////////////////////////////////////////////////////////////////////
  // implementation for DropTargetListener
  public void dropActionChanged (DropTargetDragEvent dropTargetDragEvent)
  {
  }

   //////////////////////////////////////////////////////////////////////////////
  private DragSource dragSource = DragSource.getDefaultDragSource();
  private static final DragSourceListener dragSourceListener = 
     new MapBrowserTreeDragSourceListener();

  ///////
  private static MapBrowserTree m_causedDrag = null;


  private PopupTriggerListener m_popupTriggerListener;
  private MenuItemListener m_menuItemListener = null;
  private int m_iRepeatAttribute=0;
  private int m_lastSelectedItem=0;
  private boolean m_treeReadOnly=false;
  private boolean m_dtdTreeDirty=false;
  private HashMap visitedNodesMap=null;

}
