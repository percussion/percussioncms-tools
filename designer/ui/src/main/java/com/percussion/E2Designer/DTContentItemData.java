/******************************************************************************
 *
 * [ DTContentItemData.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSContentItemData;

import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * The class to represent <code>PSXContentItemData</code> param in
 * ValueSelectorDialog.
 */
public class DTContentItemData extends AbstractDataTypeInfo
{
   /**
    * Creates a <code>PSContentItemData</code> object from the passed in name.
    *
    * @param strValue a name of the CE flield, may not be
    * <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if strValue is invalid or not supported
    * by <code>PSContentItemData</code>
    *
    * @return the <code>PSContentItemData</code> object, never <code>null
    * </code> or empty.
    */
   public Object create( String strValue )
   {
      if ( null == strValue || 0 == strValue.trim().length())
         throw new IllegalArgumentException(
            "strValue may not be null or empty.");

      if(!isSupportedField(strValue))
         throw new IllegalArgumentException(
            "The field name <" + strValue + "> is not supported");

      //strip out field scope name that is not needed on the server side
      StringTokenizer tokens = new StringTokenizer(strValue, "/");
      
      String fieldName = null;
      
      do 
      {
         fieldName = (String)tokens.nextElement();
      
      }while(tokens.hasMoreElements());
      
      return new PSContentItemData(fieldName);
   }
   
   /**
    * Checks whether the supplied field name is supported, i.e if it exists 
    * in the catalog of all fields.
    * 
    * @param fieldName, may not be <code>null</code> or empty.
    * 
    * @return <code>true</code> if it is supported, otherwise <code>false</code>
    */
   private boolean isSupportedField(String fieldName)
   {
      if (fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException("fieldName may not be null or empty.");

      return CatalogContentEditorFields.getCatalog(false).contains(fieldName);
   }

   /**
    * @returns a enum containing Strings of all available content editor
    * fields formatted as scope/fieldName, where scope is system, shared
    * or local.
    */
   public Enumeration catalog()
   {
      return CatalogContentEditorFields.getCatalog(false).elements();
   }
}
