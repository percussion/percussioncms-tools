/*[ DTCgiVariable.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSCgiVariable;

import java.util.Enumeration;

public class DTCgiVariable extends AbstractDataTypeInfo
{
   /**
    * Creates a PSCgiVariable object, using the supplied strValue as the name.
    */
   public Object create( String strValue )
   {
      if ( null == strValue || 0 == strValue.trim().length())
         throw new IllegalArgumentException();
      try
      {
         return new PSCgiVariable( strValue );
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * @returns a vector containing Strings of all available CGI Variables
    * by performing a catalog from the E2 Server.
    */
   public Enumeration catalog()
   {
    return CatalogCgiVariables.getCatalog(false).elements();
   }

  //////////////////////////////////////////////////////////////////////////////
   // private storage
}
