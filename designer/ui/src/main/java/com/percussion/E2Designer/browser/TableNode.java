/******************************************************************************
 *
 * [ TableNode.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.DragInfo;
import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.FigureCreationException;
import com.percussion.E2Designer.FigureFactoryManager;
import com.percussion.E2Designer.FigureTransfer;
import com.percussion.E2Designer.OSBackendDatatank;
import com.percussion.E2Designer.OSBackendTable;
import com.percussion.E2Designer.PipeFigureFactory;
import com.percussion.E2Designer.UICIdentifier;
import com.percussion.E2Designer.UIConnectableFigure;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.datatransfer.Transferable;

/**
 * A table node is designed for entries in the SQL browser that are represented
 * as datatanks in the process view.
 * <p>
 * A table node supports drag and drop and has a context menu to display the
 * properties of the table.
 */
public class TableNode extends DefaultBrowserNode
{
   // constructors
   public TableNode(Object userObject)
   {
      super(userObject);
   }

   /**
   * This node has a menu, so <code>true</code> is returned.
   */
   public boolean hasMenu( )
   {
      return false;
   }

   /**
   * Creates a menu with a single 'Properties' Action.
   */
   public JPopupMenu getContextMenu( )
   {
      return null;
   }

   /**
    * This node supports dragging, so <code>true</code> is returned.
    */
   public boolean isDraggable( )
   {
      return true;
   }

   /**
    * Creates a datatank object, including the cataloger, that can be dropped
    * onto a process view window. The cataloger is included so the cataloging
    * doesn't have to take place until the drop occurs. It would become very
    * annoying if the drag initiation had to wait for a remote column catalog
    * to occur.
    */
   public Transferable getDragDropObject( )
   {

      try
      {
         return createTransfer();
      }
      catch ( FigureCreationException e )
      {
         System.out.println("Could not create UIConnectableFigure");
         return null;
      }
      catch(IllegalArgumentException illarg)
      {
         System.out.println("Could not create UIConnectableFigure");
         return null;
      }
      catch(ClassNotFoundException classnot)
      {
         System.out.println("Could not create UIConnectableFigure");
         return null;
      }

   }

   /**
    * Creates {@link Transferable} from the node.
    */
   private Transferable createTransfer()
         throws FigureCreationException, ClassNotFoundException
   {
      return createTransfer(getStrDataSource());
   }

   /**
    * Finds data source name.
    */
   private String getStrDataSource()
   {
      TreeNode[] naNodes = this.getPath();
      if(naNodes == null)
         return null;

//       System.out.println("path length = "+naNodes.length);

//       for(int i=1; i<naNodes.length; i++)
//       System.out.println("Node name ="+((DefaultBrowserNode)naNodes[i]).getDisplayName());

      IHierarchyConstraints constraints = null;
      // server node is always the  root
      if(naNodes[0] instanceof ServerNode)
      {
         // get the constraints object
         constraints = ((ServerNode)naNodes[0]).getConstraints();
      }
      else
      {
         throw new RuntimeException("Root is not a server node");
      }

      final String strDatasource;
      if(constraints instanceof SQLHierarchyConstraints)
      {
         int indexDs = ((SQLHierarchyConstraints)constraints).
            getIndexOfNodeType(SQLHierarchyConstraints.NT_DATASOURCE_OBJ);

         if(naNodes[indexDs] instanceof DefaultBrowserNode) 
         {
            // get the datasource name
            DefaultBrowserNode node = (DefaultBrowserNode)naNodes[indexDs];
            if (node.getUserObject() instanceof ICatalogEntry)
            {
               strDatasource = 
                  ((ICatalogEntry)node.getUserObject()).getInternalName();
            }
            else
            {
               strDatasource = null;
            }
         }
         else
         {
            strDatasource = null;
         }
         
         if (strDatasource == null)
         {
            throw new RuntimeException("Could not get datasource node");
         }
      }
      else
      {
         throw new RuntimeException("Server Constraints object is not an " +
               "instance of SQLHierarchyConstraints");
      }
      return strDatasource;
   }

   /**
    * Creates transferable from this node using provided data source name. 
    */
   public Transferable createTransfer(final String strDatasource)
         throws FigureCreationException, ClassNotFoundException
   {
      //Create a UIConnectableFigure object
      final FigureFactoryManager ffm = FigureFactoryManager.getFactoryManager();
      if(ffm == null)
      {
         System.out.println("Null Factory manager");
         return null;
      }

      final PipeFigureFactory pipeFigFac = (PipeFigureFactory) ffm.getFactory(
            "com.percussion.E2Designer.PipeFigureFactory");
      if(pipeFigFac == null)
      {
         System.out.println("FigurFactoryManager getFactory method returned"+
         " null PipeFigureFactory");
         return null;
      }

      final UIConnectableFigure uicFigure = pipeFigFac.createFigure(
            PipeFigureFactory.BACKEND_DATATANK);

      final Object o = uicFigure.getData();
      if(o == null)
      {
         System.out.println("UIConnectableFigure getData returned null object");
         return null;
      }
      
      final OSBackendDatatank osBackendTank = o instanceof OSBackendDatatank
            ? (OSBackendDatatank) o : null;

      if(osBackendTank == null)
      {
         System.out.println("OSBackendDatatank is null");
         return null;
      }

      OSBackendTable backendTable = new OSBackendTable();
      backendTable.setDataSource(strDatasource);
      backendTable.setTable(getInternalName());
      //for now set the Alias same as the Table name
      backendTable.setAlias(getInternalName());
      backendTable.setReadOnly(true);
      PSCollection tables =
         new PSCollection("com.percussion.design.objectstore.PSBackEndTable");
      tables.add(backendTable);

      osBackendTank.setTables(tables);

      //construct the FigureTransfer object
      //constructor needs the UIConnectableFigure, and DragInfo
      //DragInfo constructor needs UIConnectableFigure, figID, point, and E2 server name
      int id = uicFigure.getId();
      Point pOffset = new Point(0,0); // point from the top left corner of the component

      String strE2Server = E2Designer.getApp().getMainFrame().getE2Server();

      UICIdentifier uicId = new UICIdentifier(uicFigure.getFactoryName(), id);

      final DragInfo dInfo = new DragInfo(uicFigure, pOffset, uicId, strE2Server,
            uicFigure.getDragInfoProperties(), uicFigure.getDragImage());

      //construct the FigureTransfer object
      //constructor needs the UIConnectableFigure, and DragInfo
      FigureTransfer ft = new FigureTransfer(uicFigure, dInfo);
      return ft;
   }
}

