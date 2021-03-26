/*[ ValidationException.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

/** Signals an invalid component value has been entered; checked during component
  * validation.
  *
  * @see ValidationFramework
  * @see ValidationConstraint
*/

public class ValidationException extends Exception
{
  
  public ValidationException()
  { super(); }

  public ValidationException(String s)
  { super(s); }
}

 
