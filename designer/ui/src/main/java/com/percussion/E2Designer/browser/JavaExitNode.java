/*[ JavaExitNode.java ]********************************************************
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
import com.percussion.E2Designer.OSExitCallSet;
import com.percussion.E2Designer.UICIdentifier;
import com.percussion.E2Designer.UIConnectableFigure;
import com.percussion.extension.IPSExtensionDef;

import java.awt.*;
import java.awt.datatransfer.Transferable;

public class JavaExitNode extends  DefaultBrowserNode
{
   /**
    * Locally used identifier for OSExitCallSet.EXT_TYPE_REQUEST_PRE_PROC
    * interface.
    */
   public static final int REQUEST_PRE_PROC_EXT = 1;
   
   /**
    * Locally used identifier for OSExitCallSet.EXT_TYPE_RESULT_DOC_PROC
    * interface.
    */
   public static final int RESULT_DOC_PROC_EXT = 2;

  /**
  * constructs an node with the java exit
  *
  *@param exit the object to be stored
  *
  *@param userObject the name of the exit ( display )
  *
  *@param path object path
  */
  public JavaExitNode(IPSExtensionDef exit,Object userObject,
        @SuppressWarnings("unused") String path)
  {
    super(userObject);
    m_javaExit=exit;
  }
  /**
  *@return the stored exit
  */
  public IPSExtensionDef getExit()
  {
    return(m_javaExit);
  }
  /**
  *@return the type of the stored exit
  *        they can be  IPSExtensionDef.EXT_TYPE_RESULT_DOC_PROC,
  *                     IPSExtensionDef.EXT_TYPE_REQUEST_PRE_PROC or
  *                     zero if no exit is stored
  *
  */
   public int getJavaExitType()
   {
      return m_javaExitType;
   }


   /**
    * @param type new type id of the exit node.
    *        they can be  IPSExtensionDef.EXT_TYPE_RESULT_DOC_PROC,
    *                     IPSExtensionDef.EXT_TYPE_REQUEST_PRE_PROC or
    *                     zero if no exit is stored.
    */
   public void setJavaExitType(int type)
   {
      m_javaExitType = type;
   }


   /**
    *@return <code> true </code> always
    */
   @Override
   public boolean isDraggable()
   {
     return(true);
  }

   /**
    *@return create an transferable object
    */
    @Override
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
   }
    
    /**
     * Creates {@link Transferable} from the node.
     */
   public Transferable createTransfer() throws FigureCreationException
   {
      //Create a UIConnectableFigure object
      FigureFactoryManager ffm = null;
      ffm = ffm.getFactoryManager();
      if(ffm == null)
      {
         System.out.println("Null Factory manager");
         return null;
      }

      // get the app factory
        AppFigureFactory figFac = (AppFigureFactory)ffm.getFactory(
         "com.percussion.E2Designer.AppFigureFactory");
      if (figFac == null)
      {
         System.out.println(
            "FigurFactoryManager getFactory method returned null AppFigureFactory");
         return null;
      }

      // create the object
      UIConnectableFigure uicFigure = figFac.createFigure(getJavaExitTypeText());
      Object o=uicFigure.getData();

       OSExitCallSet osSet = null;

      // get the set
      if( o instanceof OSExitCallSet)
         osSet = (OSExitCallSet)o;

      if(osSet == null)
      {
         System.out.println("OSExitCallSet is null");
         return null;
      }

      // set the exit
      String interfaceType;
      if ( REQUEST_PRE_PROC_EXT == m_javaExitType )
         interfaceType = OSExitCallSet.EXT_TYPE_REQUEST_PRE_PROC;
      else
         interfaceType = OSExitCallSet.EXT_TYPE_RESULT_DOC_PROC;
      osSet.setExtension( m_javaExit, interfaceType );
      // set the id
      uicFigure.setId(getJavaExitID());

      int id = uicFigure.getId();
      Point pOffset = new Point(0,0); // point from the top left corner of the component

      String strE2Server = E2Designer.getApp().getMainFrame().getE2Server();

      UICIdentifier uicId = new UICIdentifier(uicFigure.getFactoryName(), id);

      DragInfo dInfo = new DragInfo(uicFigure, pOffset, uicId, strE2Server,
         uicFigure.getDragInfoProperties(), uicFigure.getDragImage());

      //construct the FigureTransfer object
      //constructor needs the UIConnectableFigure, and DragInfo
      return new FigureTransfer(uicFigure, dInfo);
   }

  /**
  *@return the stored exit type
  *        they can be  AppFigureFactory.PRE_JAVA_EXIT_ID,
  *                     AppFigureFactory.POST_JAVA_EXIT_ID or
  *                     AppFigureFactory.JAVA_EXIT_ID
  */
   private int  getJavaExitID()
   {
      int iRet=AppFigureFactory.JAVA_EXIT_ID;
      int type=getJavaExitType();
      switch(type)
      {
         case RESULT_DOC_PROC_EXT:
            iRet = AppFigureFactory.POST_JAVA_EXIT_ID;
            break;
         case REQUEST_PRE_PROC_EXT:
            iRet=AppFigureFactory.PRE_JAVA_EXIT_ID;
            break;
      }
      return(iRet);
   }


   /**
    *@return the stored exit type in an string format
    *        they can be AppFigureFactory.JAVA_EXIT,
    *                    AppFigureFactory.POST_JAVA_EXIT or
    *                    AppFigureFactory.PRE_JAVA_EXIT
    */
   private String getJavaExitTypeText()
   {
      String name=AppFigureFactory.JAVA_EXIT;
      int type=getJavaExitType();
      switch(type)
      {
         case RESULT_DOC_PROC_EXT:
            name=AppFigureFactory.POST_JAVA_EXIT;
            break;
         case REQUEST_PRE_PROC_EXT:
            name=AppFigureFactory.PRE_JAVA_EXIT;
            break;
      }
      return(name);
   }

   /**
    *the exit to be stored
    */
   private IPSExtensionDef m_javaExit = null;

   private int m_javaExitType = 0;
}
