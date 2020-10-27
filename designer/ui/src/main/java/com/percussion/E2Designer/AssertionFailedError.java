/*[ AssertionFailedError.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

/**
 * This is a debugging class used to implement an Assertion framework.
 */
class AssertionFailedError extends RuntimeException
{
   AssertionFailedError(String strDetail)
   {
      super(strDetail);
   }

   // variables
}   
