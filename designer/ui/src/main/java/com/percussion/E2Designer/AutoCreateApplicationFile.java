/*[ AutoCreateApplicationFile.java ]*******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.awt.*;
import java.io.ByteArrayInputStream;


////////////////////////////////////////////////////////////////////////////////
class AutoCreateApplicationFile implements ICustomDropAction
{
  //////////////////////////////////////////////////////////////////////////////
   // ICustomDropAction interface implementation
   public boolean wantsDrop(UICIdentifier id)
   {
      if (AppFigureFactory.DRAG_APPLICATION_FILE_ID == id.getID())
      {
         return true;
      }

      return false;
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
                               final UIConnectableFigure target,
                                  final UIConnectableFigure source,
                               final Point dropLocation)
   {
      if (wantsDrop(new UICIdentifier(source.getFactoryName(), source.getId())))
    {
    //      System.out.println("Customize");

      addComponents(frame,
                      source,
                      dropLocation,
                      0); //action is always the same

      // indicate that this drop has been used
        return ICustomDropAction.DROP_ACCEPTED;
    }
    else
      return ICustomDropAction.DROP_IGNORED;
   }

   /**
    * Execute generic stuff for all drop actions and the pass control over to
   * the specific handlers.
    *
    * @param frame an instance of UIFigureFrame that the source was dropped within
    * @param source a page or backend datatank
    * @param point the point where the drop occurred, in frame coords
   * @param int the action the user has choosen
    */
  //////////////////////////////////////////////////////////////////////////////
   private void addComponents(UIFigureFrame frame,
                             UIConnectableFigure source,
                             Point origin,
                             int action)
   {
    // prepare the source data object for this action
//         System.out.println("add comp");


       Point location = new Point(origin);

       // create the external interface figure and set its position
       UIConnectableFigure figStaticpage = frame.add(AppFigureFactory.APPLICATION_FILE);
       figStaticpage.setLocation(location);
      OSApplicationFile appfile = (OSApplicationFile)source.getData();
      ((OSApplicationFile)figStaticpage.getData()).setInternalName(appfile.getInternalName());
      ((ICustomDropSourceData) figStaticpage.getData()).prepareSourceForDrop(ICustomDropSourceData.DropAction.STATIC, ((ICustomDropSourceData) source.getData()).getFilePath(), null);
  }



  //////////////////////////////////////////////////////////////////////////////
  private ByteArrayInputStream m_inputStreamForHtmlConverter = null;
}


