/*[ CatalogHtmlParam.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.E2Designer.IDataCataloger.ObjectType;
import com.percussion.util.PSSortTool;

import java.io.Serializable;
import java.text.Collator;
import java.util.Set;
import java.util.Vector;

/**
 * Catalogs and caches all user context information on this Rhythmyx server.
 */
////////////////////////////////////////////////////////////////////////////////
public class CatalogHtmlParam implements Serializable
{
  /**
   * Do not construct new instances. Use the static function.
   */
  private CatalogHtmlParam() {}

  /**
   * Get the catalog of all HtmlParameter names related to the current figure.
   * If available, cached data will be returned unless a new cataloging is
   * forced.
   *
   * @param figure The figure that openned its editor dialog and will receive
   * the catalog results.
   * @param   forceCatalog true to force a new cataloging
   *
   * @return Vector a vector of all cataloged elements; this Vector will never
   * be <CODE>null</CODE>.
   * @todo 1 Must change pipe figure retrival more generic; see commented code.
    */
  //////////////////////////////////////////////////////////////////////////////
  public static Vector getCatalog( UIFigure figure, boolean forceCatalog )
  {
    if ( null != m_catalog && !forceCatalog )
      return getParams( true );

    IDataCataloger catalogPerformer = null;
    UTAppNavigator aNavigator = new UTAppNavigator();
    UTPipeNavigator pNavigator = new UTPipeNavigator();
    m_catalog = new CatalogReceiver();

    if ( null == figure )
      return new Vector(0);
    else
    {
      /* Removed for bug (Rx-00-01-0009)
      // get RequestTypeHtmlParamName from this application
      UIFigureFrame frame = Util.getOwnerFrameOf( figure );
      catalogPerformer = (IDataCataloger)frame.getData();
      catalogPerformer.catalogData( IDataCataloger.HTML_PARAM, m_catalog );
      */
      String sFigName = figure.getName();
      if ( ((UIConnectableFigure)figure).getFactoryName().equals("AppFigureFactory") )
      {
        if ( sFigName.equals(AppFigureFactory.DATASET) ||
             sFigName.equals(AppFigureFactory.QUERY_DATASET) ||
             sFigName.equals(AppFigureFactory.UPDATE_DATASET) ||
             sFigName.equals(AppFigureFactory.BINARY_RESOURCE) )
        { // REQUESTOR
          catalogPerformer = (IDataCataloger)figure.getData();
          catalogPerformer.catalogData(ObjectType.HTML_PARAM, m_catalog);
        }
        else
        {
          // get all the datasets attached to the connector of this object
          // (sort later)
          Vector vDatasetList = aNavigator.getAllOwnerDatasets( (UIConnectableFigure)figure );
          if ( null != vDatasetList )
          {
            for (int i = 0; i < vDatasetList.size(); i++)
            {
              UIConnectableFigure fig = (UIConnectableFigure)vDatasetList.get(i);
              catalogPerformer = (IDataCataloger)fig.getData();
              catalogPerformer.catalogData(ObjectType.HTML_PARAM, m_catalog);
            }
          }
          else // no datasets, then just catalog self
          {
            catalogPerformer = (IDataCataloger)figure.getData();
            catalogPerformer.catalogData(ObjectType.HTML_PARAM, m_catalog);
          }
        }
      }
      else if ( sFigName.equals(PipeFigureFactory.MAPPER) ) // MAPPER
      {
        UIFigure pipeFigure = pNavigator.getPipe( figure );
        catalogPerformer = (IDataCataloger)((IOSPipe)pipeFigure.getData()).getDataset();
        catalogPerformer.catalogData(ObjectType.HTML_PARAM, m_catalog);
      }
      else if ( sFigName.equals(PipeFigureFactory.SELECTOR) ) // SELECTOR
      {
        UIFigure pipeFigure = pNavigator.getPipe( figure );
        catalogPerformer = (IDataCataloger)((IOSPipe)pipeFigure.getData()).getDataset();
        catalogPerformer.catalogData(ObjectType.HTML_PARAM, m_catalog);
      }
      /*
      else if ( ((UIConnectableFigure)figure).getFactoryName().equals("PipeFigureFactory") )
      {
        Vector vObjList = pNavigator.getAllFiguresAttached( figure );
        if ( null != vObjList )
        {
          for (int i = 0; i < vObjList.size(); i++)
          {
            UIFigure fig = (UIFigure)vObjList.get(i);
            System.out.println(fig.getName());
            Object objData = fig.getData();
            if ( objData instanceof IDataCataloger )
            {
              catalogPerformer = (IDataCataloger)objData;
              catalogPerformer.catalogData( IDataCataloger.HTML_PARAM, m_catalog );
            }
          }
        }
      }
      */
    }
      return getParams( true );
  }

  /**
   * Adds a new HTML parameter name to the cached hashmap. This is made
   * available because on DTHtmlParameter.create() call, the new string name
   * should be remembered before the editor dialog has a chance to update its
   * data members.
   *
   * @param   paramName A new HTML param name to be added before the editor dialog
   * can be closed. It will always be a valid String object.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
  public static void addNewParamToMap( String paramName )
  {
    // this doesn't work because when you open another ValueSelectorDialog,
    // it'll recatalog (sets m_catalog to new...)
    if ( null == m_catalog )
      CatalogHtmlParam.getCatalog( Util.getFigure(), false);

    m_catalog.add( paramName );
  }

  /**
   *
   */
  public static void resetParams()
  {
    m_catalog = null;
  }

  /**
   * Retrieves a Vector representation of all the HTML parameter names stored in
   * the cached hashmap. If the bSort flag is set, sort the array in alpha
   * ascending order.
   *
   * @param bSort <CODE>true</CODE> = sort the return Vector in alpha ascending
   * order.
   * @returns Vector A Vector representation of all the HTML parameter names
   * stored in the cached hashmap. <CODE>null</CODE> will NEVER be returned.
   */
  private static Vector getParams(boolean bSort)
  {
    if ( null == m_catalog )
      return new Vector(0);

    Set hashKeys = m_catalog.getCatalogData().keySet();
    if ( hashKeys.isEmpty() )
      return new Vector(0);

    // alpha sorting html parameters
    Object[] paramArray = hashKeys.toArray();

    if (bSort)
      PSSortTool.QuickSort( paramArray, Collator.getInstance() );

    Vector<Object> vCatalog = new Vector<Object>( paramArray.length );
    for (int i = 0; i < paramArray.length; i++)
      vCatalog.addElement( paramArray[i] );

    return vCatalog;
  }

  //////////////////////////////////////////////////////////////////////////////
  private static CatalogReceiver m_catalog = null;
}
