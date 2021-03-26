/*[ IDConstraint.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


import java.io.Serializable;

/**
*  This class makes sure we only attach to a connection point that allows this id
*/
public class IDConstraint implements IConnectionConstraint, Serializable
{
   public IDConstraint(UIConnectionPoint cp)
   {
      m_cp = cp;      
   }

   /**
   * @param id is the id of the object connecting
   * @param cp is the connection point trying to connect
   */
   public boolean acceptConnection(int ID, UIConnectionPoint cp)
   {
      int index = m_cp.getIndexOfID(ID);
      return index >= 0;
   }

   //the owner of the constraint
   public UIConnectionPoint m_cp = null;
}   
