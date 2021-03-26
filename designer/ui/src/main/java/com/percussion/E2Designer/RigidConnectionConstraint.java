/******************************************************************************
 *
 * [ RigidConnectionConstraint.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.E2Designer;

import java.io.Serializable;


/**
*  This class makes sure we attaching to a rigid connection point
*/
public class RigidConnectionConstraint implements IConnectionConstraint, Serializable
{

   public RigidConnectionConstraint()
   {
      super();
   }

   /**
   *
   * @param id is the id of the connection point trying to connect
   * @param cp is the connection point trying to connect.
   *
   * @return boolean indicating if the connection should be accepted
   */
   public boolean acceptConnection(int ID, UIConnectionPoint cp)
   {
      return cp != null && cp instanceof UIRigidConnectionPoint;
   }

   //the owner of the constraint
   public UIConnectionPoint m_cp = null;

}
