/*[ DTTextLiteral.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSTextLiteral;

public class DTTextLiteral extends AbstractDataTypeInfo
{
   /**
    * Creates the server object for which this object acts as a proxy, using
    * the supplied text as the value for the object.
    *
    * @param value The literal text for this object. If <code>null</code>,
    * the empty string is used.
    */
   public Object create( String value )
   {
      if ( null == value )
         value = "";
      return new PSTextLiteral( value );
   }
}
