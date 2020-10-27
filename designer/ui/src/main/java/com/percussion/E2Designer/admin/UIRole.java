/*[ UIRole.java ]**************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSRole;
/**
 * A UI wrapper class for the PSRole.
 */

public class UIRole
{  
   /**
    * Constructor that takes in the PSRole and wrapps it for the UI purpose
    * Can not be <CODE>null</CODE>
    * @param  a PSRole, can not be <CODE>null</CODE>
    * @throws if role is <CODE>null</CODE>
    */
   public UIRole(PSRole role)
   {
      if (role == null)
      {
         throw new IllegalArgumentException("The role can not be null");
      }
      else 
         m_role = role;
   }

   /**
    * Gets the role
    * @return a PSRole, never <CODE>null</CODE>
    */
   public PSRole getRole ()
   {
      return m_role;
   }

   /**
    * Returns a string string representation of the role. For the UI purposes
    * this string will be the name of the role.  Never <CODE>null</CODE>
    * @return a string representation of the role, never <CODE>null</CODE>
    */
   public String toString()
   {
      return m_role.getName();
   }

   /** A PSRole object gets initialized in the constructor*/
   private PSRole m_role = null;
}
