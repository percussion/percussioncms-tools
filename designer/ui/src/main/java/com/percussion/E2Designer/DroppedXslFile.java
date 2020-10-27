/******************************************************************************
 *
 * [ DroppedXslFile.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.Enumeration;

/**
 * This class performs all actions if a drop event for an XSL file or a Webpage
 * is received:
 *    If the file is dropped onto an existing webpage, it is replaced.
 *    If the file is dropped onto a dataset, a webpage is added and connected
 *    If the file is dropped onto empty space or any other resource, a new
 *       webpage is created.
 *    If a Webpage is dropped onto another Webpage, it is replaced.
 *    If a Webpage is dropped onto a dataset, it is automatically moved to the
 *    side and connected.
 */

public class DroppedXslFile implements ICustomDropAction
{
   /**
    * ICustomDropAction interface implementation.  Returns true if the source of
    * the drop is an XSL file or a Webpage.
    *
    * @See ICustomDropAction#wantsDrop(UICIdentifier)
    */
   public boolean wantsDrop(UICIdentifier ID)
   {
      return AppFigureFactory.XSL_FILE_ID == ID.getID()
         || AppFigureFactory.RESULT_PAGE_ID == ID.getID();
   }

   /**
    * ICustomDropAction interface implementation.  Checks {@link
    * #wantsDrop(UICIdentifier) wantsDrop}, then handles dropping onto a
    * dataset, a webpage, empty space, or any other component.
    *
    * @See ICustomDropAction#customizeDrop(UIFigureFrame, UIConnectableFigure,
    *      UIConnectableFigure, Point)
    */
   public int customizeDrop( UIFigureFrame frame,
         UIConnectableFigure target,
         UIConnectableFigure source, Point dropLocation )
   {

      int result = DROP_IGNORED;

      if (wantsDrop(new UICIdentifier(source.getFactoryName(), source.getId())))
      {
         // first handle dropping on a resource or webpage
         if (target != null)
         {
            if (target.getId() == AppFigureFactory.RESULT_PAGE_ID )
            {
              result = dropOntoStyleSheet(frame, target, source, dropLocation);
            }
            else if (target.getId() == AppFigureFactory.DATASET_ID)
            {
              result = dropOntoDataset(frame, target, source, dropLocation);
            }
         }

         /* Handle dropping onto empty space or on a resource that doesn't
          * accept the drop.  If here and result is still DROP_IGNORED, then
          * handle as a drop onto empty space
          */
         if (result == DROP_IGNORED)
         {
            result = addXslComponents( frame, source, dropLocation );
         }
      }


      return result;
   }


   /**
    * Creates the Xsl resource object and adds it to the frame
    *
    * @param frame an instance of UIFigureFrame that the source was dropped
    * within.  must not be <code>null</code>.
    * @param source the drag source, it must be not <code>null</code>, and its
    * data must be an instance of OSFile and not <code>null</code>.
    * Otherwise, nothing is processed.
    * @param origin the point where the drop occurred, in frame coords.  must
    * not be <code>null</code>.
    *
    * @return <CODE>ICustomDropAction.DROP_ACCEPTED</CODE>
    * if the drop is handled, <CODE>ICustomDropAction.DROP_IGNORED</CODE>
    * if not.
    */
   private int addXslComponents(UIFigureFrame frame,
                                 UIConnectableFigure source,
                                 Point origin)
   {

      if (!(source.getData() instanceof OSFile))
         return DROP_IGNORED;

      OSFile sourceFile = (OSFile) source.getData();
      if (sourceFile == null)
         return DROP_IGNORED;

      Point location = new Point(origin);

      // create the result page and set its location
      UIConnectableFigure figWebpage = frame.add(AppFigureFactory.RESULT_PAGE);
      figWebpage.setLocation(location);
      ((ICustomDropSourceData) figWebpage.getData()).prepareSourceForDrop(
         ICustomDropSourceData.DropAction.XSL,
         sourceFile.getFilePath(), null);
      return DROP_ACCEPTED;
   }


