/*******************************************************************************
 * $Id: MapBrowserTable.java 1.17 2001/11/19 21:21:26Z JaySeletz Exp $
 * COPYRIGHT (c) 2000 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * Version Labels  : $Name: Pre_CEEditorUI $
 *
 * Locked By       : $Locker: $
 *
 * Revision History:
 * $Log: MapBrowserTable.java $
 * Revision 1.17  2001/11/19 21:21:26Z  JaySeletz
 * Fix for Rx-01-11-0040: default to PSHtmlParameter when dropping
 * nodes on mapper from an html form.
 * Revision 1.16  2001/06/11 21:18:00Z  JedMcGraw
 * Used new method getOriginalPathString instead of getPathString.
 * Revision 1.15  2001/05/03 21:02:32Z  JaySeletz
 * Removed the functionality to enable preinstalled udfs.
 * Revision 1.14  2000/06/28 00:13:28Z  AlexTeng
 * Added code to support new UDF and extension model.
 *
 * Revision 1.13  2000/05/17 12:23:13Z  paulhoward
 * Bug fix: XML Element was not showing in the value selector when
 * editing mapper entries for an Update resource. Modified methods
 * to pass in the page tank.
 *
 * Revision 1.12  2000/02/09 19:18:39Z  candidorodriguez
 * tmp fix to allow rebuild ( using new extensions )
 *
 * Revision 1.11  1999/08/19 18:22:53Z  martingenhart
 * start using SwingWorker
 * popup UDF dialog correct while droping it on mapper table
 * Revision 1.10  1999/08/17 02:08:10  AlexTeng
 * It now catches PSIllegalArgumentException for DTCgiVar objects.
 *
 * Revision 1.9  1999/08/14 19:28:14  martingenhart
 * several bugfixes, mapper changes to support CGI, etc.
 * cache all catalogs
 * Revision 1.8  1999/08/02 17:48:16  AlexTeng
 * Added TableModelEvent fired method call to notify tablemodel
 * change to the TableModelListener in the MapperPropertyDialog.
 *
 * Revision 1.7  1999/07/30 16:59:51  martingenhart
 * avoid exceptions if conditionals not shown
 * Revision 1.6  1999/06/22 15:57:12  martingenhart
 * fixed a few mapper bugs
 * Revision 1.5  1999/06/21 18:47:02  martingenhart
 * use UTJTable to create generic behavier for tables in a central place
 * Revision 1.4  1999/06/15 16:02:11  martingenhart
 * fix mapper property dialog
 * Revision 1.3  1999/06/10 22:02:29  martingenhart
 * fix create UDFs
 * Revision 1.2  1999/06/09 23:36:47  martingenhart
 * new mapper functionality added
 * Revision 1.1  1999/06/03 21:41:56  martingenhart
 * Initial revision
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSCgiVariable;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSUserContext;
import com.percussion.design.objectstore.PSXmlField;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
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
public class MapBrowserTable extends UTJTable implements DropTargetListener
{
   /**
    * constructor.
    */
   /////////////////////////////////////////////////////////////////////////////
   public MapBrowserTable(MapperTableModel model, PSUdfSet udfSet)
   {
        super(model);
      m_udfSet = udfSet;
      m_dropTarget = new DropTarget (this, this);
   }


   /**
    * constructor.
    */
   /////////////////////////////////////////////////////////////////////////////
   public MapBrowserTable(MapperTableModel model)
   {
        super(model);
      m_dropTarget = new DropTarget (this, this);
   }


   /**
    * @param udfSet Gives the browser table access to the udfset for
    * modification.
    */
   public void setUdfSet( PSUdfSet udfSet )
   {
      m_udfSet = udfSet;
   }

   /**
    * Set the provided node at given position.
    *
    * @param node the node to set
    * @param row the row index
    * @param column the column index
    */
   //////////////////////////////////////////////////////////////////////////////
   public void setNode(MapBrowserTreeNode node, int row, int column)
   {
      try {
         if (node.isBackend())
         {
            // Reprocess the path string into the right format for 
            String dropvalue = 
               PSBackEndColumn.translatePathToDisplay(node.getPathString());
            setValueAt(dropvalue, row, column);
         }
         else if (node.isXml())
         {
            PSXmlField variable = new PSXmlField(node.getOriginalPathString());
            setValueAt(variable, row, column);
         }
         else if (node.isForm())
         {
            PSHtmlParameter variable = new PSHtmlParameter(node.getPathString());
            setValueAt(variable, row, column);
         }
         else if (node.isUdf())
         {
            OSExtensionCall call = node.getUdfCall();
            setValueAt(call, row, column);
         }
         else if (node.isCgi())
         {
            PSCgiVariable variable = new PSCgiVariable(node.getPathString());
            setValueAt(variable, row, column);
         }
         else if (node.isUserContext())
         {
            PSUserContext userContext = new PSUserContext(node.getPathString());
            setValueAt(userContext, row, column);
         }
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }
   }


   /**
    * Refresh the UDF dilog settings.
    *
    * @param set the actual UDF set
    * @param udf the actual UDF call
    * @param backendDatatank the backend data
    */
   /////////////////////////////////////////////////////////////////////////////
   public void refreshUdfDialog(PSUdfSet set, OSExtensionCall udf,
                               OSBackendDatatank backendDatatank)
   {
      m_udfDialog.refreshDialog(set, udf, backendDatatank);
   }


   /**
    * Refresh the UDF dilog settings.
    *
    * @param set the actual UDF set
    * @param udf the actual UDF call
    * @param pageTank the page tank data
    */
   /////////////////////////////////////////////////////////////////////////////
   public void refreshUdfDialog(PSUdfSet set, OSExtensionCall udf,
                               OSPageDatatank pageTank)
   {
      m_udfDialog.refreshDialog(set, udf, pageTank );
   }


   /**
    * Refresh the UDF dilog settings.
    *
    * @param set the actual UDF set.
    */
   /////////////////////////////////////////////////////////////////////////////
   public void refreshUdfDialog(PSUdfSet set )
   {
      m_udfDialog.refreshDialog( set );
   }

  //////////////////////////////////////////////////////////////////////////////
  // implementation for DropTargetListener
  public void dragEnter(DropTargetDragEvent dropTargetDragEvent)
  {
    dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
  }

   /////////////////////////////////////////////////////////////////////////////
  // implementation for DropTargetListener
  public void dragExit(DropTargetEvent dropTargetEvent) {}

   //////////////////////////////////////////////////////////////////////////////
  // implementation for DropTargetListener
  public void dragOver(DropTargetDragEvent dropTargetDragEvent)
  {
    if (dropTargetDragEvent.isDataFlavorSupported(MapBrowserTreeNode.MAPPER_XML_TREENODE_FLAVOR) ||
        dropTargetDragEvent.isDataFlavorSupported(MapBrowserTreeNode.MAPPER_BACKEND_TREENODE_FLAVOR))
    {
      Point dropPoint = dropTargetDragEvent.getLocation();
      int row = rowAtPoint(dropPoint);
      int column = columnAtPoint(dropPoint);

      MapperTableModel model = (MapperTableModel) this.getModel();
      if (column == MapperTableModel.CONDITIONALS)
        dropTargetDragEvent.rejectDrag();
      else
      {
        DataFlavor[] flavors = dropTargetDragEvent.getCurrentDataFlavors();
        String flavorName = flavors[0].getHumanPresentableName();
        if (flavorName.equals(MapBrowserTreeNode.MAPPER_XML_TREENODE_FLAVOR.getHumanPresentableName()) &&
            this.getColumnName(column).equals(model.getResources().getString("xml")))
          dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        else if (flavorName.equals(MapBrowserTreeNode.MAPPER_BACKEND_TREENODE_FLAVOR.getHumanPresentableName()) &&
                 this.getColumnName(column).equals(model.getResources().getString("backend")))
          dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        else
          dropTargetDragEvent.rejectDrag();
      }
    }
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
      if (tr.isDataFlavorSupported(MapBrowserTreeNode.MAPPER_XML_TREENODE_FLAVOR) ||
          tr.isDataFlavorSupported(MapBrowserTreeNode.MAPPER_BACKEND_TREENODE_FLAVOR))
      {
        dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        Object userObject = tr.getTransferData(MapBrowserTreeNode.MAPPER_XML_TREENODE_FLAVOR);
        MapBrowserTreeNode node = (MapBrowserTreeNode) userObject;
        Point dropPoint = dropTargetDropEvent.getLocation();
        int row = rowAtPoint(dropPoint);
        int column = columnAtPoint(dropPoint);

        if (node.isUdf())
        {
          final int iRow = row;
          final int iCol = column;
          final MapBrowserTreeNode udfNode = (MapBrowserTreeNode) userObject;

          dropTargetDropEvent.dropComplete(true);
          SwingWorker worker = new SwingWorker()
          {
            public Object construct()
            {
              try
              {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                  public void run()
                  {
                    OSExtensionCall call = udfNode.getUdfCall();
                    m_udfDialog.onEdit(call, false);
                    m_udfDialog.center();
                    m_udfDialog.setVisible(true);

                    if (!m_udfDialog.isCancelHit())
                    {
                      udfNode.setUdfCall( m_udfDialog.getUdfCall() );
                      setNode(udfNode, iRow, iCol);
                      MapBrowserTable.this.repaint();
                    }
                  }
                });
               }
               catch (InterruptedException e)
              {
                return "interrupted";
              }
               catch (java.lang.reflect.InvocationTargetException e)
              {
                return "failed";
               }

              return "done";
            }

            public void finished()
            {
            }
          };
        }
        else
        {
          setNode(node, row, column);
          repaint();

          dropTargetDropEvent.dropComplete(true);
        }
      }
      else
      {
        System.err.println ("Rejected");
        dropTargetDropEvent.rejectDrop();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      dropTargetDropEvent.dropComplete(false);
    }
  }

   //////////////////////////////////////////////////////////////////////////////
  // implementation for DropTargetListener
  public void dropActionChanged (DropTargetDragEvent dropTargetDragEvent) {}

   /////////////////////////////////////////////////////////////////////////////
   // private storage
   private DropTarget m_dropTarget = null;
   private FormulaPropertyDialog   m_udfDialog = new FormulaPropertyDialog();
   private PSUdfSet m_udfSet = null;
}
