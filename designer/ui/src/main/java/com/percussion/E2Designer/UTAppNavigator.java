/******************************************************************************
 *
 * [ UTAppNavigator.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import java.util.Enumeration;
import java.util.Vector;

/**
 * This class provides helper functionality to navigate through all objects
 * within an application.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTAppNavigator
{
   /**
   * Construct the helper object.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
  public UTAppNavigator()
  {
  }


   /**
   * Get all target result pages (the ones connected to the right side of the
   * dataset figure) for the provided dataset.
   *
   * @param      datset   the figure we are starting the search from
   * @return   Vector a vector of (UIConnectableFigure) of all result pages found.
    */
  //////////////////////////////////////////////////////////////////////////////
  public Vector getTargetResultPages(UIConnectableFigure dataset)
  {
     return getResultPages(dataset, UIConnectionPoint.CP_ID_RIGHT);
  }

  /**
   * Get all source-side result pages (the ones connected to the left side of
   * the dataset figure) for the provided dataset.
   *
   * @param      datset   the figure we are starting the search from
   * @return   Vector a vector of (UIConnectableFigure) of all result pages found.
    */
  //////////////////////////////////////////////////////////////////////////////
  public Vector getSourceResultPages(UIConnectableFigure dataset)
  {
     return getResultPages(dataset, UIConnectionPoint.CP_ID_LEFT);
  }


   /**
   * Get all datasets, the provided result page is contained by. Returns null if
   * not found or not attached.
   *
   * @param      resultPage   the figure we are starting the search from
   * @return   Vector a vector(UIConnectableFigure) of all datasets found.
    */
  //////////////////////////////////////////////////////////////////////////////
  public Vector getSourceDatasets(UIConnectableFigure resultPage)
  {
     return getDatasets(resultPage, UIConnectionPoint.CP_ID_LEFT);
  }

   /**
   * Get all target datasets for the provided result page. Returns null if
   * not found or not attached.
   *
   * @param      resultPage   the figure we are starting the search from
   * @return   Vector a vector(UIConnectableFigure) of all datasets found.
    */
  //////////////////////////////////////////////////////////////////////////////
  public Vector getTargetDatasets(UIConnectableFigure resultPage)
  {
     return getDatasets(resultPage, UIConnectionPoint.CP_ID_RIGHT);
  }

  /**
   * Gets a vector of connected external interfaces (static webpages) provided
   * the dataset.
   *
   * @param dataset The dataset figure we are starting the search from.
   * @return Vector A Vector of UIConnectableFigures (OSExternalInterface)
   *
   * attached to the parameter dataset. <CODE>null</CODE> will be returned if
   * no External Interfaces were found.
   */
  public Vector getExternalInterfaces( UIConnectableFigure dataset )
  {
    Vector extInterfaces = new Vector();
    try
    {
      if ( !(dataset instanceof UIConnectableFigure) ||
           dataset.getId() != AppFigureFactory.DATASET_ID )
      {
        throw new IllegalArgumentException("Invalid figure parameter: Dataset figure expected!");
      }
      else
      {
        UIConnectionPoint cp = dataset.getConnectionPoint(UIConnectionPoint.CP_ID_LEFT);
        if (cp != null)  // don't see when this would ever happen...
        {
          for (int i = 0; i < cp.getAttachedFigureCount(); i++)
          {
            UIConnectableFigure connector = cp.getAttached(i);
            if ( connector != null &&
                 connector.getId() == AppFigureFactory.DIRECTED_CONNECTION_ID )
            {
                 Enumeration e = connector.getDynamicConnections();
               while (e.hasMoreElements())
               {
                        cp = (UIConnectionPoint) e.nextElement();
                 UIConnectableFigure fig = cp.getOwner();
                 if (fig != null)
                 {
                    if (fig.getId() == AppFigureFactory.EXTERNAL_INTERFACE_ID)
                        extInterfaces.addElement(fig);
                 }
               }
             }
          }
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return extInterfaces;
  }

  /**
   * Get all the figures attached to the provided UIConnectableFigure.
   * Returns null if not found or not attached.
   *
   * @param      currentFigure   The figure we are starting the search from
   * @return   Vector a vector(UIConnectableFigure) of all figures found.
    */
  //////////////////////////////////////////////////////////////////////////////
  public Vector getAllAttachingFigures(UIConnectableFigure currentFigure)
  {
      Vector figures = new Vector();
     try
    {
      Enumeration connPoints = currentFigure.getConnectionPoints();
      while (connPoints.hasMoreElements())
      {
        UIConnectionPoint cp = (UIConnectionPoint)connPoints.nextElement();
        if (cp != null)
        {
          for (int i=0; i<cp.getAttachedFigureCount(); i++)
          {
            UIConnectableFigure figure = cp.getAttached(i);
            if ( figure != null )
            {
              if ( figure.getId() == AppFigureFactory.DIRECTED_CONNECTION_ID )
              {
                UIConnectionPoint point = ((UIConnector)figure).getOtherEnd(cp);
                if ( null != point )
                {
                  UIConnectableFigure fig = point.getOwner();
                  if ( null != fig && fig != currentFigure )
                    figures.addElement(fig);
                }
              }
              else
                figures.addElement(figure);
            }
          }
        }
      }
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }

    return figures;
  }

  /**
   * Get all the owner datasets attached to the provided UIConnectableFigure.
   * Returns null if not found or not attached.
   *
   * @param      currentFigure   The figure we are starting the search from
   * @return   Vector a vector(UIConnectableFigure) of all figures found.
    */
  //////////////////////////////////////////////////////////////////////////////
  public Vector getAllOwnerDatasets(UIConnectableFigure currentFigure)
  {
    Vector figures = null;
    if ( AppFigureFactory.RESULT_PAGE_ID == currentFigure.getId() )
      figures = getDatasets(currentFigure, UIConnectionPoint.CP_ID_LEFT);
    else
    {
        figures = new Vector( 1 );
       try
      {
        Enumeration connPoints = currentFigure.getDynamicConnections();
        while (connPoints.hasMoreElements())
        {
          UIConnectionPoint cp = (UIConnectionPoint)connPoints.nextElement();
          if ( null == cp && !(cp instanceof UIRigidConnectionPoint) )
            continue;

          UIConnectableFigure figure = cp.getOwner();
          if ( figure != null )
          {
            if ( figure.getId() == AppFigureFactory.DATASET_ID )
            {
              if ( null != figure && figure != currentFigure )
                figures.addElement(figure);
            }
          }
        }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
    }
    if ( figures.isEmpty() )
      figures = null;

    return figures;
  }

   /**
   * Get all datasets for the provided dataset and connection point id.
   * Returns null if not found or not attached.
   *
   * @param      dataset   the figure we are starting the search from
   * @param      cpId   the connection point id
   * @return   Vector a vector(UIConnectableFigure) of all datasets found.
    */
  //////////////////////////////////////////////////////////////////////////////
  private Vector getResultPages(UIConnectableFigure dataset, int cpId)
  {
      Vector resultPages = new Vector();
     try
    {
         if (dataset instanceof UIConnectableFigure &&
             dataset.getId() == AppFigureFactory.DATASET_ID)
      {
            UIConnectionPoint srcCp = dataset.getConnectionPoint(cpId);
            if ( srcCp != null)
            {
               int connPts = srcCp.getAttachedFigureCount();
               for (int i=0; i<connPts; i++)
               {
                  UIConnectableFigure connector = srcCp.getAttached(i);
                  if (connector != null &&
                        connector.getId() == AppFigureFactory.DIRECTED_CONNECTION_ID)
                  {
                     Enumeration e = connector.getDynamicConnections();
                     while (e.hasMoreElements())
                     {
                        UIConnectionPoint cp = (UIConnectionPoint) e.nextElement();
                 UIConnectableFigure fig = cp.getOwner();
                 if (fig != null)
                 {
                    if (fig.getId() == AppFigureFactory.RESULT_PAGE_ID)
                        resultPages.addElement(fig);
                 }
               }
             }
          }
        }
         }
      else
         throw new IllegalArgumentException("UIConnectableFigure.AppFigureFactory.DATASET_ID expected");
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }

    return resultPages;
  }

   /**
    * Get all datasets for the provided result page and connection p[oint id.
    * Returns null if not found or not attached.
    * 
    * @param resultPage the figure we are starting the search from
    * @param cpId the connection point id
    * @return Vector a vector(UIConnectableFigure) of all datasets found.
    */
   //////////////////////////////////////////////////////////////////////////////
   private Vector getDatasets(UIConnectableFigure resultPage, int cpId)
   {
      Vector datasets = new Vector();
      try
      {
         if (resultPage instanceof UIConnectableFigure
            && resultPage.getId() == AppFigureFactory.RESULT_PAGE_ID)
         {
            UIConnectionPoint cp = resultPage.getConnectionPoint(cpId);

            // the left connection point of the result page leads us to all
            // datasets
            // the result page will be contained by.
            if (cp != null)
            {
               for (int i = 0; i < cp.getAttachedFigureCount(); i++)
               {
                  UIConnectableFigure connector = cp.getAttached(i);
                  if (connector != null && connector.getId() == 
                     AppFigureFactory.DIRECTED_CONNECTION_ID)
                  {
                     Enumeration e = connector.getDynamicConnections();
                     while (e.hasMoreElements())
                     {
                        UIConnectionPoint attCp = 
                           (UIConnectionPoint) e.nextElement();
                        UIConnectableFigure fig = attCp.getOwner();
                        if (fig != null)
                        {
                           if (fig.getId() == AppFigureFactory.DATASET_ID || 
                              fig.getId() == 
                                 AppFigureFactory.BINARY_RESOURCE_ID)
                           {
                              datasets.addElement(fig);
                           }
                        }
                     }
                  }
               }
            }
         }
         else
            throw new IllegalArgumentException(
               "UIConnectableFigure.AppFigureFactory.RESULT_PAGE_ID expected");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return datasets;
   }

}

