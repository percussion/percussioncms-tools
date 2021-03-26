/*[ DTUserContext.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSUserContext;

import java.util.Enumeration;

public class DTUserContext extends AbstractDataTypeInfo
{
   /**
    * Creates a DTUserContext object, using the supplied strValue as the name.
    *
    * @throws IllegalArgumentException if strValue is null or empty
    */
   public Object create(String strValue) throws IllegalArgumentException
   {
      if (null == strValue || 0 == strValue.trim().length())
         throw new IllegalArgumentException();

      return new PSUserContext(strValue);
   }

   /**
    * @returns a vector containing Strings of all available User Context Variables
    */
   public Enumeration catalog()
   {
    return CatalogUserContext.getCatalog(false).elements();
   }

  //////////////////////////////////////////////////////////////////////////////
}

