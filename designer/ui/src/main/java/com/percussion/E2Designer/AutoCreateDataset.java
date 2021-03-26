/******************************************************************************
 *
 * [ AutoCreateDataset.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.E2Designer.ICustomDropSourceData.DropAction;
import com.percussion.content.IPSMimeContent;
import com.percussion.content.PSContentFactory;
import com.percussion.utils.xml.PSSaxParseException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Iterator;

////////////////////////////////////////////////////////////////////////////////
class AutoCreateDataset implements ICustomDropAction
{
  //////////////////////////////////////////////////////////////////////////////
   // ICustomDropAction interface implementation
   public boolean wantsDrop(UICIdentifier id)
   {
      return PipeFigureFactory.BACKEND_DATATANK_ID == id.getID() ||
            PipeFigureFactory.PAGE_DATATANK_ID == id.getID();
   }

   /**
    * If the source figure is a datatank, then a number of items are created.
    * Within the app frame, a dataset, connected to a standard webpage is created.
    * In addition, the dataset is created with a pipe, mapper, selector and 1 or
    * 2 tanks, depending on the type of tank being dropped. If the dropped tank
    * is a page dt, only the page datatank is created. If the dropped dt is a
    * backend dt, then both backend and page dts are created, with the page dt
    * being created such that it will create a DTD when the app is saved.
    *
    * @param frame an instance of the AppFigureFrame that the source was dropped
    * within
    *
    * @param target this value is ignored
    *
    * @param source a page or backend datatank
    *
    * @param dropLocation the point where the drop occurred, in frame coords
    *
    * @returns <code>true</code> if the source is a desirable type, otherwise
    * <code>false</code> is returned and nothing is done to any parameter.
    */
  //////////////////////////////////////////////////////////////////////////////
   public int customizeDrop(final UIFigureFrame frame,
         @SuppressWarnings("unused") final UIConnectableFigure target,
                                  final UIConnectableFigure source,
                               final Point dropLocation)
   {
      if (wantsDrop(new UICIdentifier(source.getFactoryName(), source.getId())) )// &&
      //  target == null)
      // TODO: temp fix for bug (GBOD-4B5NNP); need to drop table into Resource
      // objects as replacements.
    {
      // assume no valid actions
      boolean actionUpdate = false;
      boolean actionQuery = false;
      boolean actionStatic = false;
      boolean actionImage = false;

        if (PipeFigureFactory.PAGE_DATATANK_ID == source.getId())
      {
        // set valid actions for a page drop
          OSPageDatatank tank = (OSPageDatatank) source.getData();
        if (tank == null )
          return ICustomDropAction.DROP_IGNORED;


        String xmlStr = tank.getFilePath();
        Document xmlDoc = null;

        if (tank.getDocumentType() == OSPageDatatank.SRC_TYPE_XML)
        {
            xmlDoc = getXmlDocument (xmlStr);
            if (xmlDoc == null)
              return ICustomDropAction.DROP_CANCELED;
        }

         actionUpdate = true;
         actionQuery = true;
         actionStatic = tank.isHTML();
      }
      else if (PipeFigureFactory.BACKEND_DATATANK_ID == source.getId())
      {
         // set valid actions for a backend drop
         actionUpdate = true;
         actionQuery = true;
         actionImage = true;
      }
      else
        // this should never happen if wantsDrop works fine
        return ICustomDropAction.DROP_IGNORED;

        // create the action selection menu the user has to choose from
        final JPopupMenu selectAction = new JPopupMenu();
      if (actionQuery)
      {
        JMenuItem menuItem = new JMenuItem(E2Designer.getResources().getString("menuQuery"));
          selectAction.add(menuItem).addActionListener(new ActionListener()
          {
              public void actionPerformed(
                    @SuppressWarnings("unused") ActionEvent event)
             {
                 try
                 {
                    addComponents(frame, source, dropLocation, DropAction.QUERY);
                    hideDropMenu(selectAction);
                 }
                 catch (Exception e)
                 {
                    PSDlgUtil.showError(e);
                 }
             }
          });
      }
      if (actionUpdate)
      {
        JMenuItem menuItem = new JMenuItem(E2Designer.getResources().getString("menuUpdate"));
        selectAction.add(menuItem).addActionListener(new ActionListener()
        {
          public void actionPerformed(
                @SuppressWarnings("unused") ActionEvent event)
          {
             try
             {
                addComponents(frame, source, dropLocation, DropAction.UPDATE);
                hideDropMenu(selectAction);
             }
             catch (Exception e)
             {
                PSDlgUtil.showError(e);
             }
          }
        });
      }
        if (actionStatic)
        {
        JMenuItem menuItem = new JMenuItem(E2Designer.getResources().getString("menuStatic"));
           selectAction.add(menuItem).addActionListener(new ActionListener()
           {
              public void actionPerformed(
                    @SuppressWarnings("unused") ActionEvent event)
              {
                 try
                 {
                    addComponents(frame, source, dropLocation, DropAction.STATIC);
                    hideDropMenu(selectAction);
                 }
                 catch (Exception e)
                 {
                    PSDlgUtil.showError(e);
                 }
              }
           });
      }
      if (actionImage)
      {
        JMenuItem menuItem = new JMenuItem(E2Designer.getResources().getString("menuImage"));
        selectAction.add(menuItem).addActionListener(new ActionListener()
          {
             public void actionPerformed(
                   @SuppressWarnings("unused") ActionEvent event)
             {
                try
                {
                   addComponents(frame, source, dropLocation, DropAction.IMAGE);
                   hideDropMenu(selectAction);
                }
                catch (Exception e)
                {
                   PSDlgUtil.showError(e);
                }
             }
        });
      }

      // fix for JDK bug: JPopupMenu.replacePopup(int)
      Thread worker = new Thread(new Runnable()
      {
        public void run()
        {
           // This is a hack to refresh the frame as dragging of resources from
            // naviation are causing some lines to appear on screen.
           frame.getTheGlassPane().repaint();
          // show the action selection menu
           selectAction.show(frame.getTheGlassPane(), dropLocation.x,
                     dropLocation.y);
        }
      });
      worker.start();

      // indicate that this drop has been used
        return ICustomDropAction.DROP_ACCEPTED;
    }
    else
      return ICustomDropAction.DROP_IGNORED;
   }

   /**
    * This is a workaround for bug# RX-13903. When a dtd file has been dropped
    * on to an application, the popup menu is not disappearing even after
    * clicking on one of the menu items. This started happening after upgrading
    * the Eclipse to version 3.2.2 and JRE to 1.6. The method below just sets
    * the menu visibility to false.
    */
   private void hideDropMenu(JPopupMenu selectAction)
   {
      selectAction.setVisible(false);
   }

   /**
    * Execute generic stuff for all drop actions and the pass control over to
    * the specific handlers.
    *
    * @param frame an instance of UIFigureFrame that the source was dropped within
    * @param source a page or backend datatank
    * @param origin the point where the drop occurred, in frame coords
    * @param action the action the user has choosen
    */
  //////////////////////////////////////////////////////////////////////////////
   private void addComponents(UIFigureFrame frame,
                             UIConnectableFigure source,
                             Point origin,
                             DropAction action)
   {
      // prepare the source data object for this action
      if (!((ICustomDropSourceData) source.getData()).prepareSourceForDrop(action,
         ((ICustomDropSourceData) source.getData()).getFilePath(),null))
      return;

      switch (action)
      {
         case UPDATE:
            addUpdateComponents(frame, source, origin);
            break;

         case QUERY:
            addQueryComponents(frame, source, origin);
            break;

         case STATIC:
            addStaticComponents(frame, source, origin);
            break;

         case IMAGE:
            addImageComponents(frame, source, origin);
            break;
         default:
            throw new UnsupportedOperationException();
      }
   }

   /**
    * Set the requestor default values for the provided dataset figure.
    *
    * @param frame the application figure frame, may not be <code>null</code>
    * and must be an instance of an {@link UIAppFrame}.
    * @param figDataset the dataset figure, may not be <code>null</code>.
    */
   public static void setRequestorDefaults(UIFigureFrame frame,
      UIConnectableFigure figDataset)
   {
      if (!(frame instanceof UIAppFrame))
         throw new IllegalArgumentException();
      
      if (figDataset == null)
         throw new IllegalArgumentException("figDataset may not be null");

      if (figDataset.getId() != AppFigureFactory.DATASET_ID
         && figDataset.getId() != AppFigureFactory.BINARY_RESOURCE_ID)
      {
         throw new IllegalArgumentException();
      }
      
      UIConnectionPoint cp = figDataset.getConnectionPoint(
         UIConnectionPoint.CP_ID_LEFT);
      if (cp == null || cp.getData() == null)
         throw new IllegalArgumentException();

      if (figDataset.getData() instanceof OSDataset)
      {
         OSDataset dataset = (OSDataset) figDataset.getData();
         dataset.defaultRequestorName();
         cp.setData(dataset.getRequestor());
      }

      /*
       * updating the name of the dataset to reflect Request URL. Ideally, this
       * wouldn't need to be done here. It is because the dataset's label is
       * based on the requestor object, which belongs to the connection point.
       */
      figDataset.invalidateLabel();
   }

   /**
    * Add all components for an update action.
    * 
    * @param frame an instance of UIFigureFrame that the source was dropped
    *        within
    * @param source a page or backend datatank
    * @param origin the point where the drop occurred, in frame coords
    */
  //////////////////////////////////////////////////////////////////////////////
   private void addUpdateComponents(UIFigureFrame frame,
                                   UIConnectableFigure source,
                                   Point origin)
   {
    Point location = new Point(origin);

    // create the dataset figure and set its position
    UIConnectableFigure figDataset = frame.add(AppFigureFactory.UPDATE_DATASET);

    // dropping an HTML file creates also an external interface and connects it
    // to the dataset
    if (source.getData() instanceof OSPageDatatank &&
        ((OSPageDatatank) source.getData()).isHTML())
    {
      UIConnectableFigure figStaticpage = frame.add(AppFigureFactory.EXTERNAL_INTERFACE);
      figStaticpage.setLocation(location);
         figDataset.setLocation(Util.getNewFigureLocation(frame, figStaticpage, figDataset, 50));
      ((ICustomDropSourceData) figStaticpage.getData()).prepareSourceForDrop(ICustomDropSourceData.DropAction.UPDATE, ((ICustomDropSourceData) source.getData()).getFilePath(), null);

      // creat a connector and connect dataset and webpage
      UIConnector connector = (UIConnector) frame.add(AppFigureFactory.DIRECTED_CONNECTION);
      // we need to connect these in reverse order or we may get the wrong end of
      // the connector
      Dimension size = figStaticpage.getSize();
         connector.createDynamicConnectionProgrammatic(figDataset.getClosestConnector(figStaticpage.getId(),
            connector.getConnectionConstraint(), new Point(0, size.height / 2)), false);

         size = figDataset.getSize();
         connector.createDynamicConnectionProgrammatic(figStaticpage.getClosestConnector(connector.getId(),
               null, new Point(size.width, size.height / 2 )), true);
    }
    else
      figDataset.setLocation(location);

    // get the figures data object and create an update pipe
    OSDataset dataset = (OSDataset) figDataset.getData();
    createUpdatePipe(dataset, source);

    setRequestorDefaults(frame, figDataset);
  }

   /**
    * Add all components for a query action.
    *
    * @param frame an instance of UIFigureFrame that the source was dropped within
    * @param source a page or backend datatank
    * @param origin the point where the drop occurred, in frame coords
    */
  //////////////////////////////////////////////////////////////////////////////
   private void addQueryComponents(UIFigureFrame frame,
                                  UIConnectableFigure source,
                                  Point origin)
   {
    Point location = new Point(origin);

    // create the dataset figure and set its position
    UIConnectableFigure figDataset = frame.add(AppFigureFactory.QUERY_DATASET);
    figDataset.setLocation(location);

    // create the webpage figure and set its position relative to its dataset
    UIConnectableFigure figWebpage = frame.add(AppFigureFactory.RESULT_PAGE);
    figWebpage.setLocation(Util.getNewFigureLocation(frame, figDataset, figWebpage, 50));
    if ( ((ICustomDropSourceData) figWebpage.getData()).prepareSourceForDrop(ICustomDropSourceData.DropAction.QUERY, ((ICustomDropSourceData) source.getData()).getFilePath(), null) )
    {
       ((OSResultPage)figWebpage.getData()).startReplace(); // reset mod flag
    }

    // creat a connector and connect dataset and webpage
    UIConnector connector = (UIConnector) frame.add(AppFigureFactory.DIRECTED_CONNECTION);
    // we need to connect these in reverse order or we may get the wrong end of
    // the connector
    Dimension size = figWebpage.getSize();
    connector.createDynamicConnectionProgrammatic(figWebpage.getClosestConnector(figDataset.getId(),
          connector.getConnectionConstraint(), new Point(0, size.height / 2)), false);
    size = figDataset.getSize();
    connector.createDynamicConnectionProgrammatic(figDataset.getClosestConnector(connector.getId(),
          null, new Point(size.width, size.height / 2)), true);

    // get the datasets data object and create the query pipe
    OSDataset dataset = (OSDataset) figDataset.getData();
    createQueryPipe(dataset, source);

    setRequestorDefaults(frame, figDataset);
  }

   /**
    * Add all components for a static action.
    *
    * @param frame an instance of UIFigureFrame that the source was dropped within
    * @param source a page or backend datatank
    * @param origin the point where the drop occurred, in frame coords
    */
  //////////////////////////////////////////////////////////////////////////////
   private void addStaticComponents(UIFigureFrame frame,
                                    UIConnectableFigure source,
                                    Point origin)
   {
      Point location = new Point(origin);

      // create the external interface figure and set its position
      UIConnectableFigure figStaticpage = frame.add(AppFigureFactory.EXTERNAL_INTERFACE);
      figStaticpage.setLocation(location);

      ((ICustomDropSourceData) figStaticpage.getData()).prepareSourceForDrop(ICustomDropSourceData.DropAction.STATIC, ((ICustomDropSourceData) source.getData()).getFilePath(), null);
   }

   /**
    * Add all components for an image action.
    *
    * @param frame an instance of UIFigureFrame that the source was dropped within
    * @param source a page or backend datatank
    * @param origin the point where the drop occurred, in frame coords
    */
  //////////////////////////////////////////////////////////////////////////////
   private void addImageComponents( UIFigureFrame frame,
                                    UIConnectableFigure source,
                                    Point origin)
   {
      // create the image dataset and set its location
      UIConnectableFigure image = frame.add(AppFigureFactory.BINARY_RESOURCE);
      image.setLocation(origin);

      // get the datasets data object and create the query pipe
      OSBinaryDataset dataset = (OSBinaryDataset) image.getData();
      createImagePipe(dataset, source);

      setRequestorDefaults(frame, image);
   }

   /**
    * Create an empty update pipe with a BackendTank, PageTank, Mapper and
    * Synchronizer. The source will be used according to its type for one of
    * the tanks.
    *
    * @param dataset the dataset where this pipe will be created in
    * @param source the doped source object
    */
  //////////////////////////////////////////////////////////////////////////////
   private void createUpdatePipe(OSDataset dataset, UIConnectableFigure source)
   {
      try
      {
         OSUpdatePipe pipe = new OSUpdatePipe();
         OSDataMapper mapper = new OSDataMapper();
         OSDataSynchronizer sync = new OSDataSynchronizer();

         pipe.setDataSynchronizer(sync);
         pipe.setDataMapper(mapper);
         dataset.setPipe(pipe);

         if (source.getId() == PipeFigureFactory.PAGE_DATATANK_ID)
         {
            OSPageDatatank pageTank = (OSPageDatatank) source.getData();
            dataset.setPageDataTank(pageTank);
         }
         else
         {
            OSBackendDatatank backendTank = (OSBackendDatatank) source.getData();
            pipe.setBackEndDataTank(backendTank);

            autoCreatePageDatatank(dataset, backendTank);
         }

         mapper.guessMapping(dataset);
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }
   }

  /**
   * Create an empty query pipe with a BackendTank, PageTank, Mapper and
   * Selector. The source will be used according to its type for one of
   * the tanks.
   *
   * @param dataset the dataset where this pipe will be created in, may not be
   * <code>null</code>.
   * @param source the doped source object, may not be <code>null</code>.
   */
   public static void createQueryPipe(OSDataset dataset, 
      UIConnectableFigure source)
   { 
      
      if (dataset == null)
         throw new IllegalArgumentException("dataset may not be null");
      
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      try
      {
         OSQueryPipe pipe = new OSQueryPipe();
         OSDataMapper mapper = new OSDataMapper();
         OSDataSelector selector = new OSDataSelector();
         
         pipe.setDataSelector(selector);
         pipe.setDataMapper(mapper);
         dataset.setPipe(pipe);
         
         if (source.getId() == PipeFigureFactory.PAGE_DATATANK_ID)
         {
            OSPageDatatank pageTank = (OSPageDatatank) source.getData();
            dataset.setPageDataTank(pageTank);
         }
         else if (source.getId() == PipeFigureFactory.BACKEND_DATATANK_ID)
         {
            OSBackendDatatank backendTank = 
               (OSBackendDatatank) source.getData();
            pipe.setBackEndDataTank(backendTank);
            
            autoCreatePageDatatank(dataset, backendTank);
         }

         mapper.guessMapping(dataset);         
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Create an empty image pipe with a BackendTank, Mapper and Selector.
    *
    * @param dataset the dataset where this pipe will be created in
   * @param source the doped source object
    */
  //////////////////////////////////////////////////////////////////////////////
   private void createImagePipe(OSBinaryDataset dataset, UIConnectableFigure source)
   {
      try
      {
         OSBackendDatatank backendTank = (OSBackendDatatank) source.getData();

         dataset.getPipe().setBackEndDataTank(backendTank);
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Create a page datatank from the OSBackendDatatank.
    *
    * @param dataset the dataset which will contain the created page datatank
    * @param backendTank the backend datatank
    */
  //////////////////////////////////////////////////////////////////////////////
   private static void autoCreatePageDatatank(OSDataset dataset,
                                      OSBackendDatatank backendTank)
   {
      try
      {
         OSPageDatatank pageTank = new OSPageDatatank();
         pageTank.autoCreatePageDatatank(backendTank);
         dataset.setPageDataTank(pageTank);
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }
      catch (MalformedURLException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Creates an xml document from a file path of a dropped file. Convenience
    * version of {@link #getXmlDocument(String, boolean, boolean)}, that does
    * not validate by default.
    */
   public static Document getXmlDocument(String filePath)
   {
      return getXmlDocument(filePath, false, false);
   }

   /**
    * Creates an xml document from a file path of a file.  Displays a message
    * dialog describing the problem if any error occurs.
    *
    * @param filePath a file path of the dropped xml file, not <code>null</code>
    * or empty.
    * @param validate if <code>true</code>, the document will be validated
    * as it is parsed, if <code>false</code>, it will only be checked for
    * well-formedness.
    * @param isTemplate This is ignored if <code>validate</code> is not
    * <code>true</code>. If <code>true</code> checks for validity of the xml
    * document as a template, otherwise as dropped file. Templates does not
    * support multi-property simple child field sets.
    *
    * @return xmlDoc a newly created xml document, <CODE>null</CODE> if
    * exception gets thrown or not a valid content editor when checking for
    * validity, otherwise a valid xml document.
    */
   public static Document getXmlDocument(String filePath, boolean validate,
      boolean isTemplate)
   {
      if( filePath == null || 0 == filePath.trim().length() )
         throw new IllegalArgumentException(
            "File path can not be empty or null");

      File droppedFile = new File(filePath);

      // verify that the file is readable and not empty
      String errorMsg = null;
      if (!droppedFile.canRead())
      {
         if (isTemplate)
            errorMsg = E2Designer.getResources().getString("cannotReadTemplate");
         else
            errorMsg = E2Designer.getResources().getString("ErrorCannotReadXml");
      }
      if (0 == droppedFile.length())
      {
         if (isTemplate)
            errorMsg = E2Designer.getResources().getString("emptyTemplate");
         else
            errorMsg = E2Designer.getResources().getString("ErrorEmptyXml");
      }
      if (errorMsg != null)
      {
         PSDlgUtil.showErrorDialog(errorMsg, E2Designer.getResources().getString( "IOErrorTitle" ));
         return null;
      }

      Document xmlDoc = null;
      try
      {
         // Build an xml document from a dropped file
         IPSMimeContent content = PSContentFactory.loadXmlFile(droppedFile);
         Reader in = PSContentFactory.getReader(content);
         xmlDoc = PSXmlDocumentBuilder.createXmlDocument(in, validate);
      }
      catch (IOException ioe)
      {
         PSDlgUtil.showError(ioe, false,
               E2Designer.getResources().getString("IOErrorTitle"));
      }
      catch (PSSaxParseException e)
      {
         // todo: handle
         PSDlgUtil.showErrorDialog(
               processSaxParseErrors(e.getExceptions()),
               E2Designer.getResources().getString("SaxErrorTitle"));
      }
      catch (SAXParseException spe)
      {
         String strSystemId = spe.getSystemId();
         Object[] params =
         {
            spe.getLocalizedMessage(),
            Integer.toString(spe.getLineNumber()),
            Integer.toString(spe.getColumnNumber()),
            (strSystemId == null) ? "none" : strSystemId

         };
         PSDlgUtil.showErrorDialog(
            MessageFormat.format( E2Designer.getResources().getString(
               "SaxParseError"), params ),
            E2Designer.getResources().getString("SaxErrorTitle"));
      }
      catch (SAXException saxe)
      {
         PSDlgUtil.showError(saxe, false,
               E2Designer.getResources().getString("invalidXml"));
      }

      return xmlDoc;
   }

   /**
    * Walks list of SAXParseExceptions and creates a single message detailing
    * all errors.
    *
    * @param errors Iterator over SAXParseExceptions to process.  Assumed not
    * <code>null</code> or empty.
    *
    * @return A single localized message containing details for each exception.
    * Never <code>null</code>.
    */
   private static String processSaxParseErrors(Iterator errors)
   {
      StringBuffer message = new StringBuffer();

      // add header of message
      message.append(E2Designer.getResources().getString(
         "SaxParseErrorHeader"));

      // add detail for each error
      while (errors.hasNext())
      {
         SAXParseException spe = (SAXParseException)errors.next();
         String strSystemId = spe.getSystemId();
         Object[] params =
         {
            spe.getLocalizedMessage(),
            Integer.toString(spe.getLineNumber()),
            Integer.toString(spe.getColumnNumber()),
            (strSystemId == null) ? "none" : strSystemId
         };

         message.append(MessageFormat.format(
            E2Designer.getResources().getString("SaxParseErrorDetail"),
            params));
      }

      return message.toString();
   }
}


