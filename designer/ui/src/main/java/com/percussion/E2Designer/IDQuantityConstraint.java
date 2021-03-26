/*[ IDQuantityConstraint.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


import java.io.Serializable;

/**
*  This class allows us to restrict the number of attachments per id
*/
public class IDQuantityConstraint implements IConnectionConstraint, Serializable
{
   public IDQuantityConstraint(UIConnectionPoint cp, int ID, int iMax)
   {
      m_cp = cp;      
      m_id = ID;
      m_iMax = iMax;
   }

   /**
   * @param id is the id of the object connecting
   * @param cp is the connection point trying to connect
   */
   public boolean acceptConnection(int ID, UIConnectionPoint cp)
   {
      //see if we are over the max
      int iCount = 0;
      for(int iAttach = 0; iAttach < m_cp.getAttachedFigureCount(); ++iAttach)
      
      {
         UIConnectableFigure connector = m_cp.getAttached(iAttach);
         if(connector instanceof UIConnector)
         {
            UIConnectionPoint otherend = ((UIConnector)connector).getOtherEnd(m_cp);
            if(otherend != null && otherend.getOwner().getId() == m_id)
            {
               ++iCount;
               if(iCount >= m_iMax)
                  return(false);
            }
         }
      } 

      return(true);
   }

   //the owner of the constraint
   private UIConnectionPoint m_cp = null;
   private int m_iMax, m_id;
}   
