/*[ UninitializedException.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

/**
 * This execption is thrown when an object is accessed before it has been 
 * initialized.
 */
public class UninitializedException extends RuntimeException 
{
   public UninitializedException()
   {
      
   }

   public UninitializedException(String strDetail)
   {
      super(strDetail);
   }
}
