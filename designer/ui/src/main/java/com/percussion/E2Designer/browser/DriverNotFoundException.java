/*[ DriverNotFoundException.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

class DriverNotFoundException extends RuntimeException
{
   // constructors
   public DriverNotFoundException( String strMessage )
   {
      super( strMessage );
   }
}
