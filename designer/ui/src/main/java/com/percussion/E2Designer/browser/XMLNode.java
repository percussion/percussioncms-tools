/*[ XMLNode.java ]*************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.AppFigureFactory;
import com.percussion.E2Designer.DragInfo;
import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.FigureCreationException;
import com.percussion.E2Designer.FigureFactoryManager;
import com.percussion.E2Designer.FigureTransfer;
import com.percussion.E2Designer.OSApplicationFile;
import com.percussion.E2Designer.OSFile;
import com.percussion.E2Designer.OSPageDatatank;
import com.percussion.E2Designer.PipeFigureFactory;
import com.percussion.E2Designer.UICIdentifier;
import com.percussion.E2Designer.UIConnectableFigure;
import com.percussion.E2Designer.Util;
import com.percussion.error.PSIllegalArgumentException;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class XMLNode extends DefaultBrowserNode
{
   // constructors
   public XMLNode( Object userObject, String strPath )
   {
      super( userObject );
      m_strPath = strPath;
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
      if (m_CatalogEntry == null)
            return null;

      if (!setFactoryAndFigureId(m_CatalogEntry.getInternalName()))
            return null;
      return createTransfer(m_CatalogEntry.getInternalName());
      }
      catch (FigureCreationException e)
      {
         e.printStackTrace();
         return null;
      }
      catch (PSIllegalArgumentException e)
      {
         e.printStackTrace();
         return null;
      }
      catch (MalformedURLException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   /**
    * Creates {@link Transferable} from the node.
    */
   public Transferable createTransfer(String fileName)
         throws MalformedURLException, FigureCreationException,
         PSIllegalArgumentException
   {
      UIConnectableFigure uic = null;
      if (m_pipeFactory != null)
        uic = createPageDatatank(fileName);
      else if (m_appFactory != null && m_figureType == AppFigureFactory.APPLICATION_FILE)
         uic = createApplicationFile(fileName);
      else if (m_appFactory != null && m_figureType == AppFigureFactory.XSL_FILE)
        uic = createXslFile(fileName);
      else
        return null;

         // construct the FigureTransfer object
         int id = uic.getId();
      // point from the top left corner of the component
         Point pOffset = new Point(0,0);
         UICIdentifier uicId = new UICIdentifier(uic.getFactoryName(), id);

         DragInfo info = new DragInfo(uic, pOffset, uicId,
                                   getServer(), uic.getDragInfoProperties(), uic.getDragImage());
         FigureTransfer transfer = new FigureTransfer(uic, info);

         return transfer;
   }

   String getServer()
   {
      return E2Designer.getApp().getMainFrame().getE2Server();
   }

   /**
    * Get the full path of the catalog entry.
    *
   * @return String the path
    */
   //////////////////////////////////////////////////////////////////////////////
   public String getFullPath()
   {
      return m_strPath;
   }

   /**
    * Set the figure factory type and the figure ID which should be ceated.
    *
    * @param fileName the file name including its file type (the file extension).
  * @return boolean true if successful, false otherwise
  * @throws IllegalArgumentException if fileName is null or no file extension
    */
   //////////////////////////////////////////////////////////////////////////////
   public boolean setFactoryAndFigureId(String fileName)
   {
    m_appFactory = null;
    m_pipeFactory = null;
    m_figureType = "";

    String strTemp = fileName.toLowerCase();
    if (Util.isSplitable(fileName.toLowerCase()) ||
        strTemp.indexOf(".xml") != -1 ||
        strTemp.indexOf(".dtd") != -1)
    {
      m_pipeFactory = (PipeFigureFactory) FigureFactoryManager.getFactoryManager().getFactory("com.percussion.E2Designer.PipeFigureFactory");
      if (m_pipeFactory == null)
        return false;

      m_figureType = PipeFigureFactory.PAGE_DATATANK;
      return true;
    }
    else if (strTemp.indexOf(".xsl") != -1)
    {
      m_appFactory = (AppFigureFactory) FigureFactoryManager.getFactoryManager().getFactory("com.percussion.E2Designer.AppFigureFactory");
      if (m_appFactory == null)
        return false;

      m_figureType = AppFigureFactory.XSL_FILE;
      return true;
    }
   else
   {
     m_appFactory = (AppFigureFactory) FigureFactoryManager.getFactoryManager().getFactory("com.percussion.E2Designer.AppFigureFactory");
      if (m_appFactory == null)
        return false;

      m_figureType = AppFigureFactory.APPLICATION_FILE;
      return true;
   }
   }

   /**
    * Create a page datatank and initialize all data nessecary for the drop
   * action.
   *
   * @return UIConnectableFigure the created figure
    */
   //////////////////////////////////////////////////////////////////////////////
   private UIConnectableFigure createPageDatatank(String fileName) throws MalformedURLException,
                                                          FigureCreationException,
                                                          PSIllegalArgumentException
   {
    UIConnectableFigure uic = m_pipeFactory.createFigure(m_figureType);
    OSPageDatatank pageTank = (OSPageDatatank) uic.getData();

    // Don't put in a leading slash because we want the path relative to E2 root.
    pageTank.setSchemaSource(new URL("file:" + fileName));

    // store full path, its needed to create the XML document in the drop action
    pageTank.setFilePath(getFullPath() + File.separator + fileName);
    pageTank.setDocumentType(fileName);

    // indicate that we used the local file system to create it
    pageTank.setUsedLocalFileSystem();

    return uic;
  }


   private UIConnectableFigure createApplicationFile(String fileName)   throws FigureCreationException
   {
      UIConnectableFigure uic = m_appFactory.createFigure(m_figureType);
      uic.setId(m_appFactory.DRAG_APPLICATION_FILE_ID);
      OSApplicationFile appFile = (OSApplicationFile)uic.getData();
      // store full path, its needed to create the XSL document in the drop action
       appFile.setFilePath(getFullPath() + File.separator + fileName);
      appFile.setInternalName(fileName);

       // indicate that we used the local file system to create it
       appFile.setUsedLocalFileSystem();


       return(uic);
   }

   /**
    * Creates a UIConnectableFigure of the correct figure type and sets this
    * node's file path info into the UIC's data object
    *
    * @return a UIConnectableFigure which contains a data object of type
    * <code>OSFile</code>
    *
    * @throws FigureCreationException if any errors occur while creating the figure.
    */
   private UIConnectableFigure createXslFile(String fileName)   throws FigureCreationException
   {
      UIConnectableFigure uic = m_appFactory.createFigure(m_figureType);
      OSFile localFile = (OSFile)uic.getData();
      // store full path, its needed to create the XSL document in the drop action
      localFile.setFilePath(getFullPath() + File.separator + fileName);

      return(uic);
   }

   //////////////////////////////////////////////////////////////////////////////
   private String m_strPath = null;
  private AppFigureFactory m_appFactory = null;
  private PipeFigureFactory m_pipeFactory = null;
  private String m_figureType = "";
}

