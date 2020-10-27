/*[ IConnectionConstraint.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;



/**
*  This interface is used to decide if a connection between connection
*  points can be established
*/
public interface IConnectionConstraint
{
   public boolean acceptConnection(int ID, UIConnectionPoint cp);   
}   
