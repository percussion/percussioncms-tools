/*[ CatalogDtds.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.util.Iterator;
import java.util.Vector;

/**
 * This object performs the &quot;Lazy Polling&quot; of all attaching
 * (left-side) ResultPages for the PageDataTank.
 */
public class CatalogDtds
{
   /**
    * Gets the DTDs in PSDtdTree format from all attached ResultPage objects and
    * returns them in a vector.
    *
    * @param dataset This figure must be a dataset object; if it is not, an empty list
    * is returned. Passing in a <CODE>null</CODE> here will cause the method to return
    * immediately.
    *
    * @param bForceCatalog <CODE>true</CODE> will force cataloging
    * and a <CODE>false</CODE> will simply use cached data. This method must
    * always poll to see if any changes have been made. If none are detected,
    * the cached data will be returned unless the flag is true.
    *
    * @return A list of PSDtdTrees representing all the DTDs. Returns
    * an empty Vector if list is empty.
    */
   public static Vector getCatalog( UIConnectableFigure dataset, boolean bForceCatalog )
   {
      Vector list = new Vector(0);

      // do param validation
      if ( null == dataset )
         return list;
      String datasetType = dataset.getType();
      int datasetId = dataset.getId();
      if ( !AppFigureFactory.QUERY_DATASET.equals( datasetType )
            && !AppFigureFactory.UPDATE_DATASET.equals( datasetType ))
      {
         return list;
      }

      CatalogReceiver receiver = null;

      UTAppNavigator aNavigator = new UTAppNavigator();
      Vector pageList = aNavigator.getTargetResultPages( dataset );
      if ( null == pageList )
         return list;

      receiver = new CatalogReceiver();
      for (int i = 0; i < pageList.size(); i++)
      {
         UIConnectableFigure pageFigure = (UIConnectableFigure)pageList.get( i );
         IDataCataloger cataloger = (IDataCataloger)pageFigure.getData();
         cataloger.catalogData( IDataCataloger.ObjectType.XML_DTD, receiver );
      }

      Iterator iterator = receiver.getCatalogData().keySet().iterator();
      while (iterator.hasNext())
         list.addElement( iterator.next() );

    return list;
  }
}
