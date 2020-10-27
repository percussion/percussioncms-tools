/******************************************************************************
 *
 * [ DTContentItemStatus.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSContentItemStatus;

import java.util.Enumeration;

/**
 * The class to represent <code>PSXContentItemStatus</code> param in 
 * ValueSelectorDialog.
 */
public class DTContentItemStatus extends AbstractDataTypeInfo
{
   /**
    * Creates a <code>PSContentItemStatus</code> object from the passed in name.
    *
    * @param strValue a name in the form 'table_name.column_name', may not be
    * <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if strValue is invalid or the supplied 
    * table name is not one of the supported tables of <code>
    * PSContentItemStatus</code>
    * 
    * @return the <code>PSContentItemStatus</code> object, never <code>null
    * </code> or empty.
    */
   public Object create( String strValue )
   {   
      if ( null == strValue || 0 == strValue.trim().length())
         throw new IllegalArgumentException(
            "strValue may not be null or empty.");
   
      char seperator = '.';
      int firstOccur = strValue.indexOf( seperator );
      int lastOccur = strValue.lastIndexOf( seperator );
      if ( firstOccur != lastOccur  || firstOccur == -1)
         throw new IllegalArgumentException(
            "There is a problem with seperators!");
   
      String tableName = null;
      if ( firstOccur > 0 )
         tableName = strValue.substring( 0, firstOccur );      
      
      if(!PSContentItemStatus.isSupportedTable(tableName))
         throw new IllegalArgumentException(
            "The table <" + tableName + "> is not supported");
  
      String colName = strValue.substring( firstOccur+1 );
      if(colName.trim().length() == 0)
         throw new IllegalArgumentException("empty column name is not allowed");
      
      return new PSContentItemStatus(tableName, colName);         
   }
   
   /**
    * @returns a enum containing Strings of all available content item status values.
    */
   public Enumeration catalog()
   {
      return CatalogContentItemStatus.getCatalog(false).elements();
   }
}
