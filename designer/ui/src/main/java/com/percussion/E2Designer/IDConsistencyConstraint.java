/*[ IDConsistencyConstraint.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


import java.io.Serializable;

/**
*  This class makes sure we only attach to one id at a time
*/
public class IDConsistencyConstraint implements IConnectionConstraint, Serializable
{
   public IDConsistencyConstraint(UIConnectionPoint cp)
   {
      m_cp = cp;      
   }

   /**
   * @param id is the id of the object connecting
   * @param cp is the connection point trying to connect
   */
   public boolean acceptConnection(int ID, UIConnectionPoint cp)
   {
      //return false if we are already attached to another id
      //I need the cp from the other end
      if(m_cp.getAttachedFigureCount() > 0)
      {
         UIConnectableFigure connector = m_cp.getAttached();
         if(connector instanceof UIConnector)
         {
            UIConnectionPoint otherend = ((UIConnector)connector).getOtherEnd(m_cp);
            if(otherend != null && otherend.getOwner().getId() != ID)
               return(false);
         }
      }
         
      return(true);
   }

   //the owner of the constraint
   public UIConnectionPoint m_cp = null;
}   
