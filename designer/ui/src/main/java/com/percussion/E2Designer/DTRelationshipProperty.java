/*[ DTRelationshipProperty.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipProperty;
import com.percussion.util.IPSHtmlParameters;

import java.util.Enumeration;
import java.util.Vector;

/**
 * The class to represent <code>PSXRelationshipProperty</code> param in 
 * ValueSelectorDialog.
 */
public class DTRelationshipProperty extends AbstractDataTypeInfo
{
   /**
    * Creates a <code>PSRelationshipProperty</code> object from the passed in 
    * name.
    *
    * @param strValue the property name, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if strValue is invalid.
    * 
    * @return the <code>PSRelationshipProperty</code> object, never <code>null
    * </code>.
    */
   public Object create( String strValue )
   {   
      if ( null == strValue || 0 == strValue.trim().length())
         throw new IllegalArgumentException(
            "strValue may not be null or empty.");
      
      return new PSRelationshipProperty(strValue);         
   }

   /**
    * @returns a enum containing Strings of all available
    * originating relationship types, never <code>null</code>.
    */
   public Enumeration catalog()
   {
      if (ms_catalog==null)
      {
         ms_catalog = new Vector();
        
         //add 'name' and 'category'
         ms_catalog.add(PSRelationshipConfig.XML_ATTR_NAME);
         ms_catalog.add(PSRelationshipConfig.XML_ATTR_CATEGORY);
         
         //add 'owner/sys_contentid' 'owner/sys_revision'  
         ms_catalog.add(PSRelationshipConfig.XML_ATTR_OWNER +
            "/" + IPSHtmlParameters.SYS_CONTENTID);
            
         //add 'dependent/sys_contentid' 'dependent/sys_revision'  
         ms_catalog.add(PSRelationshipConfig.XML_ATTR_DEPENDENT +
            "/" + IPSHtmlParameters.SYS_CONTENTID);
            
         ms_catalog.add(PSRelationshipConfig.XML_ATTR_DEPENDENT +
            "/" + IPSHtmlParameters.SYS_REVISION);
         
         for (int i = 0; i < PSRelationshipConfig.RS_PROPERTY_NAME_ENUM.length; i++)
         {
            //add 'property/RS_' property names
            ms_catalog.add(PSRelationshipConfig.XML_ATTR_PROPERTY + "/" +
               PSRelationshipConfig.RS_PROPERTY_NAME_ENUM[i]);
         }
      }
      
      return ms_catalog.elements(); 
   }
   
   /**
    * Contains Strings of all available originating relationship replacement
    * names, lazy-loaded by the {@link #catalog()} method,
    * never <code>null</code> after that. 
    */
   private static Vector ms_catalog;      
}