   /*
    * This method handles all drop actions onto a web page.  If the source is
    * an Xsl file from the browser or another webpage, a popup menu
    * asks the user to confirm or cancel the operation.  If confirmed, the
    * target webpage is replaced with the source.  If the source is a webpage
    * that is already connected to a resource, the drop is ignored.
    * @param frame the frame where all the action takes place
    * @param target the target figure, its data must be an instance of
    * OSResultPage
    * @param source the drag source, it must be not <code>null</code>, and its
    * data must be an instance of OSFile or OSResultPage and not
    * <code>null</code>. Otherwise, nothing is processed.
    * @param location the drop location
    * @return int <CODE>ICustomDropAction.DROP_ACCEPTED</CODE>
    * if the action is consumed. Otherwise,
    * <CODE>ICustomDropAction.DROP_IGNORED</CODE> will be returned.
    */
   private int dropOntoStyleSheet(final UIFigureFrame frame,
                                  final UIConnectableFigure target,
                                  final UIConnectableFigure source,
                                  final Point location)
   {
      int iDropFlag = DROP_IGNORED;

      // for prompting user
      final JPopupMenu selectAction = new JPopupMenu();

      // be sure we're dropping onto a stylesheet resource
      if (!(target.getData() instanceof OSResultPage))
         return iDropFlag;

      // be sure we're not dropping on self
      if (target == source)
         return iDropFlag;

      final OSResultPage targetpage = (OSResultPage) target.getData();


      // get target file name
      String targetName = targetpage.getFilePath();

      // two cases - source is xsl file from browser, source is another webpage
      if (source.getData() instanceof OSFile)
      {
         final OSFile sourceFile = (OSFile) source.getData();
         if (sourceFile == null)
            return iDropFlag;

         // get source file name
         String sourceName = sourceFile.getFilePath();

         // check if replacing or updating
         String strMenuName = getMenuItemString(sourceName, targetName);

         // add replace/update menu item
         JMenuItem menuItem = new JMenuItem(
            E2Designer.getResources().getString(strMenuName));
         selectAction.add(menuItem).addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent event)
            {
               replaceStyleSheet(sourceFile, targetpage);
            }
         });

         // add cancel menu item - this will do nothing
         selectAction.add(new JMenuItem(
            E2Designer.getResources().getString("menuCancelWebpage")));

      }
      else if (source.getData() instanceof OSResultPage)
      {
         // do nothing if it's connected to something
         UTAppNavigator nav = new UTAppNavigator();
         if (!nav.getAllAttachingFigures(source).isEmpty())
            return iDropFlag;

         final OSResultPage sourcepage = (OSResultPage) source.getData();

         // get source base name
         String sourceName = sourcepage.getFilePath();

         // check if replacing or updating
         String strMenuName = getMenuItemString(sourceName, targetName);

         // add replace menu item
         JMenuItem menuItem = new JMenuItem(
            E2Designer.getResources().getString(strMenuName));
         selectAction.add(menuItem).addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent event)
            {
               replaceWebpage(source, target);
            }
         });

         // add cancel menu item
         menuItem = new JMenuItem(
            E2Designer.getResources().getString("menuMoveWebpage"));
         selectAction.add(menuItem).addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent event)
            {
               moveToTargetFrame(source, target, location);
            }
         });

         // add menu cancel event listener
         selectAction.addPopupMenuListener(new PopupMenuListener()
         {
            public void popupMenuCanceled(PopupMenuEvent e){}
            public void popupMenuWillBecomeVisible(PopupMenuEvent e){}

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
            {
               /* Should be using popupMenuCanceled to handle this, but there
                * is a bug in the vm and this is never called: bug 4234793
                * developer.java.sun.com/developer/bugParade/bugs/4234793.html
                * so using this method instead.  Thus this method is always
                * called before processing the user choice, but will not do
                * any harm - if the source is from a different window, it is
                * "moved" to the target window before we proceed with the user
                * action.
                */

               moveToTargetFrame(source, target, location);

            }
         });
      }
      else
         return iDropFlag;





      /* display it, we use a worker thread because we are in the middle of a
         drop */
      Thread worker = new Thread(new Runnable()
      {
        public void run()
        {
          // show the action selection menu
          selectAction.show(frame.getTheGlassPane(), location.x, location.y);
        }
      });
      worker.start();

      iDropFlag = DROP_ACCEPTED;

      return iDropFlag;
   }


   /**
    * Handles dropping an xsl file or a webpage onto a resource.  Creates a
    * new webpage if necessary and connects it to the target.
    * Assumes that all parameter checks are already done within the calling
    * function.
    *
    * @param frame the frame where all the action takes place
    * @param target the target figure
    * @param source the drag source
    * @param location the drop location
    * @return int <CODE>ICustomDropAction.DROP_ACCEPTED</CODE> if the target
    * is updated with the file. Otherwise, <CODE>ICustomDropAction.DROP_IGNORED
    * </CODE> will be returned.
    */
   private int dropOntoDataset(final UIFigureFrame frame,
                             final UIConnectableFigure target,
                             final UIConnectableFigure source,
                             @SuppressWarnings("unused")
                           final Point location)
   {

      int iDropFlag = DROP_IGNORED;

      UIConnectableFigure figWebpage;


      if (AppFigureFactory.XSL_FILE_ID == source.getId())
      {
         //create the web page from the xsl file
         if (!(source.getData() instanceof OSFile))
            return iDropFlag;

         OSFile sourceFile = (OSFile) source.getData();
         if (sourceFile == null)
            return iDropFlag;

         // create the new webpage and connect it to the target dataset
         figWebpage = frame.add(AppFigureFactory.RESULT_PAGE);
         if ( !((ICustomDropSourceData)
            figWebpage.getData()).prepareSourceForDrop(
            ICustomDropSourceData.DropAction.XSL, sourceFile.getFilePath(), null) )
         {
            /* since the data preparation for the webpage figure failed, remove
             * it from the frame
             */
            figWebpage.remove();
            return iDropFlag;
         }
         else
         {
            ((OSResultPage)figWebpage.getData()).startReplace();
         }
      }
      else
      {
         // we already have a webpage, just use it
         figWebpage = source;

         // be sure it's not already connected to the target
         UTAppNavigator nav = new UTAppNavigator();
         Enumeration e = nav.getTargetResultPages(target).elements();
         while (e.hasMoreElements())
         {
            UIConnectableFigure tempFig = (UIConnectableFigure)e.nextElement();
            if (tempFig == figWebpage)
               return iDropFlag;
         }

         // see if we are moving it from another frame
         Container sourceParent = source.getParent();
         Container targetParent = target.getParent();
         if (sourceParent != targetParent)
         {
            // need to add the figure to this frame or it just gets lost
            targetParent.add(source, 0);
         }

      }

      // set the new location
      figWebpage.setLocation(Util.getNewFigureLocation(frame, target,
         figWebpage, 50));

      // now connect the webpage
      UIConnector connector =
         (UIConnector) frame.add(AppFigureFactory.DIRECTED_CONNECTION);
      /* we need to connect these in reverse order or we may get the wrong end
       * of the connector
       */
      Dimension size = figWebpage.getSize();
      connector.createDynamicConnectionProgrammatic(
         figWebpage.getClosestConnector(target.getId(),
          connector.getConnectionConstraint(), new Point(0, size.height / 2)),
          false);
      size = target.getSize();
      connector.createDynamicConnectionProgrammatic(
         target.getClosestConnector(figWebpage.getId(),
         null, new Point(size.width, size.height / 2)), true);

      iDropFlag = DROP_ACCEPTED;

      return iDropFlag;
   }

   /**
    * Replaces the stylesheet of a webpage.  Assumes objects were validated
    * by the calling method.
    * @param sourceFile the source xsl file object
    * @param webpage the webpage whose stylesheet is being replaced
    */
   private void replaceStyleSheet(OSFile sourceFile, OSResultPage webpage)
   {
      // replace stylesheet
      webpage.startReplace();
      if ( webpage.prepareSourceForDrop(ICustomDropSourceData.DropAction.XSL,
                                  sourceFile.getFilePath(),
                                  null) )
      {
         // acknowledge the user that the drop was successful
         String msgFormat = E2Designer.getResources().getString(
            "replaceResultPage");
         E2Designer.getApp().getMainFrame().setStatusMessage(
            MessageFormat.format(msgFormat, new Object[]{sourceFile.getFilePath()}));
      }

   }

   /**
    * Replaces the target webpage with the source.   Assumes objects were
    * validated by the calling method.
    * @param the source figure, should be a UIC whose data object is an
    * OSResultPage
    * @param the target figure, should be a UIC whose data object is an
    * OSResultPage
    */
   private void replaceWebpage(UIConnectableFigure source,
      UIConnectableFigure target)
   {
      // swap the data
      target.setData(source.getData());

      // update the ui figure
      target.invalidateLabel();

      // remove the source
      source.remove();

   }

   /**
    * checks the base names of the two paths supplied and determines what
    * menu item to add.
    * @param sourcePath the full path of the source file.  May not be
    * <code>null</code> or empty.
    * @param targetPath the full path of the target file.  May not be
    * <code>null</code> or empty.
    * @return the resource string to use when adding a menu item
    */
   private String getMenuItemString(String sourcePath, String targetPath)
   {
      if ((sourcePath == null) || (sourcePath.length() == 0))
         throw new IllegalArgumentException("sourcePath must not be null or empty");
      String sourceName = getBaseName(sourcePath);

      if ((targetPath == null) || (targetPath.length() == 0))
         throw new IllegalArgumentException("targetPath must not be null or empty");
      String targetName = getBaseName(targetPath);

      // check if replacing or updating
      String strMenuName;
      if (sourceName.equals(targetName))
         strMenuName = "menuUpdateWebpage";
      else
         strMenuName = "menuReplaceWebpage";

      return strMenuName;
   }

   /**
    * Get basename of file passed in
    * @param sourcePath the full path of a file.  May not be
    * <code>null</code> or empty.
    * @return the basename
    */
   private String getBaseName(String sourcePath)
   {
      if ((sourcePath == null) || (sourcePath.length() == 0))
         throw new IllegalArgumentException("sourcePath must not be null or empty");

      String baseName = sourcePath.substring(
         sourcePath.lastIndexOf(File.separator));
      baseName = baseName.substring(0, baseName.lastIndexOf('.'));
      return baseName;
   }

   /**
    * Handles source coming from a different app window.
    * If so, performs a "move" so the source webpage is not lost if cancelled,
    * since it is removed from the source frame as soon as the mouse is
    * released, but before the menu selection is made.  In this case we always
    * move it to the target frame, and then handle the action from there.
    * Figure is moved to the drop location regardless of the source frame.
    * @param the source webpage
    * @param the target webpage
    */
   private void moveToTargetFrame(UIConnectableFigure source,
      UIConnectableFigure target, Point location)
   {
      // see if moving from another frame
      Container sourceParent = source.getParent();
      Container targetParent = target.getParent();

      /* need to add the figure to this frame as a move
       * or else it is removed from the source frame
       * and never added anywhere, so it just vanishes!
       */
      if (sourceParent != targetParent)
      {
         targetParent.add(source, 0);
      }

      // now move the figure to the drop location
      source.setLocation(location);
   }

}
