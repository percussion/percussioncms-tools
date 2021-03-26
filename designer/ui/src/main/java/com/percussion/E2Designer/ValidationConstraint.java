/*[ ValidationConstraint.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

/** Defines the 2 required methods for all constraint subclasses.
  *
  * @see ValidationFramework
  * @see IntegerConstraint
  * @see StringConstraint
*/

public interface ValidationConstraint
{
/** Method definition to return the error message to be posted by the warning
  * Dialog when a component contains an invalid value.
*/
  public String getErrorText();

/** Method definition to validate the value of the component (Object) passed in. 
  *
*/
  public void checkComponent(Object x) throws ValidationException;

}

 
