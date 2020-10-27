/*[ AbstractDataTypeInfo.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Default implementations for all methods in the interface except <code>create</code>.
 */
public abstract class AbstractDataTypeInfo implements IDataTypeInfo
{
   /**
    * @returns <code>true</code> always
    */
   public boolean allowUncatalogedValues()
   {
      return true;
   }
   
   /**
    * @returns the displayable name for this type
    */
   public String getDisplayName()
   {
      String strClass = getClass().getName();
      return E2Designer.getResources().getString( 
            strClass.substring( strClass.lastIndexOf('.')+1));
   }

   /**
    * @returns an empty vector
    */
   public Enumeration catalog()
   {
      Vector v = new Vector(0);
      return v.elements();
   }
}
