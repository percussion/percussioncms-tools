/*******************************************************************************
 * $Id: UTPipeNavigator.java 1.5 2000/06/28 00:24:14Z AlexTeng Release $
 * COPYRIGHT (c) 2000 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted 
 * work including confidential and proprietary information of Percussion.
 *
 * Version Labels  : $Name: Pre_CEEditorUI RX_40_REL 20010618_3_5 20001027_3_0 20000724_2_0 $
 *
 * Locked By       : $Locker: $
 *
 * Revision History:
 * $Log: UTPipeNavigator.java $
 * Revision 1.5  2000/06/28 00:24:14Z  AlexTeng
 * Made getAllFiguresAttached() work.
 * 
 * Revision 1.4  2000/01/14 18:48:27Z  AlexTeng
 * Added implementation for HTML Parameter implementation.
 * 
 * Revision 1.3  1999/06/24 22:42:27Z  vimalagrawal
 * added getMapper method
 * 
 * Revision 1.2  1999/04/28 21:40:44  martingenhart
 * added getPipe function
 * Revision 1.1  1999/04/22 15:59:42  martingenhart
 * Initial revision
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.util.Enumeration;
import java.util.Vector;

/**
 * This class provides helper functionality to navigate through all objects
 * within a pipe.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTPipeNavigator
{
   /**
    * Construct the helper object.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTPipeNavigator() {}

   /**
    * Get FIRST pipe found the sourceFigure is attached to. Returns null
    * if not found, or sourceFigure is not attached.
    *
    * @param      source         the figure we are starting the search from
    * @return   UIFigure      a reference to the requested figure.
    */
  //////////////////////////////////////////////////////////////////////////////
   public UIFigure getPipe(UIFigure source) throws IllegalArgumentException
   {
        return getFigure(source, PipeFigureFactory.PIPE_ID);
   }

   /**
   * Get FIRST backend tank of pipe the sourceFigure is attached to. Returns null
   * if not found, or sourceFigure is not attached.
   *
   * @param      source         the figure we are starting the search from
   * @return   UIFigure      a reference to the requested figure.
    */
  //////////////////////////////////////////////////////////////////////////////
   public UIFigure getBackendTank(UIFigure source)
         throws IllegalArgumentException
   {
        return getFigure(source, PipeFigureFactory.BACKEND_DATATANK_ID);
   }

   /**
   * Get FIRST page tank of pipe the sourceFigure is attached to. Returns null
   * if not found, or sourceFigure is not attached.
   *
   * @param      source         the figure we are starting the search from
   * @return   UIFigure      a reference to the requested figure.
    */
  //////////////////////////////////////////////////////////////////////////////
   public UIFigure getPageTank(UIFigure source) throws IllegalArgumentException
   {
        return getFigure(source, PipeFigureFactory.PAGE_DATATANK_ID);
   }

   /**
   * Get the mapper of pipe the sourceFigure is attached to. Returns null
   * if not found, or sourceFigure is not attached.
   *
   * @param      source         the figure we are starting the search from
   * @return   UIFigure      a reference to the requested figure.
    */
  //////////////////////////////////////////////////////////////////////////////
   public UIFigure getMapper(UIFigure source) throws IllegalArgumentException
   {
        return getFigure(source, PipeFigureFactory.MAPPER_ID);
   }

  /**
   * Get the mapper of pipe the sourceFigure is attached to. Returns null
   * if not found, or sourceFigure is not attached.
   *
   * @param      source         the figure we are starting the search from
   * @return   UIFigure      a reference to the requested figure.
    */
  //////////////////////////////////////////////////////////////////////////////
   public UIFigure getSelector(UIFigure source) throws IllegalArgumentException
   {
        return getFigure(source, PipeFigureFactory.SELECTOR_ID);
   }

  /**
   * Get all figures attached to pipe (sourceFigure).
   * Returns null if no figures are found on pipe.
   *
   * @return Vector A list of figures attached to pipeFigure.
    */
  //////////////////////////////////////////////////////////////////////////////
   public Vector getAllFiguresAttached(UIFigure pipeFigure)
         throws IllegalArgumentException
   {
      Vector vFigureList = null;
        try
      {
          // the source must be a connectable figure
          if ( null != pipeFigure ||
              pipeFigure instanceof UIConnectableFigure &&
              pipeFigure.getId() == PipeFigureFactory.PIPE_ID)
          {
            vFigureList = new Vector(5);
            // loop through all connection points until we found the first of
            // requested type
            //Enumeration enum = ((UIConnectableFigure)pipeFigure).getDynamicConnections();
            Enumeration e = ((UIConnectableFigure)pipeFigure).getConnectionPoints();
            while (e.hasMoreElements())
            {
               UIRigidConnectionPoint cp = (UIRigidConnectionPoint)e.nextElement();
               if ( cp.getAttached() != null )
                  vFigureList.addElement( (UIFigure)cp.getAttached() );
            }
          }
         else
            throw new IllegalArgumentException("UIConnectableFigure (PipeFigure) expected");
      }
      catch (Exception e)
      {
          e.printStackTrace();
      }
      return vFigureList;
   }


   /**
   * Get the FIRST requested figure from pipe the sourceFigure is attached to.
   * Returns null if not found, or sourceFigure is not attached.
   *
   * @param      source      the figure we are starting the search from
   * @param      findId      the figure id we are looking for
   * @return   UIFigure   a reference to the requested figure.
    */
  //////////////////////////////////////////////////////////////////////////////
   private UIFigure getFigure(UIFigure source, int findId)
         throws IllegalArgumentException
   {
        try
      {
         if ( source == null )
            return null;

          // the source must be a connectable figure
          if (source instanceof UIConnectableFigure)
          {
            // get pipe first
            UIConnectableFigure pipe = getPipe((UIConnectableFigure) source);
              if (findId == PipeFigureFactory.PIPE_ID)
                 return pipe;

            if (pipe != null)
            {
               // loop through all connection points until we found the first of
                 // requested type
                  Enumeration e = pipe.getConnectionPoints();
               while (e.hasMoreElements())
               {
                  UIRigidConnectionPoint cp = (UIRigidConnectionPoint) e.nextElement();
                    if ((cp.getAttached() != null) && (cp.getAttached().getId() == findId))
                        return cp.getAttached();
                }
            }
          }
         else
            throw new IllegalArgumentException("UIConnectableFigure expected");
      }
      catch (Exception e)
      {
          e.printStackTrace();
      }

      // not found
      return null;
   }

   /**
   * Walk up the tree and return the pipe if found. If the passed object is not
   * attached to a pipe we will return null.
   *
   * @param      source                        the figure we are starting the search from
   * @return   UIConnectableFigure      a reference to the pipe
    */
  //////////////////////////////////////////////////////////////////////////////
   private UIConnectableFigure getPipe(UIConnectableFigure source)
   {
        // this is already a pipe
        if (source.getId() == PipeFigureFactory.PIPE_ID)
          return source;

      // walk up the chain til we found a pipe
      Enumeration e = source.getDynamicConnections();
      while (e.hasMoreElements() && (source.getId() != PipeFigureFactory.PIPE_ID))
      {
           UIConnectionPoint cp = (UIConnectionPoint) e.nextElement();
           if (cp.getOwner() != null)
         {
            if (cp.getOwner().getId() == PipeFigureFactory.PIPE_ID)
               return cp.getOwner();
            else
                 return getPipe(cp.getOwner());
         }
      }

      return null;
   }
}

