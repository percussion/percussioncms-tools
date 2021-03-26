/******************************************************************************
 *
 * [ CatalogExtendedTypes.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.E2Designer;

import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.design.catalog.PSCataloger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

/**
 *this class catalog table(s) searching for primary key, foreign keys
 * and unique keys on the passed table.
 */

public class CatalogExtendedTypes
{
   /**
    * catalogs and returns the extended table types, such as primary keys.
    * 
    * @param osTable a valid OSBackendTable. May not be <code>null</code>.
    * 
    * @param keyType indicates the type of cataloger to construct, must be one 
    * of these ID's:
    *  <table BORDER COLS=1 WIDTH="50%" >
    *     <tr>
    *        <td>FOREIGN_KEY</td>
    *        <td>PRIMARY_KEY</td>
    *        <td>UNIQUE_KEY</td>
    *     </tr>
    *  </table>
    * 
    * @param forceCatalog if <code>true</code> recatalog the table
    * 
    * @return The catalog, never <code>null</code>.
    */
   public static ExtendedTypes getCatalog(OSBackendTable osTable, int keyType,
      boolean  forceCatalog)
   {

      ExtendedTypes m_catalog = null;
      Vector vc = null;

      if (osTable == null)
         throw new IllegalArgumentException();

      try
      {
         String table = osTable.getTable();
         String strDatasource = osTable.getDataSource();

         // generate the key
         String key = strDatasource + ":" + table + getTextType(keyType);

         // try to get the vector from the map
         m_catalog = (ExtendedTypes) m_catalogMap.get(key);

         // if force then will recatalog
         if (forceCatalog && m_catalog != null)
            m_catalogMap.remove(key);

         if (m_catalog == null || forceCatalog == true)
         {
            m_catalog = new ExtendedTypes();
            Properties properties = new Properties();
            String scType;

            // prepare the properties
            properties.put("RequestCategory", "data");
            properties.put("Datasource", strDatasource);
            properties.put("TableName", table);

            switch (keyType)
            {
               case FOREIGN_KEY : // request foreign keys
                  properties.put("RequestType", "ForeignKey");
                  break;

               // primary and unique keys are returned by the same cataloger
               case PRIMARY_KEY :
               case UNIQUE_KEY :
                  properties.put("RequestType", "UniqueKey");
                  break;
            }
            PSCataloger cataloger = new PSCataloger(E2Designer.getDesignerConnection());
            Document columns = cataloger.catalog(properties);
            PSCatalogResultsWalker column = new PSCatalogResultsWalker(columns);
            switch (keyType)
            {
               // please note that this only sets the vector and FALLS TROUGH
               case UNIQUE_KEY :
                  //CHECKSTYLE:OFF
                  vc = new Vector(); // fall trough
               case PRIMARY_KEY :
               {
                  //CHECKSTYLE:ON
                  // extract all the elements
                  while (column.nextResultObject("UniqueKey"))
                  {
                     // get the type
                     scType = column.getResultData("type");
                     if (scType != null)
                     {
                        // walk all columns
                        Node cur = column.getCurrent();
                        while (column.nextResultObject("name"))
                        {
                           String name = column.getElementData();
                           if (keyType == PRIMARY_KEY
                              && scType.equals("primaryKey"))
                           {
                              ExtendedTypesPrimaryKey primary = 
                                 new ExtendedTypesPrimaryKey(
                                    osTable.getTable(), name);
                              m_catalog.addType(primary);
                           }
                           else if (keyType == UNIQUE_KEY
                              && scType.equals("index"))
                           {
                              vc.addElement(name);
                           }
                        }

                        // set back to key element
                        column.setCurrent(cur);
                     }
                  }
                  // if unique then
                  if (keyType == UNIQUE_KEY && vc != null)
                  {
                     // construct the ExtendedTypesUniqueKeys
                     ExtendedTypesUniqueKeys unique = new ExtendedTypesUniqueKeys(
                        osTable.getTable(), vc);
                     m_catalog.addType(unique);
                  }
                  break;
               }

               case FOREIGN_KEY :
               {
                  String primaryKeyColumnName, foreignTableSchema;
                  String foreignKeyTableName, foreignKeyColumnName;
                  // search for foreign key
                  while (column.nextResultObject("ForeignKey"))
                  {
                     scType = column.getResultData("type");
                     if (scType != null && scType.equals("ForeignKey"))
                     {
                        primaryKeyColumnName = "";
                        foreignTableSchema = "";
                        foreignKeyTableName = "";
                        foreignKeyColumnName = "";
                        primaryKeyColumnName = column.getElementData("name",
                           false);
                        if (primaryKeyColumnName != null)
                        {
                           if (column.nextResultObject("ExternalColumn"))
                           {
                              foreignTableSchema = column.getElementData(
                                 "schemaName", false);
                              foreignKeyTableName = column.getElementData(
                                 "tableName", false);
                              foreignKeyColumnName = column.getElementData(
                                 "columnName", false);
                           }
                           if (foreignTableSchema != null
                              && foreignKeyTableName != null
                              && foreignKeyColumnName != null)
                           {
                              // construct a ExtendedTypesForeignKey
                              ExtendedTypesForeignKey foreign = 
                                 new ExtendedTypesForeignKey(
                                    primaryKeyColumnName, foreignTableSchema,
                                    foreignKeyTableName, foreignKeyColumnName);

                              m_catalog.addType(foreign); // add it
                           }
                        }
                     }
                  }// while
               }// case foreign key
            }// switch

            // add into the cataloger list
            m_catalogMap.put(key, m_catalog);
         } //    if( m_catalog == null || forceCatalog == true)

      }// try
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return m_catalog;
  }

  /**
    * returns a string containing the extended types  name
    *
    * @param type a valid CatalogExtendedTypes enum, If invalid,
   * an IllegalArgumentException is thrown.
    *
    * @throws IllegalArgumentException if type is not  FOREIGN_KEY,
   * PRIMARY_KEY or  UNIQUE_KEY
   *
    */
  private static String getTextType(int type)
  {
      String strRet;
      switch(type)
      {
         case FOREIGN_KEY:
                  strRet="FOREIGN_KEY";
         break;

         case PRIMARY_KEY:
               strRet="PRIMARY_KEY";
         break;

         case UNIQUE_KEY:
                   strRet="UNIQUE_KEY";
         break;
         default:
               throw new IllegalArgumentException();

       }
       return(strRet);
  }
 /**
 * catalog foreign keys
 */
  public static final int FOREIGN_KEY =1;

 /**
 * catalog the primary key
 */
  public static final int PRIMARY_KEY =2;

  /**
   * catalog unique key(s)
   */
  public static final int UNIQUE_KEY=3;

  /**
  * hash to contain the catalog tables
  */
  private static HashMap m_catalogMap = new HashMap();

}
