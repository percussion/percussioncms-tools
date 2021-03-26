/******************************************************************************
 *
 * [ DroppedBackendDatatank.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSBackEndDataTank;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.util.PSCollection;

import java.awt.*;
import java.util.HashMap;

/**
 * This class performs all actions if a backend drop event is received.
 */
////////////////////////////////////////////////////////////////////////////////
public class DroppedBackendDatatank implements ICustomDropAction
{
  //////////////////////////////////////////////////////////////////////////////
  // implementation for ICustomDropAction
   public boolean wantsDrop(UICIdentifier id)
   {
      return PipeFigureFactory.BACKEND_DATATANK_ID == id.getID();
   }

  //////////////////////////////////////////////////////////////////////////////
  /**
   * implementation for ICustomDropAction, nows it adds the tanks into the
   * pipe.
   *
   * @param frame The frame which is receiving the drop; it can either be an
   * UIAppFrame or an UIPipeFrame.
   * @param target A UIConnectableFigure that represents the pipe figure, which
   * can be either query or update; this figure receives the drop.
   * @param source A dervied figure class from UIConnectableFigure,
   * a UIConnectableFigureBEDatatank object; representing the figure that is
   * being dropped.
   * @param dropLocation The Point representation of where <CODE>source</CODE>
   * was dropped.
   * @return int Either <CODE>ICustomDropAction.DROP_ACCEPTED</CODE> for
   * accepting the drop, <CODE>ICustomDropAction.DROP_REJECTED</CODE> for
   * rejecting the drop, or
   * <CODE>ICustomDropAction.DROP_ACCEPTED_AND_ATTACH_OBJ<CODE> for accept and
   * the data object dropped will handle the attachment.
   */
   public int customizeDrop( UIFigureFrame frame,
                            UIConnectableFigure target,
                            UIConnectableFigure source,
                            @SuppressWarnings("unused") Point dropLocation)
   {
      if (wantsDrop(new UICIdentifier(source.getFactoryName(), source.getId())) &&
        target != null)
      {
         int iRet=ICustomDropAction.DROP_ACCEPTED_AND_ATTACH_OBJ;

         Cursor restoreCursor = E2Designer.getApp().getMainFrame().getCursor();
           Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
         E2Designer.getApp().getMainFrame().setCursor(waitCursor);

         boolean bAdd=false;
         boolean bPrepareSource = false;
         UTPipeNavigator navigator = new UTPipeNavigator();

         UIConnectableFigure pageTank =
            (UIConnectableFigure) navigator.getPageTank(target);
         OSPageDatatank pageData = null;
         if ( null != pageTank )
            pageData = (OSPageDatatank) pageTank.getData();

         UIConnectableFigure Tank =
            (UIConnectableFigure) navigator.getBackendTank(target);

         if( Tank != null )
         {
            boolean bSetJoins = true;    // if true then auto guess the joins
            if (target.getType().equals(PipeFigureFactory.UPDATE_PIPE))
               bSetJoins = false;
            else
            {
               // get the user config
               bSetJoins = UserConfig.getConfig().getBoolean(
                  E2Designer.getResources().getString("JOIN_OPTION"),true);
            }

            // get the target tank ( the tank attached to the pipe )
            OSBackendDatatank pTank=(OSBackendDatatank)Tank.getData();

            if( pTank != null )
            {
               // get the dropped tank
               OSBackendDatatank pSrcTank =
                  (OSBackendDatatank)source.getData();
               if( pSrcTank != null )
               {
                  // add the tables
                  pTank.addTables(pSrcTank.getTables(),bSetJoins);
                  bAdd=true;
               }

            }
         }
         else
            bPrepareSource = true;

         if ( bPrepareSource )
         {
            if (target.getType().equals(PipeFigureFactory.QUERY_PIPE))
            {
               ((ICustomDropSourceData) source.getData()).
                  prepareSourceForDrop(ICustomDropSourceData.DropAction.QUERY,
                  ((ICustomDropSourceData) source.getData()).getFilePath(),
                  null);
            }
            else if (target.getType().equals(PipeFigureFactory.UPDATE_PIPE))
            {
               ((ICustomDropSourceData) source.getData()).
               prepareSourceForDrop(ICustomDropSourceData.DropAction.UPDATE,
               ((ICustomDropSourceData) source.getData()).getFilePath(),
               null);
            }
            iRet=ICustomDropAction.DROP_ACCEPTED;
            bPrepareSource = false;
         }

         if( frame instanceof UIPipeFrame)
         {
            try
            {
               OSDataset dataset = ((UIPipeFrame) frame).getDataset();
               boolean bNeedRemove = true;
               if( bAdd == false )
               {
                  dataset.getPipe().setBackEndDataTank(
                     (PSBackEndDataTank) source.getData());
               // the mappings in mapper must be validated for mis-matched
               // backend references. Bad references will be removed from the
               // mapper.

               // NOTE[AT]: removed temporarily due to bug (wrongfully removed
               // UDFs and mappings with the same name); probably use post-2.0
               //bNeedRemove = mapper.removeMismatchedMappings(
               //  ((OSBackendDatatank)source.getData()).getBackendColumns() );
               }

               OSBackendDatatank beData = (OSBackendDatatank)source.getData();
               Object exitstingBackEndData = null;
               UIFigure existingBackEndFig = navigator.getBackendTank(target);
               if ( null != existingBackEndFig )
                  exitstingBackEndData = existingBackEndFig.getData();

               // If the source BackEndDataTank in the DnD operation has a
               // table with the same alias name as a table in the existing
               // tank, then DO NOT guess. Otherwise, guess the mappings.
               UIFigure figMapper = navigator.getMapper( target );
               if ( null != figMapper )
               {
                  OSDataMapper mapper = (OSDataMapper)figMapper.getData();
                  boolean bNeedGuess = true;
                  if ( null != pageData && null != beData )
                  {
                     //I need to check if the existing backend tank already
                     //has a table that has the same name as one in the
                     //source tank
                     PSCollection tables = beData.getTables();
                     if ( null != tables && null != exitstingBackEndData )
                     {
                        HashMap<String, OSBackendTable> tableMap =
                              new HashMap<String, OSBackendTable>(5);
                        for ( int i = 0; i < tables.size(); i++ )
                        {
                           OSBackendTable table = new OSBackendTable(
                              (PSBackEndTable)tables.get(i) );
                           tableMap.put( table.getAlias(), table );
                        }

                        PSCollection exTables = ((OSBackendDatatank)
                           exitstingBackEndData).getTables();
                        for ( int i = 0; i < exTables.size(); i++ )
                        {
                           OSBackendTable table = new OSBackendTable(
                              (PSBackEndTable)exTables.get( i ) );
                           String tableName = table.getAlias();
                           if ( null != tableMap.get( tableName ) )
                           {
                              bNeedGuess = false;
                              break;
                           }
                        }
                     }
                     mapper.guessMapping( beData, pageData, !bNeedRemove,
                        bNeedGuess );
                  }
               }
            }
            catch (IllegalArgumentException e)
            {
                 E2Designer.getApp().getMainFrame().setCursor(restoreCursor);
               e.printStackTrace();
            }
         }
         E2Designer.getApp().getMainFrame().setCursor(restoreCursor);
         return iRet;
      }

      return ICustomDropAction.DROP_IGNORED;
   }

}
