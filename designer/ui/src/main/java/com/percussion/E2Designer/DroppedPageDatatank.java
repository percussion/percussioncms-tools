/******************************************************************************
 *
 * [ DroppedPageDatatank.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.E2Designer.ICustomDropSourceData.DropAction;
import com.percussion.design.objectstore.PSPageDataTank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Vector;


/**
 * This class performs all actions if a page drop event is received.
 */
public class DroppedPageDatatank implements ICustomDropAction
{
   /**
    * This member handles all drop action onto an dataset figure.
    *
    * @param frame the frame where all the action takes place
    * @param target the target figure which must be a dataset
    * @param source the drag source, its data must be an instance of
    *               OSPageDatatank and not null
    * @param location the drop location
    */
   ////////////////////////////////////////////////////////////////////////////
   private int dropOntoDataset(final UIFigureFrame frame,
                                  final UIConnectableFigure target,
                                  final UIConnectableFigure source,
                                  final Point location)
   {
      if ( target.getId() != AppFigureFactory.DATASET_ID )
         return DROP_IGNORED;

      if (!(source.getData() instanceof OSPageDatatank))
         return DROP_IGNORED;

      OSPageDatatank pageTank = (OSPageDatatank) source.getData();
      if (pageTank == null)
         return DROP_IGNORED;

      // we need to know the target datasets pipe type to popup the correct menu
      boolean isQuery = true;
      OSDataset dataset = (OSDataset) target.getData();
      if (dataset.getPipe() instanceof OSUpdatePipe)
         isQuery = false;

      if (pageTank.isHTML())
      {
         // get current DTD name
         String dtdName = null;
         String fileName = Util.stripPath(((ICustomDropSourceData)
            source.getData()).getFilePath(), "");

         /* there may be a case where the pageTank of the dataset is removed
         when a user wants to drop an html file onto the dataset. if the
         pagetank is not set, then the dataset will simply add stylesheet/add
         static page,regardless of the name of the file dropped.*/
         if ( null != dataset.getPageDataTank())
         {
            String filePath =
               ((OSPageDatatank)dataset.getPageDataTank()).getFilePath();

            if(filePath != null) {

               /*gets the original file name as it may be converted to a valid
               XML file name*/
               dtdName = Util.stripPath(filePath,"");

               //warn user that dropping HTML onto XML will recreate the DTD
               E2DesignerResources res = E2Designer.getResources();
               String dlgMsg = res.getString("DtdReplaceWarningBody");
               String dlgTitle = res.getString("Warning");

               int result =
                  PSDlgUtil.showConfirmDialog(dlgMsg, dlgTitle,
                                                JOptionPane.OK_CANCEL_OPTION,
                                                JOptionPane.WARNING_MESSAGE);

               if (result != JOptionPane.OK_OPTION)
                  return DROP_CANCELED;

            }
         }
         else
         {
            dtdName = fileName;
         }

         // the source was created from an HTML file
         final JPopupMenu selectAction = new JPopupMenu();
         JMenuItem menuItem = null;
         if ( null != dataset.getPageDataTank() )
         {
            if(fileName != null && dtdName != null)
            {
               if (isQuery)
               {
                  if (fileName.toLowerCase().equals(dtdName.toLowerCase()))
                  {
                     replaceQueryDTD(target, source, dtdName);
                     E2Designer.getApp().getMainFrame().setStatusMessage
                        ("Replaced Query DTD/XSL " + dtdName + " with " +
                        ((ICustomDropSourceData) source.getData()).getFilePath());
                     return DROP_ACCEPTED;
                  }
               }
               else
               {
                  if (fileName.toLowerCase().equals(dtdName.toLowerCase()))
                  {
                     replaceUpdateDTD(target, source, dtdName);
                     E2Designer.getApp().getMainFrame().setStatusMessage
                        ("Replaced Update DTD " + dtdName + " with " +
                        ((ICustomDropSourceData) source.getData()).getFilePath());
                     return DROP_ACCEPTED;
                  }
               }
            }
         }
         System.out.println(dtdName);
         final String finalDtdName = dtdName;
         menuItem =
         new JMenuItem(E2Designer.getResources().getString("menuAddStylesheet"));
         selectAction.add(menuItem).addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent event)
            {
               addStylesheet(frame, target, source, finalDtdName);
               E2Designer.getApp().getMainFrame().setStatusMessage
                  ("Added stylesheet " +
                  ((ICustomDropSourceData) source.getData()).getFilePath());
            }
         });
         menuItem =
            new JMenuItem(E2Designer.getResources().getString("menuAddHtmlPage"));
         selectAction.add(menuItem).addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent event)
            {
               addHtmlPage(frame, target, source);
               E2Designer.getApp().getMainFrame().setStatusMessage
                  ("Added static page " +
                  ((ICustomDropSourceData) source.getData()).getFilePath());
            }
         });

         // fix for JDK bug: JPopupMenu.replacePopup(int)
         Thread worker = new Thread(new Runnable()
         {
            public void run()
            {
               // show the action selection menu
               selectAction.show(frame.getTheGlassPane(), location.x, location.y);
            }
         });
         worker.start();

         return DROP_ACCEPTED;
      }

      return DROP_IGNORED;
   }

   /**
    * This member handles all drop action onto an external interface figure.
    *
    * @param frame the frame where all the action takes place
    * @param target the target figure which must be a dataset
    * @param source the drag source, its data must be an instance of
    *               OSPageDatatank and not null
    * @param location the drop location
    */
   ////////////////////////////////////////////////////////////////////////////
   private int dropOntoExternalInterface(
         final UIConnectableFigure target, final UIConnectableFigure source)
   {
      if (!target.getType().equals(AppFigureFactory.EXTERNAL_INTERFACE))
         return DROP_IGNORED;

      if (!(source.getData() instanceof OSPageDatatank))
         return DROP_IGNORED;

      OSPageDatatank pageTank = (OSPageDatatank) source.getData();
      if (pageTank == null)
         return DROP_IGNORED;

      if (pageTank.isHTML())
      {
         // replace the HTML page
         OSExternalInterface externalInterface =
               (OSExternalInterface) target.getData();
         externalInterface.startReplace();
         externalInterface.prepareSourceForDrop(
               DropAction.STATIC,
               ((ICustomDropSourceData) source.getData()).getFilePath(),
               null);

         E2Designer.getApp().getMainFrame().setStatusMessage(
               "Replaced static page with "
               + ((ICustomDropSourceData) source.getData()).getFilePath());
         return DROP_ACCEPTED;
      }

      return DROP_IGNORED;
   }

   /**
    * This member handles all drop action onto an external interface figure.
    *
    * @param frame the frame where all the action takes place
    * @param target the target figure which must be a dataset
    * @param source the drag source, its data must be an instance of
    *               OSPageDatatank and not null
    * @param location the drop location
    * @return int <CODE>ICustomDropAction.DROP_ACCEPTED</CODE> if result page
    * preparation was okay. Otherwise, <CODE>ICustomDropAction.DROP_IGNORED
    * </CODE> will be returned.
    */
   ////////////////////////////////////////////////////////////////////////////
   private int dropOntoWebpage(
         final UIConnectableFigure target, final UIConnectableFigure source)
   {
      int iDropFlag = DROP_IGNORED;
      if (!target.getType().equals(AppFigureFactory.RESULT_PAGE))
         return iDropFlag;

      if (!(source.getData() instanceof OSPageDatatank))
         return iDropFlag;

      OSPageDatatank pageTank = (OSPageDatatank) source.getData();
      if (pageTank == null)
         return iDropFlag;

      if (pageTank.isHTML())
      {
         UTAppNavigator nav = new UTAppNavigator();
         Vector datasets = nav.getSourceDatasets(target);
         String root = null;
         if (datasets.size() > 0)
         {
            // for now just take the first one found
            // todo: check that all source datasets have the same root and warn
            // the user if not
            UIConnectableFigure fig = (UIConnectableFigure) datasets.get(0);
            OSDataset data = (OSDataset) fig.getData();
            root = data.getRequestor().getRequestPage();
         }

         // replace stylesheet
         OSResultPage webpage = (OSResultPage) target.getData();
         if ( webpage.prepareSourceForDrop(ICustomDropSourceData.DropAction.XSL,
               ((ICustomDropSourceData) source.getData()).getFilePath(),
               root))
         {
            // acknowledge the user that the drop was successful
            E2Designer.getApp().getMainFrame().setStatusMessage(
                  "Replaced result page with "
                  + ((ICustomDropSourceData) source.getData()).getFilePath());
         }

         webpage.startReplace();
         iDropFlag = DROP_ACCEPTED;
      }

      return iDropFlag;
   }

   /**
    * Replace the DTD and add a stylesheet. Creates a DTD using the splitter and
    * replaces the DTD within the target figure. Creates a new webpage using the
    * new stylesheet and connects it to the target. Existing webpages remain
    * untouched.
    * Assumes that all parameter checks are already done within the calling
    * function.
    *
    * @param frame the frame where all the action takes place
    * @param target the target figure
    * @param source the drag source
    * @param origin the drop location
    * @param rootName the root name to be used
    */
   ///////////////////////////////////////////////////////////////////////////
   private void replaceQueryDTD(final UIConnectableFigure target,
                               final UIConnectableFigure source,
                               final String rootName)
   {
      OSDataset dataset = (OSDataset) target.getData();

      // prepare the source for dataset (replace DTD)
      ((ICustomDropSourceData) source.getData()).prepareSourceForDrop(
            ICustomDropSourceData.DropAction.QUERY,
            ((ICustomDropSourceData) source.getData()).getFilePath(),
            rootName );

      // prepare the source for result page(replace XSL)
      UTAppNavigator nav = new UTAppNavigator();
      Vector resultPages = nav.getTargetResultPages( target );
      for (int i = 0; i < resultPages.size(); i++)
      {
         UIConnectableFigure resultPage =
            (UIConnectableFigure) resultPages.get( i );
         OSResultPage resultPageData = (OSResultPage) resultPage.getData();
         URL urlStyleSheet = resultPageData.getStyleSheet();

         if (null != urlStyleSheet) // if style sheet is default; it is null
         {
            String fileName = Util.stripPath( urlStyleSheet.getFile(), "" );
            if (fileName.equals( rootName ))
            {
               resultPageData.startReplace();
               resultPageData.prepareSourceForDrop(
                  ICustomDropSourceData.DropAction.QUERY,
                  ((ICustomDropSourceData) source.getData()).getFilePath(),
                  rootName );
               break;
            }
         }
      }

      // replace the existing pipe with the new one
      dataset.setPageDataTank( (PSPageDataTank) source.getData() );

      // NOTE:[AT] temp removal; will be put back post-2.0;
      /*
      if (dataset.getPipe().getDataMapper() instanceof OSDataMapper)
      {
         OSDataMapper mapper = (OSDataMapper) dataset.getPipe().getDataMapper();
         mapper.guessMapping(dataset);
      }
      */
   }

   /**
    * Replace the DTD and add an HTML page. Creates a DTD using the splitter and
    * replaces the DTD within the target figure. Creates a new external
    * interface and connects it to the target. Existing external interfaces
    * remain untouched.
    * Assumes that all parameter checks are already done within the calling
    * function.
    *
    * @param frame the frame where all the action takes place
    * @param target the target figure
    * @param source the drag source
    * @param origin the drop location
    * @param rootName the root name to be used
    */
   ////////////////////////////////////////////////////////////////////////////
   private void replaceUpdateDTD(final UIConnectableFigure target,
                                final UIConnectableFigure source,
                                final String rootName)
   {
      // prepare the source
      ((ICustomDropSourceData) source.getData()).prepareSourceForDrop(
            ICustomDropSourceData.DropAction.UPDATE,
            ((ICustomDropSourceData) source.getData()).getFilePath(),
            rootName );

      // replace the existing pipe with the new one
      OSDataset dataset = (OSDataset) target.getData();
      dataset.setPageDataTank( (PSPageDataTank) source.getData() );

      // NOTE:[AT] temp removal; will be put back post-2.0;
      /*
      if (dataset.getPipe().getDataMapper() instanceof OSDataMapper)
      {
         OSDataMapper mapper = (OSDataMapper) dataset.getPipe().getDataMapper();
         mapper.guessMapping(dataset);
      }
      */
   }

   /**
    * Creates a new stylesheet, then adds a new webpage and connects it to the
    * target.
    * Assumes that all parameter checks are already done within the calling
    * function.
    *
    * @param frame the frame where all the action takes place
    * @param target the target figure
    * @param source the drag source
    * @param location the drop location
    */
    ///////////////////////////////////////////////////////////////////////////
   private void addStylesheet(final UIFigureFrame frame,
                             final UIConnectableFigure target,
                             final UIConnectableFigure source,
                             final String rootName)
   {
      // create the new webpage and connect it to the target dataset
      UIConnectableFigure figWebpage = frame.add(AppFigureFactory.RESULT_PAGE);
      figWebpage.setLocation(
            Util.getNewFigureLocation(frame, target, figWebpage, 50));

      if ( !((ICustomDropSourceData) figWebpage.getData()).prepareSourceForDrop(
               ICustomDropSourceData.DropAction.QUERY,
               ((ICustomDropSourceData) source.getData()).getFilePath(),
               rootName) )
      {
         // since the data preparation for the webpage figure failed, remove it
         // from the frame
         figWebpage.remove();
         return;
      }
      else
      {
         ((OSResultPage)figWebpage.getData()).startReplace();
      }

      UIConnector connector =
            (UIConnector) frame.add(AppFigureFactory.DIRECTED_CONNECTION);
      // we need to connect these in reverse order or we may get the wrong end
      // of the connector
      Dimension size = figWebpage.getSize();
      connector.createDynamicConnectionProgrammatic(
            figWebpage.getClosestConnector(target.getId(),
            connector.getConnectionConstraint(), new Point(0, size.height / 2)),
            false);
      size = target.getSize();
      connector.createDynamicConnectionProgrammatic(
            target.getClosestConnector(figWebpage.getId(),
            null, new Point(size.width, size.height / 2)), true);
   }

   /**
    * Adds a new external interface and connects it to the target.
    * Assumes that all parameter checks are already done within the calling
    * function.
    *
    * @param frame the frame where all the action takes place
    * @param target the target figure
    * @param source the drag source
    * @param location the drop location
    */
   ////////////////////////////////////////////////////////////////////////////
   private void addHtmlPage(final UIFigureFrame frame,
                           final UIConnectableFigure target,
                           final UIConnectableFigure source)
   {
      UIConnectableFigure figStaticpage =
            frame.add(AppFigureFactory.EXTERNAL_INTERFACE);
      figStaticpage.setLocation(
            Util.getNewFigureLocation(frame, target, figStaticpage, -50));
      ((ICustomDropSourceData) figStaticpage.getData()).prepareSourceForDrop(
            ICustomDropSourceData.DropAction.STATIC,
            ((ICustomDropSourceData) source.getData()).getFilePath(),
            null);

      // creat a connector and connect dataset and webpage
      UIConnector connector =
            (UIConnector) frame.add(AppFigureFactory.DIRECTED_CONNECTION);
      // we need to connect these in reverse order or we may get the wrong end
      // of the connector
      Dimension size = figStaticpage.getSize();
      connector.createDynamicConnectionProgrammatic(
            target.getClosestConnector(figStaticpage.getId(),
            connector.getConnectionConstraint(),
            new Point(0, size.height / 2)), false);

      size = target.getSize();
      connector.createDynamicConnectionProgrammatic(
            figStaticpage.getClosestConnector(connector.getId(),
            null, new Point(size.width, size.height / 2 )), true);
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for ICustomDropAction
   public boolean wantsDrop(UICIdentifier id)
   {
      return PipeFigureFactory.PAGE_DATATANK_ID == id.getID();
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for ICustomDropAction
   public int customizeDrop(final UIFigureFrame frame,
                              final UIConnectableFigure target,
                              final UIConnectableFigure source,
                              final Point location)
   {
      if (wantsDrop(new UICIdentifier(source.getFactoryName(), source.getId())) &&
        target != null)
      {
         // we do not allow drops if the target figure is currently edited
         if (target.isEdited())
            return DROP_ACCEPTED;

         if (target.getType().equals( PipeFigureFactory.QUERY_PIPE ))
         {
            // handle page datatank drop onto a query pipe
            ((ICustomDropSourceData) source.getData()).prepareSourceForDrop(
                  ICustomDropSourceData.DropAction.QUERY,
                  ((ICustomDropSourceData) source.getData()).getFilePath(),
                  null);
            return DROP_ACCEPTED;
         }
         else if (target.getType().equals( PipeFigureFactory.UPDATE_PIPE ))
         {
            // handle page datatank drop onto an update pipe
            ((ICustomDropSourceData) source.getData()).prepareSourceForDrop(
                  ICustomDropSourceData.DropAction.UPDATE,
                  ((ICustomDropSourceData) source.getData()).getFilePath(),
                  null);
            return DROP_ACCEPTED;
         }
         else if ( target.getId() == AppFigureFactory.DATASET_ID )
         {
            return dropOntoDataset(frame, target, source, location);
         }
         else if ( target.getId() == AppFigureFactory.BINARY_RESOURCE_ID )
         {
            return DROP_ACCEPTED;
         }
         else if (target.getId() == AppFigureFactory.EXTERNAL_INTERFACE_ID )
         {
            return dropOntoExternalInterface(target, source);
         }
         else if (target.getId() == AppFigureFactory.RESULT_PAGE_ID )
         {
            return dropOntoWebpage(target, source);
         }
         else if (target.getId() == AppFigureFactory.DIRECTED_CONNECTION_ID )
         {
            return DROP_ACCEPTED;
         }
      }

      return DROP_IGNORED;
   }
}
