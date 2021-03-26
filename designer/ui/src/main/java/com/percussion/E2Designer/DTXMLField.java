/*[ DTXMLField.java ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSXmlField;

import java.util.Enumeration;
import java.util.Vector;

public class DTXMLField extends AbstractDataTypeInfo
{
   /**
    * @param xmlElements list of all elements in the page datatank associated with 
    * the current context. Each entry should support toString to create a String 
    * of the form 'element1/element2/element3' (slashes as level separators).
    * All elements in the vector should be Strings.
    *
    * @throws IllegalArgumentException if all elements in the supplied vector
    * are not Strings
    */
   public DTXMLField( Vector xmlElements )
   {
      for ( int index = xmlElements.size()-1; index >= 0; index-- )
      {
         if ( !( xmlElements.get( index ) instanceof String ))
            throw new IllegalArgumentException();
      }
      m_xmlElements = xmlElements;
   }
   
   /**
    * Creates a PSXMLField object.
    *
    * @param strValue a name obtained from <code>catalog</code> of the form 
    * 'element1/element2/element3', or any other name that matches this form.
     * If an empty string is provided, then the first component of the 
     * first element in the catalog is used. If there are no elements in 
     * the catalog, the string "document" is used. 
    *
    * @throws IllegalArgumentException if strValue is null. Empty is allowed
     * as it is needed for default values for the mapper.
    */
   public Object create( String strValue ) 
         throws IllegalArgumentException
   {
      if ( null == strValue)
         throw new IllegalArgumentException();
        
        if (strValue.trim().length() == 0)
        {
           Enumeration e = catalog();
           if (e.hasMoreElements())
           {
              Object first = e.nextElement();
              String xmlelements = first.toString();
              int sep = xmlelements.indexOf('/');
              if (sep > 0)
              {
                 strValue = xmlelements.substring(0, sep);
              }
              else
              {
                 strValue = xmlelements;
              }
           }
           else
           {
              strValue = "document";
           }
        }

      return new PSXmlField( strValue );
   }

   /**
    * @returns a vector containing Strings of all the column names in the form
    * 'element1/element2/element3'. One of these may be passed as the param to the
    * <code>create</code> method, if it is called.
    */
   public Enumeration catalog()
   {
      return m_xmlElements.elements();
   }

   // storage
   private Vector m_xmlElements = null;
}
