/**[ CatalogUdfs ]**************************************************************
 * COPYRIGHT (c) 2000 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted 
 * work including confidential and proprietary information of Percussion.
 *
 * $Id: CatalogUdfs.java 1.2 2001/07/10 18:33:36Z JedMcGraw Release $
 *
 * Version Labels  : $Name: Pre_CEEditorUI RX_40_REL $
 *
 * Locked By       : $Locker: $
 *
 * Revision History:
 * $Log: CatalogUdfs.java $
 * Revision 1.2  2001/07/10 18:33:36Z  JedMcGraw
 * Revised documentation to eliminate warnings when making javadocs.
 * Revision 1.1  2000/06/28 00:26:48Z  AlexTeng
 * Initial revision
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.E2Designer.IDataCataloger.ObjectType;
import com.percussion.util.PSCollection;

import java.util.HashMap;

/**
 * This class catalogs the specified application for all UDFs within its data
 * objects.
 */
public class CatalogUdfs
{
   /**
    * Hidden from visibility b/c all methods are static.
    */
   private CatalogUdfs() {}

   /**
    * Gets a collection of datasets within an application and catalogs through
    * the objects looking for UDFs.
    *
    * @param strAppName
    * @param iUdfType
    * @param forceCatalog <CODE>false</CODE> if we want to use a cached list of
    * UDFs, <CODE>true</CODE> if we want to force a catalog to happen. Currently
    * there is no support for using a cached list of UDFs (there is no cache).
    *
    * @return The <CODE>CatalogReceiver</CODE> object used by the
    * <CODE>IDataCataloger</CODE> interface. To get the <CODE>Map</CODE> object
    * of the appropriate UDF type, use the <CODE>CatalogReceiver</CODE>
    * constants (CATALOG_UDF_APP or CATALOG_UDF_SERVER) to
    * {@link CatalogReceiver#get(Object) get} the udfs.
    *
    * @see CatalogReceiver
    */
   public static CatalogReceiver getCatalog( String strAppName,
                                             int iUdfType,
                                             boolean forceCatalog )
   {
      PSCollection datasets = E2Designer.getApp().getMainFrame().getApplicationFrame( strAppName ).getDatasets();
      CatalogReceiver store = new CatalogReceiver();

      if ( (iUdfType & CatalogUdfs.UDF_APP) == CatalogUdfs.UDF_APP  )
         store.put( CatalogReceiver.CATALOG_UDF_APP, new HashMap( 10 ) );

      if ( (iUdfType & CatalogUdfs.UDF_SERVER) == CatalogUdfs.UDF_SERVER )
         store.put( CatalogReceiver.CATALOG_UDF_SERVER, new HashMap( 10 ) );

      for ( int i = 0; i < datasets.size(); i++ )
      {
         OSDataset dataset = (OSDataset)datasets.get( i );
         dataset.catalogData(ObjectType.UDF, store );
      }

      return store;
   }

   /** Signifies that we are cataloging for application udfs. CAN be used in
    * conjunction (bit-wise or) with other CatalogUdfs constants. */
   public static final int UDF_APP = 1;
   /** Signifies that we are cataloging for server udfs. CAN be used in
    * conjunction (bit-wise or) with other CatalogUdfs constants. */
   public static final int UDF_SERVER = 2;
}