/*[ IOConstraint.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


import java.io.Serializable;

/**
*  This class makes sure we do not attach an input to an input or output to an output
*/
public class IOConstraint implements IConnectionConstraint, Serializable
{
   public IOConstraint(UIConnectionPoint cp)
   {
      m_cp = cp;      
   }

   /**
   * @param id is the id of the object connecting
   * @param cp is the connection point trying to connect
   */
   public boolean acceptConnection(int ID, UIConnectionPoint cp)
   {
      //only flexible connection points can be inputs and outputs
      if(cp != null && cp instanceof UIFlexibleConnectionPoint && m_cp instanceof UIFlexibleConnectionPoint)
      {
         if(((UIFlexibleConnectionPoint)cp).isAnInput() != 
            ((UIFlexibleConnectionPoint)m_cp).isAnInput())
            {
//            System.out.println("they are not the same.");
            return(true);
         }
      }

//      System.out.println("they are the same.");
      return(false);
   }

   //the owner of the constraint
   public UIConnectionPoint m_cp = null;
}   
